package me.friedhof.hyperbuilder.save;

import me.friedhof.hyperbuilder.computation.modules.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import me.friedhof.hyperbuilder.computation.modules.items.Block;
/**
 * Serializable data class for saving and loading Chunk4D data.
 * Contains all necessary information to reconstruct a Chunk4D object.
 */
public class ChunkSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Chunk position
    private final int posX, posY, posZ, posW;
    
    // Block data - stored as a flattened array for efficiency
    private final Material[] blockIds;
    
    // Entity data
    private final Map<Integer, EntitySaveData> entities;
    
    // Dirty flag
    private final boolean dirty;
    
    /**
     * Creates ChunkSaveData from a Chunk4D object.
     * 
     * @param chunk The chunk to serialize
     */
    public ChunkSaveData(Chunk4D chunk) {
        Vector4DInt pos = chunk.getPosition();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.posW = pos.getW();
        
        // Serialize blocks
        this.blockIds = new Material[Chunk4D.CHUNK_VOLUME];
        int index = 0;
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        Block block = chunk.getBlock(x, y, z, w);
                        blockIds[index++] = (block != null) ? block.getBlockId() : Material.AIR;
                    }
                }
            }
        }
        
        // Serialize entities (excluding players, they're saved separately)
        this.entities = new HashMap<>();
        for (Map.Entry<Integer, Entity> entry : chunk.getEntities().entrySet()) {
            Entity entity = entry.getValue();
            if (!(entity instanceof Player)) {
                this.entities.put(entry.getKey(), new EntitySaveData(entity));
            }
        }
        
        this.dirty = chunk.isDirty();
    }
    
    /**
     * Reconstructs a Chunk4D object from this save data.
     * 
     * @return The reconstructed Chunk4D object
     */
    public Chunk4D toChunk() {
        Vector4DInt position = new Vector4DInt(posX, posY, posZ, posW);
        Chunk4D chunk = new Chunk4D(position);
        
        // Restore blocks
        int index = 0;
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        Material blockId = blockIds[index++];
                        Block block = new Block(blockId);
                        chunk.setBlock(x, y, z, w, block);
                    }
                }
            }
        }
        
        // Restore entities (non-player entities)
        for (Map.Entry<Integer, EntitySaveData> entry : entities.entrySet()) {
            Entity entity = entry.getValue().toEntity();
            if (entity != null) {
                chunk.addEntity(entity);
            }
        }
        
        // Restore dirty state
        if (!dirty) {
            chunk.markClean();
        }
        
        return chunk;
    }
    
    // Getters for debugging/inspection
    public Vector4DInt getPosition() { return new Vector4DInt(posX, posY, posZ, posW); }
    public Map<Integer, EntitySaveData> getEntities() { return new HashMap<>(entities); }
    public boolean isDirty() { return dirty; }
}

/**
 * Serializable data class for Entity objects (excluding Players).
 * Currently supports basic entity data - can be extended for specific entity types.
 */
class EntitySaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int id;
    private final double posX, posY, posZ, posW;
    private final double velX, velY, velZ, velW;
    private final double sizeX, sizeY, sizeZ, sizeW;
    private final boolean gravity;
    private final String entityType;
    
    public EntitySaveData(Entity entity) {
        this.id = entity.getId();
        
        Vector4D pos = entity.getPosition();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.posW = pos.getW();
        
        Vector4D vel = entity.getVelocity();
        this.velX = vel.getX();
        this.velY = vel.getY();
        this.velZ = vel.getZ();
        this.velW = vel.getW();
        
        this.sizeX = entity.getSizeX();
        this.sizeY = entity.getSizeY();
        this.sizeZ = entity.getSizeZ();
        this.sizeW = entity.getSizeW();
        
        this.gravity = entity.hasGravity();
        this.entityType = entity.getClass().getSimpleName();
    }
    
    public Entity toEntity() {
        // For now, we only support basic entities
        // This can be extended to support specific entity types as needed
        System.out.println("Warning: Loading generic entity of type " + entityType + ". Specific entity data may be lost.");
        return null; // Return null for now - specific entity types would need their own implementations
    }
    
    // Getters for debugging/inspection
    public int getId() { return id; }
    public Vector4D getPosition() { return new Vector4D(posX, posY, posZ, posW); }
    public Vector4D getVelocity() { return new Vector4D(velX, velY, velZ, velW); }
    public String getEntityType() { return entityType; }
}