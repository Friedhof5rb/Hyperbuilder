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
        
        // TODO: Use a proper world generator
        // For now, just create a simple flat world
        
        // If y < 0, fill with stone
        // If y = 0, fill with grass
        // If y > 0, fill with air
        
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        // Convert local coordinates to world coordinates
                        int worldY = y + position.getY() * Chunk4D.CHUNK_SIZE;
                        
                        byte blockType;
                        if (worldY < 0) {
                            blockType = Block.TYPE_STONE;
                        } else if (worldY == 0) {
                            blockType = Block.TYPE_GRASS;
                        } else {
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