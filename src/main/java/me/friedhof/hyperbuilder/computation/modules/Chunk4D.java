package me.friedhof.hyperbuilder.computation.modules;

import java.util.HashMap;
import java.util.Map;

import me.friedhof.hyperbuilder.computation.modules.items.Block;

/**
 * Represents a 4D chunk in the voxel world.
 * A chunk is a 16x16x16x16 section of blocks that is loaded and unloaded as a unit.
 */
public class Chunk4D {
    // Chunk size constants
    public static final int CHUNK_SIZE = 8;
    public static final int CHUNK_VOLUME = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE;
    
    // Chunk position in the world (in chunk coordinates)
    private final Vector4DInt position;
    
    // Blocks stored in this chunk
    private final Block[][][][] blocks;
    
    // Map of entities in this chunk
    private final Map<Integer, Entity> entities;
    
    // Dirty flag to track if the chunk needs to be saved
    private boolean dirty;
    
    /**
     * Creates a new chunk at the specified position.
     * 
     * @param position The position of this chunk in chunk coordinates
     */
    public Chunk4D(Vector4DInt position) {
        this.position = position;
        this.blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        this.entities = new HashMap<>();
        this.dirty = false;
        
        // Initialize all blocks as air
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    for (int w = 0; w < CHUNK_SIZE; w++) {
                        blocks[x][y][z][w] = new Block(Material.AIR);
                    }
                }
            }
        }
    }
    
    /**
     * Gets the position of this chunk in chunk coordinates.
     * 
     * @return The chunk position
     */
    public Vector4DInt getPosition() {
        return position;
    }
    
    /**
     * Gets the block at the specified local coordinates within this chunk.
     * 
     * @param x Local X coordinate (0-15)
     * @param y Local Y coordinate (0-15)
     * @param z Local Z coordinate (0-15)
     * @param w Local W coordinate (0-15)
     * @return The block at the specified position
     */
    public Block getBlock(int x, int y, int z, int w) {
        if (isValidLocalCoordinate(x) && isValidLocalCoordinate(y) && 
            isValidLocalCoordinate(z) && isValidLocalCoordinate(w)) {
            return blocks[x][y][z][w];
        }
        return null;
    }
    
    /**
     * Gets the block at the specified local position within this chunk.
     * 
     * @param localPos Local position within the chunk
     * @return The block at the specified position
     */
    public Block getBlock(Vector4DInt localPos) {
        return getBlock(localPos.getX(), localPos.getY(), localPos.getZ(), localPos.getW());
    }
    
    /**
     * Sets the block at the specified local coordinates within this chunk.
     * 
     * @param x Local X coordinate (0-15)
     * @param y Local Y coordinate (0-15)
     * @param z Local Z coordinate (0-15)
     * @param w Local W coordinate (0-15)
     * @param block The block to set
     * @return true if the block was set, false if the coordinates are invalid
     */
    public boolean setBlock(int x, int y, int z, int w, Block block) {
        if (isValidLocalCoordinate(x) && isValidLocalCoordinate(y) && 
            isValidLocalCoordinate(z) && isValidLocalCoordinate(w)) {
            blocks[x][y][z][w] = block;
            dirty = true;
            return true;
        }
        return false;
    }
    
    /**
     * Sets the block at the specified local position within this chunk.
     * 
     * @param localPos Local position within the chunk
     * @param block The block to set
     * @return true if the block was set, false if the position is invalid
     */
    public boolean setBlock(Vector4DInt localPos, Block block) {
        return setBlock(localPos.getX(), localPos.getY(), localPos.getZ(), localPos.getW(), block);
    }
    
    /**
     * Adds an entity to this chunk.
     * 
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        entities.put(entity.getId(), entity);
        dirty = true;
    }
    
    /**
     * Removes an entity from this chunk.
     * 
     * @param entityId The ID of the entity to remove
     * @return The removed entity, or null if not found
     */
    public Entity removeEntity(int entityId) {
        Entity removed = entities.remove(entityId);
        if (removed != null) {
            dirty = true;
        }
        return removed;
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
     * Gets all entities in this chunk.
     * 
     * @return A map of entity IDs to entities
     */
    public Map<Integer, Entity> getEntities() {
        return new HashMap<>(entities);
    }
    
    /**
     * Checks if this chunk has been modified since it was last saved.
     * 
     * @return true if the chunk is dirty, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Marks this chunk as clean (saved).
     */
    public void markClean() {
        dirty = false;
    }
    
    /**
     * Converts a world position to a local chunk position.
     * 
     * @param worldPos The position in world coordinates
     * @return The position in local chunk coordinates
     */
    public Vector4DInt worldToLocalPos(Vector4DInt worldPos) {
        Vector4DInt chunkOrigin = position.scale(CHUNK_SIZE);
        return new Vector4DInt(
            worldPos.getX() - chunkOrigin.getX(),
            worldPos.getY() - chunkOrigin.getY(),
            worldPos.getZ() - chunkOrigin.getZ(),
            worldPos.getW() - chunkOrigin.getW()
        );
    }
    
    /**
     * Converts a local chunk position to a world position.
     * 
     * @param localPos The position in local chunk coordinates
     * @return The position in world coordinates
     */
    public Vector4DInt localToWorldPos(Vector4DInt localPos) {
        Vector4DInt chunkOrigin = position.scale(CHUNK_SIZE);
        return new Vector4DInt(
            localPos.getX() + chunkOrigin.getX(),
            localPos.getY() + chunkOrigin.getY(),
            localPos.getZ() + chunkOrigin.getZ(),
            localPos.getW() + chunkOrigin.getW()
        );
    }
    
    /**
     * Checks if a coordinate is valid within the chunk (0-15).
     * 
     * @param coordinate The coordinate to check
     * @return true if the coordinate is valid, false otherwise
     */
    private boolean isValidLocalCoordinate(int coordinate) {
        return coordinate >= 0 && coordinate < CHUNK_SIZE;
    }
}