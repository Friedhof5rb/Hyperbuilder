package com.adventure4d.computation.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the 4D world containing chunks and entities.
 * Manages chunk loading/unloading and entity tracking.
 */
public class World {
    // The name of this world
    private final String name;
    
    // The seed used for world generation
    private final long seed;
    
    // Map of loaded chunks (chunk position -> chunk)
    private final Map<Vector4DInt, Chunk4D> chunks;
    
    // Map of entities (entity ID -> entity)
    private final Map<Integer, Entity> entities;
    
    // The next available entity ID
    private int nextEntityId;
    
    /**
     * Creates a new world with the specified name and seed.
     * 
     * @param name The world name
     * @param seed The world generation seed
     */
    public World(String name, long seed) {
        this.name = name;
        this.seed = seed;
        this.chunks = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();
        this.nextEntityId = 1;
    }
    
    /**
     * Gets the name of this world.
     * 
     * @return The world name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the seed used for world generation.
     * 
     * @return The world seed
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * Gets the chunk at the specified position.
     * If the chunk is not loaded, it will be generated.
     * 
     * @param position The chunk position
     * @return The chunk at the specified position
     */
    public Chunk4D getChunk(Vector4DInt position) {
        // Check if the chunk is already loaded
        Chunk4D chunk = chunks.get(position);
        
        // If not, generate it
        if (chunk == null) {
            chunk = generateChunk(position);
            chunks.put(position, chunk);
        }
        
        return chunk;
    }
    
    /**
     * Generates a new chunk at the specified position.
     * 
     * @param position The chunk position
     * @return The generated chunk
     */
    private Chunk4D generateChunk(Vector4DInt position) {
        // Create a new chunk
        Chunk4D chunk = new Chunk4D(position);
        
        // Generate realistic terrain with hills and valleys
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        // Convert local coordinates to world coordinates
                        int worldX = x + position.getX() * Chunk4D.CHUNK_SIZE;
                        int worldY = y + position.getY() * Chunk4D.CHUNK_SIZE;
                        int worldZ = z + position.getZ() * Chunk4D.CHUNK_SIZE;
                        int worldW = w + position.getW() * Chunk4D.CHUNK_SIZE;
                        
                        // Generate terrain height using multiple octaves of noise
                        double terrainHeight = generateTerrainHeight(worldX, worldZ, worldW);
                        
                        byte blockType;
                        if (worldY < terrainHeight - 3) {
                            // Deep underground: stone
                            blockType = Block.TYPE_STONE;
                        } else if (worldY < terrainHeight - 1) {
                            // Shallow underground: dirt
                            blockType = Block.TYPE_DIRT;
                        } else if (worldY <= terrainHeight) {
                            // Surface: grass
                            blockType = Block.TYPE_GRASS;
                        } else {
                            // Above surface: air
                            blockType = Block.TYPE_AIR;
                        }
                        
                        chunk.setBlock(x, y, z, w, new Block(blockType));
                    }
                }
            }
        }
        
        return chunk;
    }
    
    /**
     * Generates terrain height at the given world coordinates using realistic Perlin noise.
     * 
     * @param x World X coordinate
     * @param z World Z coordinate  
     * @param w World W coordinate
     * @return The terrain height at this position
     */
    private double generateTerrainHeight(int x, int z, int w) {
        // Base height around 0
        double height = 0.0;
        
        // Continental scale features (large landmasses)
        height += 20.0 * perlinNoise(x * 0.003, z * 0.003, w * 0.003, seed);
        
        // Mountain ranges and large valleys
        height += 15.0 * perlinNoise(x * 0.008, z * 0.008, w * 0.008, seed + 1000);
        
        // Hills and medium valleys
        height += 8.0 * perlinNoise(x * 0.02, z * 0.02, w * 0.02, seed + 2000);
        
        // Rolling terrain
        height += 4.0 * perlinNoise(x * 0.05, z * 0.05, w * 0.05, seed + 3000);
        
        // Surface detail and small features
        height += 2.0 * perlinNoise(x * 0.12, z * 0.12, w * 0.12, seed + 4000);
        
        // Fine surface texture
        height += 1.0 * perlinNoise(x * 0.3, z * 0.3, w * 0.3, seed + 5000);
        
        return height;
    }
    
    /**
     * 4D Perlin noise implementation for realistic terrain generation.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param seed Random seed
     * @return Noise value between -1 and 1
     */
    private double perlinNoise(double x, double y, double z, long seed) {
        // Get integer coordinates
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        int zi = (int) Math.floor(z);
        
        // Get fractional coordinates
        double xf = x - xi;
        double yf = y - yi;
        double zf = z - zi;
        
        // Smooth the fractional coordinates using fade function
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);
        
        // Hash coordinates of the 8 cube corners
        int aaa = hash(xi, yi, zi, seed);
        int aba = hash(xi, yi + 1, zi, seed);
        int aab = hash(xi, yi, zi + 1, seed);
        int abb = hash(xi, yi + 1, zi + 1, seed);
        int baa = hash(xi + 1, yi, zi, seed);
        int bba = hash(xi + 1, yi + 1, zi, seed);
        int bab = hash(xi + 1, yi, zi + 1, seed);
        int bbb = hash(xi + 1, yi + 1, zi + 1, seed);
        
        // Calculate gradients at each corner
        double x1 = lerp(grad(aaa, xf, yf, zf), grad(baa, xf - 1, yf, zf), u);
        double x2 = lerp(grad(aba, xf, yf - 1, zf), grad(bba, xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);
        
        x1 = lerp(grad(aab, xf, yf, zf - 1), grad(bab, xf - 1, yf, zf - 1), u);
        x2 = lerp(grad(abb, xf, yf - 1, zf - 1), grad(bbb, xf - 1, yf - 1, zf - 1), u);
        double y2 = lerp(x1, x2, v);
        
        return lerp(y1, y2, w);
    }
    
    /**
     * Fade function for smooth interpolation (6t^5 - 15t^4 + 10t^3).
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    /**
     * Linear interpolation between two values.
     */
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    
    /**
     * Hash function for generating pseudo-random values.
     */
    private int hash(int x, int y, int z, long seed) {
        long hash = seed;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return (int) (hash & 255);
    }
    
    /**
     * Gradient function for Perlin noise.
     */
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    /**
     * Unloads the chunk at the specified position.
     * 
     * @param position The chunk position
     * @return true if the chunk was unloaded, false if it wasn't loaded
     */
    public boolean unloadChunk(Vector4DInt position) {
        return chunks.remove(position) != null;
    }
    
    /**
     * Gets the block at the specified position.
     * 
     * @param position The block position
     * @return The block at the specified position, or null if the chunk is not loaded
     */
    public Block getBlock(Vector4DInt position) {
        // Calculate the chunk position
        Vector4DInt chunkPos = new Vector4DInt(
            Math.floorDiv(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Calculate the local position within the chunk
        Vector4DInt localPos = new Vector4DInt(
            Math.floorMod(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Get the chunk
        Chunk4D chunk = getChunk(chunkPos);
        
        // Get the block
        return chunk.getBlock(localPos);
    }
    
    /**
     * Sets the block at the specified position.
     * 
     * @param position The block position
     * @param block The block to set
     * @return true if the block was set, false otherwise
     */
    public boolean setBlock(Vector4DInt position, Block block) {
        // Calculate the chunk position
        Vector4DInt chunkPos = new Vector4DInt(
            Math.floorDiv(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Calculate the local position within the chunk
        Vector4DInt localPos = new Vector4DInt(
            Math.floorMod(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Get the chunk
        Chunk4D chunk = getChunk(chunkPos);
        
        // Set the block
        return chunk.setBlock(localPos, block);
    }
    
    /**
     * Adds an entity to the world.
     * 
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        entities.put(entity.getId(), entity);
    }
    
    /**
     * Removes an entity from the world.
     * 
     * @param entityId The ID of the entity to remove
     * @return The removed entity, or null if not found
     */
    public Entity removeEntity(int entityId) {
        return entities.remove(entityId);
    }
    
    /**
     * Gets an entity by its ID.
     * 
     * @param entityId The ID of the entity to get
     * @return The entity, or null if not found
     */
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    /**
     * Gets all entities in the world.
     * 
     * @return A map of entity IDs to entities
     */
    public Map<Integer, Entity> getEntities() {
        return new HashMap<>(entities);
    }
    
    /**
     * Creates a new player in the world.
     * 
     * @param username The player's username
     * @param position The initial position of the player
     * @return The created player
     */
    public Player createPlayer(String username, Vector4D position) {
        int id = nextEntityId++;
        Player player = new Player(id, position, username);
        addEntity(player);
        return player;
    }
    
    /**
     * Updates all entities in the world.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    public void update(double deltaTime) {
        // Update all entities
        for (Entity entity : entities.values()) {
            if(!(entity instanceof Player)){
                entity.update(deltaTime, this);
            }
        }
    }
}