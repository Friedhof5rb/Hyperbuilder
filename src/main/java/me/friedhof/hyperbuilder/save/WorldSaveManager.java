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
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
            
            // Clear lazy loader cache after saving to ensure consistency
            if (world.getChunkLoader() != null) {
                world.getChunkLoader().clearCache();
            }
            
            System.out.println("World '" + worldName + "' saved successfully to " + worldDir);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to save world: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Interface for receiving progress updates during world loading.
     */
    public interface LoadProgressCallback {
        void onProgress(String message, int current, int total);
    }
    
    /**
     * Loads a world from disk with lazy chunk loading.
     * Only loads essential data initially, chunks are loaded on-demand.
     * 
     * @param worldInfo The world info containing save location
     * @return A WorldSaveData object containing the loaded world and player, or null if loading failed
     */
    public WorldSaveData loadWorld(SavedWorldInfo worldInfo) {
        return loadWorld(worldInfo, null);
    }
    
    /**
     * Loads a world from disk with lazy chunk loading and progress reporting.
     * Only loads essential data initially, chunks are loaded on-demand.
     * 
     * @param worldInfo The world info containing save location
     * @param progressCallback Callback for progress updates (can be null)
     * @return A WorldSaveData object containing the loaded world and player, or null if loading failed
     */
    public WorldSaveData loadWorld(SavedWorldInfo worldInfo, LoadProgressCallback progressCallback) {
        try {
            if (progressCallback != null) {
                progressCallback.onProgress("Initializing world loading...", 0, 5);
            }
            
            Path worldDir = savesPath.resolve(worldInfo.getFileName());
            
            if (!Files.exists(worldDir)) {
                System.err.println("World directory not found: " + worldDir);
                return null;
            }
            
            if (progressCallback != null) {
                progressCallback.onProgress("Creating world instance...", 1, 5);
            }
            
            // Load world (create new world with same name and seed)
            World world = new World(worldInfo.getName(), worldInfo.getSeed());
            
            if (progressCallback != null) {
                progressCallback.onProgress("Setting up chunk loader...", 2, 5);
            }
            
            // Set up lazy chunk loading for this world
            Path chunksDir = worldDir.resolve(CHUNKS_DIRECTORY);
            if (Files.exists(chunksDir)) {
                world.setChunkLoader(new LazyChunkLoader(chunksDir));
            }
            
            if (progressCallback != null) {
                progressCallback.onProgress("Loading player data...", 3, 5);
            }
            
            // Load player data
            Path playerFile = worldDir.resolve(PLAYER_DATA_FILE);
            Player player = null;
            if (Files.exists(playerFile)) {
                player = loadPlayerData(playerFile, world);
                
                if (progressCallback != null) {
                    progressCallback.onProgress("Loading initial chunks...", 4, 5);
                }
                
                // Load chunks around player spawn position for immediate gameplay
                if (player != null) {
                    loadChunksAroundPlayer(world, player, chunksDir, 2); // Load 2 chunk radius
                }
            }
            
            if (progressCallback != null) {
                progressCallback.onProgress("World loading complete!", 5, 5);
            }
            
            System.out.println("World '" + worldInfo.getName() + "' loaded successfully (lazy loading enabled)");
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
        
        // Sync entities from world to chunks before saving
        world.syncEntitiesToChunks();
        
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
                
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new GZIPOutputStream(Files.newOutputStream(chunkFile)))) {
                    oos.writeObject(new ChunkSaveData(chunk));
                    chunk.markClean(); // Mark as clean after saving
                }
            }
        }
    }
    
    /**
     * Loads all chunks from disk (legacy method for compatibility).
     * Consider using lazy loading for better performance.
     */
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
                            
                            try (ObjectInputStream ois = new ObjectInputStream(
                                    new GZIPInputStream(Files.newInputStream(chunkFile)))) {
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
    
    /**
     * Loads chunks around a player's position for immediate gameplay using parallel processing.
     * 
     * @param world The world to load chunks into
     * @param player The player whose position to center the loading around
     * @param chunksDir The directory containing chunk files
     * @param radius The radius in chunks to load around the player
     */
    private void loadChunksAroundPlayer(World world, Player player, Path chunksDir, int radius) {
        if (player == null || !Files.exists(chunksDir)) {
            return;
        }
        
        // Get player's chunk position
        Vector4DInt playerPos = new Vector4DInt(
            (int) Math.floor(player.getPosition().getX()),
            (int) Math.floor(player.getPosition().getY()),
            (int) Math.floor(player.getPosition().getZ()),
            (int) Math.floor(player.getPosition().getW())
        );
        
        Vector4DInt playerChunkPos = new Vector4DInt(
            Math.floorDiv(playerPos.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(playerPos.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(playerPos.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(playerPos.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Collect all chunk positions to load
        List<Vector4DInt> chunksToLoad = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int w = -radius; w <= radius; w++) {
                        chunksToLoad.add(new Vector4DInt(
                            playerChunkPos.getX() + x,
                            playerChunkPos.getY() + y,
                            playerChunkPos.getZ() + z,
                            playerChunkPos.getW() + w
                        ));
                    }
                }
            }
        }
        
        // Load chunks in parallel using a thread pool
        int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), chunksToLoad.size());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        try {
            List<Future<Void>> futures = new ArrayList<>();
            
            for (Vector4DInt chunkPos : chunksToLoad) {
                Future<Void> future = executor.submit(() -> {
                    loadSingleChunk(world, chunkPos, chunksDir);
                    return null;
                });
                futures.add(future);
            }
            
            // Wait for all chunks to load
            for (Future<Void> future : futures) {
                try {
                    future.get(5, TimeUnit.SECONDS); // 5 second timeout per chunk
                } catch (TimeoutException e) {
                    System.err.println("Chunk loading timed out");
                    future.cancel(true);
                } catch (ExecutionException e) {
                    System.err.println("Error loading chunk: " + e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Chunk loading interrupted");
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Loaded " + chunksToLoad.size() + " chunks around player using " + numThreads + " threads");
    }
    
    /**
     * Loads a single chunk from disk if it exists.
     * 
     * @param world The world to load the chunk into
     * @param chunkPos The position of the chunk to load
     * @param chunksDir The directory containing chunk files
     */
    private void loadSingleChunk(World world, Vector4DInt chunkPos, Path chunksDir) {
        String chunkFileName = String.format("chunk_%d_%d_%d_%d.dat", 
            chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunkPos.getW());
        Path chunkFile = chunksDir.resolve(chunkFileName);
        
        if (Files.exists(chunkFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream(Files.newInputStream(chunkFile)))) {
            ChunkSaveData saveData = (ChunkSaveData) ois.readObject();
            Chunk4D chunk = saveData.toChunk();
            world.setChunk(chunkPos, chunk);
        } catch (Exception e) {
                System.err.println("Failed to load chunk from " + chunkFile + ": " + e.getMessage());
            }
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