package me.friedhof.hyperbuilder.save;

import me.friedhof.hyperbuilder.computation.modules.World;
import me.friedhof.hyperbuilder.computation.modules.Player;
import me.friedhof.hyperbuilder.computation.modules.Chunk4D;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages saving and loading of world data to/from disk.
 * Handles world metadata, chunk data, and player data persistence.
 */
public class WorldSaveManager {
    private static final String SAVES_DIRECTORY = "saves";
    private static final String WORLD_INFO_FILE = "world.dat";
    private static final String PLAYER_DATA_FILE = "player.dat";
    private static final String CHUNKS_DIRECTORY = "chunks";
    
    private final Path savesPath;
    
    public WorldSaveManager() {
        this.savesPath = Paths.get(SAVES_DIRECTORY);
        createSavesDirectory();
    }
    
    /**
     * Creates the saves directory if it doesn't exist.
     */
    private void createSavesDirectory() {
        try {
            if (!Files.exists(savesPath)) {
                Files.createDirectories(savesPath);
                System.out.println("Created saves directory: " + savesPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create saves directory: " + e.getMessage());
        }
    }
    
    /**
     * Gets a list of all saved worlds.
     * 
     * @return List of SavedWorldInfo objects for all saved worlds
     */
    public List<SavedWorldInfo> getSavedWorlds() {
        List<SavedWorldInfo> worlds = new ArrayList<>();
        
        try {
            if (!Files.exists(savesPath)) {
                return worlds;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(savesPath, Files::isDirectory)) {
                for (Path worldDir : stream) {
                    Path worldInfoFile = worldDir.resolve(WORLD_INFO_FILE);
                    if (Files.exists(worldInfoFile)) {
                        try {
                            SavedWorldInfo worldInfo = loadWorldInfo(worldInfoFile);
                            if (worldInfo != null) {
                                worlds.add(worldInfo);
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to load world info from " + worldDir + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to list saved worlds: " + e.getMessage());
        }
        
        // Sort by last played date (most recent first)
        worlds.sort((a, b) -> b.getLastPlayed().compareTo(a.getLastPlayed()));
        return worlds;
    }
    
    /**
     * Saves a world to disk.
     * 
     * @param world The world to save
     * @param player The player data to save
     * @return true if the save was successful, false otherwise
     */
    public boolean saveWorld(World world, Player player) {
        try {
            String worldName = world.getName();
            Path worldDir = savesPath.resolve(sanitizeFileName(worldName));
            
            // Create world directory if it doesn't exist
            if (!Files.exists(worldDir)) {
                Files.createDirectories(worldDir);
            }
            
            // Save world info
            SavedWorldInfo worldInfo = new SavedWorldInfo(
                worldName, 
                world.getSeed(), 
                LocalDateTime.now(), // This will be overwritten if loading existing world
                LocalDateTime.now(),
                worldDir.getFileName().toString()
            );
            
            // Check if world info already exists to preserve creation date
            Path worldInfoFile = worldDir.resolve(WORLD_INFO_FILE);
            if (Files.exists(worldInfoFile)) {
                try {
                    SavedWorldInfo existingInfo = loadWorldInfo(worldInfoFile);
                    if (existingInfo != null) {
                        worldInfo = new SavedWorldInfo(
                            worldName,
                            world.getSeed(),
                            existingInfo.getCreationDate(),
                            LocalDateTime.now(),
                            worldDir.getFileName().toString()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load existing world info, using new creation date: " + e.getMessage());
                }
            }
            
            saveWorldInfo(worldInfo, worldInfoFile);
            
            // Save player data
            savePlayerData(player, worldDir.resolve(PLAYER_DATA_FILE));
            
            // Save chunks
            saveChunks(world, worldDir.resolve(CHUNKS_DIRECTORY));
            
            System.out.println("World '" + worldName + "' saved successfully to " + worldDir);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to save world: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads a world from disk.
     * 
     * @param worldInfo The world info containing save location
     * @return A WorldSaveData object containing the loaded world and player, or null if loading failed
     */
    public WorldSaveData loadWorld(SavedWorldInfo worldInfo) {
        try {
            Path worldDir = savesPath.resolve(worldInfo.getFileName());
            
            if (!Files.exists(worldDir)) {
                System.err.println("World directory not found: " + worldDir);
                return null;
            }
            
            // Load world (create new world with same name and seed)
            World world = new World(worldInfo.getName(), worldInfo.getSeed());
            
            // Load player data
            Path playerFile = worldDir.resolve(PLAYER_DATA_FILE);
            Player player = null;
            if (Files.exists(playerFile)) {
                player = loadPlayerData(playerFile, world);
            }
            
            // Load chunks
            Path chunksDir = worldDir.resolve(CHUNKS_DIRECTORY);
            if (Files.exists(chunksDir)) {
                loadChunks(world, chunksDir);
            }
            
            System.out.println("World '" + worldInfo.getName() + "' loaded successfully");
            return new WorldSaveData(world, player, worldInfo.withUpdatedLastPlayed());
            
        } catch (Exception e) {
            System.err.println("Failed to load world: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Checks if a world with the given name already exists.
     * 
     * @param worldName The name of the world to check
     * @return true if a world with this name exists, false otherwise
     */
    public boolean worldExists(String worldName) {
        List<SavedWorldInfo> savedWorlds = getSavedWorlds();
        return savedWorlds.stream().anyMatch(world -> world.getName().equals(worldName));
    }
    
    /**
     * Deletes a world from disk.
     * 
     * @param worldName The name of the world to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteWorld(String worldName) {
        try {
            Path worldDir = savesPath.resolve(sanitizeFileName(worldName));
            
            if (Files.exists(worldDir)) {
                deleteDirectory(worldDir);
                System.out.println("World '" + worldName + "' deleted successfully");
                return true;
            } else {
                System.err.println("World directory not found: " + worldDir);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to delete world: " + e.getMessage());
            return false;
        }
    }
    
    private void saveWorldInfo(SavedWorldInfo worldInfo, Path file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(worldInfo);
        }
    }
    
    private SavedWorldInfo loadWorldInfo(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            return (SavedWorldInfo) ois.readObject();
        }
    }
    
    private void savePlayerData(Player player, Path file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(new PlayerSaveData(player));
        }
    }
    
    private Player loadPlayerData(Path file, World world) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            PlayerSaveData saveData = (PlayerSaveData) ois.readObject();
            return saveData.toPlayer(world);
        }
    }
    
    private void saveChunks(World world, Path chunksDir) throws IOException {
        if (!Files.exists(chunksDir)) {
            Files.createDirectories(chunksDir);
        }
        
        // Get all loaded chunks from the world
        Map<Vector4DInt, Chunk4D> chunks = world.getLoadedChunks();
        
        for (Map.Entry<Vector4DInt, Chunk4D> entry : chunks.entrySet()) {
            Vector4DInt chunkPos = entry.getKey();
            Chunk4D chunk = entry.getValue();
            
            // Only save dirty chunks to improve performance
            if (chunk.isDirty()) {
                String chunkFileName = String.format("chunk_%d_%d_%d_%d.dat", 
                    chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunkPos.getW());
                Path chunkFile = chunksDir.resolve(chunkFileName);
                
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(chunkFile))) {
                    oos.writeObject(new ChunkSaveData(chunk));
                    chunk.markClean(); // Mark as clean after saving
                }
            }
        }
    }
    
    private void loadChunks(World world, Path chunksDir) throws IOException {
        try {
            Files.list(chunksDir)
                .filter(path -> path.toString().endsWith(".dat") && path.getFileName().toString().startsWith("chunk_"))
                .forEach(chunkFile -> {
                    try {
                        String fileName = chunkFile.getFileName().toString();
                        // Parse chunk coordinates from filename
                        String[] parts = fileName.replace("chunk_", "").replace(".dat", "").split("_");
                        if (parts.length == 4) {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            int z = Integer.parseInt(parts[2]);
                            int w = Integer.parseInt(parts[3]);
                            
                            Vector4DInt chunkPos = new Vector4DInt(x, y, z, w);
                            
                            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(chunkFile))) {
                                 ChunkSaveData saveData = (ChunkSaveData) ois.readObject();
                                 Chunk4D chunk = saveData.toChunk();
                                 world.setChunk(chunkPos, chunk);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load chunk from " + chunkFile + ": " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            System.err.println("Failed to list chunk files: " + e.getMessage());
        }
    }
    
    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Container class for loaded world data.
     */
    public static class WorldSaveData {
        private final World world;
        private final Player player;
        private final SavedWorldInfo worldInfo;
        
        public WorldSaveData(World world, Player player, SavedWorldInfo worldInfo) {
            this.world = world;
            this.player = player;
            this.worldInfo = worldInfo;
        }
        
        public World getWorld() { return world; }
        public Player getPlayer() { return player; }
        public SavedWorldInfo getWorldInfo() { return worldInfo; }
    }
}