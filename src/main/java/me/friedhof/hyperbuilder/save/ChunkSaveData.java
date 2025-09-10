package me.friedhof.hyperbuilder.save;

import me.friedhof.hyperbuilder.computation.modules.*;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.SmelterItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.SmelterPoweredItem;
import me.friedhof.hyperbuilder.computation.modules.SmelterInventory;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
/**
 * Serializable data class for saving and loading Chunk4D data.
 * Contains all necessary information to reconstruct a Chunk4D object.
 */
public class ChunkSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Chunk position
    private final int posX, posY, posZ, posW;
    
    // Block data - stored using run-length encoding for better compression
    private final List<BlockRun> blockRuns;
    
    // Extra block data for blocks with state (like smelters)
    private final Map<String, BlockExtraData> blockExtraData;
    
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
        
        // Serialize blocks using run-length encoding
        this.blockRuns = new ArrayList<>();
        this.blockExtraData = new HashMap<>();
        Material currentMaterial = null;
        int runLength = 0;
        
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        Block block = chunk.getBlock(x, y, z, w);
                        Material blockId = (block != null) ? block.getBlockId() : Material.AIR;
                        
                        // Save extra data for blocks with state
                        if (block instanceof SmelterItem || block instanceof SmelterPoweredItem) {
                            String key = x + "," + y + "," + z + "," + w;
                            blockExtraData.put(key, new BlockExtraData(block));
                        }
                        
                        if (currentMaterial == null || !currentMaterial.equals(blockId)) {
                            // Start new run
                            if (currentMaterial != null) {
                                blockRuns.add(new BlockRun(currentMaterial, runLength));
                            }
                            currentMaterial = blockId;
                            runLength = 1;
                        } else {
                            // Continue current run
                            runLength++;
                        }
                    }
                }
            }
        }
        
        // Add the final run
        if (currentMaterial != null) {
            blockRuns.add(new BlockRun(currentMaterial, runLength));
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
        
        // Restore blocks from run-length encoding
        int blockIndex = 0;
        int runIndex = 0;
        int currentRunRemaining = blockRuns.isEmpty() ? 0 : blockRuns.get(0).getLength();
        Material currentMaterial = blockRuns.isEmpty() ? Material.AIR : blockRuns.get(0).getMaterial();
        
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        // Check if we need to move to the next run
                        if (currentRunRemaining <= 0 && runIndex + 1 < blockRuns.size()) {
                            runIndex++;
                            BlockRun nextRun = blockRuns.get(runIndex);
                            currentMaterial = nextRun.getMaterial();
                            currentRunRemaining = nextRun.getLength();
                        }
                        
                        Block block;
                        String key = x + "," + y + "," + z + "," + w;
                        
                        // Check if this block has extra data
                        if (blockExtraData.containsKey(key)) {
                            block = blockExtraData.get(key).toBlock();
                        } else {
                            block = ItemRegistry.createBlock(currentMaterial);
                        }
                        
                        chunk.setBlock(x, y, z, w, block);
                        currentRunRemaining--;
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
    
    /**
     * Represents a run of consecutive identical blocks for compression.
     */
    private static class BlockRun implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Material material;
        private final int length;
        
        public BlockRun(Material material, int length) {
            this.material = material;
            this.length = length;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public int getLength() {
            return length;
        }
    }
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
    
    // DroppedItem specific data
    private final Material itemType;
    private final int itemCount;
    private final double despawnTimer;
    
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
        
        // Handle DroppedItem specific data
        if (entity instanceof DroppedItem) {
            DroppedItem droppedItem = (DroppedItem) entity;
            this.itemType = droppedItem.getItem().getItemId();
            this.itemCount = droppedItem.getCount();
            this.despawnTimer = droppedItem.getDespawnTimer();
        } else {
            this.itemType = null;
            this.itemCount = 0;
            this.despawnTimer = 0.0;
        }
    }
    
    public Entity toEntity() {
        if ("DroppedItem".equals(entityType)) {
            // Reconstruct DroppedItem
            try {
                BaseItem item = ItemRegistry.createItem(itemType, itemCount);
                if (item != null) {
                    DroppedItem droppedItem = new DroppedItem(id, new Vector4D(posX, posY, posZ, posW), item, itemCount);
                    droppedItem.setVelocity(new Vector4D(velX, velY, velZ, velW));
                    droppedItem.setDespawnTimer(despawnTimer);
                    return droppedItem;
                }
            } catch (Exception e) {
                System.err.println("Failed to reconstruct DroppedItem: " + e.getMessage());
            }
        }
        
        // For unsupported entity types
        System.out.println("Warning: Loading generic entity of type " + entityType + ". Specific entity data may be lost.");
        return null;
    }
    
    // Getters for debugging/inspection
    public int getId() { return id; }
    public Vector4D getPosition() { return new Vector4D(posX, posY, posZ, posW); }
    public Vector4D getVelocity() { return new Vector4D(velX, velY, velZ, velW); }
    public String getEntityType() { return entityType; }
}

/**
 * Serializable data class for blocks with extra state (like smelters).
 */
class BlockExtraData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Material blockType;
    private final boolean isProcessing;
    private final long processingStartTime;
    private final long powerExpireTime; // For powered smelters
    private final Material inputItemType;
    private final int inputItemCount;
    private final Material outputItemType;
    private final int outputItemCount;
    
    public BlockExtraData(Block block) {
        this.blockType = block.getBlockId();
        
        if (block instanceof SmelterItem) {
            SmelterItem smelter = (SmelterItem) block;
            this.isProcessing = smelter.isProcessing();
            this.processingStartTime = smelter.getProcessingStartTime();
            this.powerExpireTime = 0; // Regular smelters don't have power
            
            SmelterInventory inventory = smelter.getInventory();
            BaseItem inputItem = inventory.getInputItem();
            BaseItem outputItem = inventory.getOutputItem();
            
            this.inputItemType = (inputItem != null) ? inputItem.getItemId() : null;
            this.inputItemCount = (inputItem != null) ? inputItem.getCount() : 0;
            this.outputItemType = (outputItem != null) ? outputItem.getItemId() : null;
            this.outputItemCount = (outputItem != null) ? outputItem.getCount() : 0;
        } else if (block instanceof SmelterPoweredItem) {
            SmelterPoweredItem poweredSmelter = (SmelterPoweredItem) block;
            this.isProcessing = poweredSmelter.isProcessing();
            this.processingStartTime = poweredSmelter.getProcessingStartTime();
            this.powerExpireTime = poweredSmelter.getPowerExpireTime();
            
            SmelterInventory inventory = poweredSmelter.getInventory();
            BaseItem inputItem = inventory.getInputItem();
            BaseItem outputItem = inventory.getOutputItem();
            
            this.inputItemType = (inputItem != null) ? inputItem.getItemId() : null;
            this.inputItemCount = (inputItem != null) ? inputItem.getCount() : 0;
            this.outputItemType = (outputItem != null) ? outputItem.getItemId() : null;
            this.outputItemCount = (outputItem != null) ? outputItem.getCount() : 0;
        } else {
            // Default values for non-smelter blocks
            this.isProcessing = false;
            this.processingStartTime = 0;
            this.powerExpireTime = 0;
            this.inputItemType = null;
            this.inputItemCount = 0;
            this.outputItemType = null;
            this.outputItemCount = 0;
        }
    }
    
    public Block toBlock() {
        if (blockType == Material.SMELTER) {
            // Create regular smelter with saved state
            SmelterInventory inventory = new SmelterInventory();
            if (inputItemType != null && inputItemCount > 0) {
                BaseItem inputItem = ItemRegistry.createItem(inputItemType, inputItemCount);
                inventory.setInputItem(inputItem);
            }
            if (outputItemType != null && outputItemCount > 0) {
                BaseItem outputItem = ItemRegistry.createItem(outputItemType, outputItemCount);
                inventory.setOutputItem(outputItem);
            }
            
            SmelterItem smelter = new SmelterItem(1, inventory);
            if (isProcessing) {
                smelter.setProcessing(true);
                smelter.setProcessingStartTime(processingStartTime);
            }
            return smelter;
        } else if (blockType == Material.SMELTER_POWERED) {
            // Create powered smelter with saved state
            SmelterInventory inventory = new SmelterInventory();
            if (inputItemType != null && inputItemCount > 0) {
                BaseItem inputItem = ItemRegistry.createItem(inputItemType, inputItemCount);
                inventory.setInputItem(inputItem);
            }
            if (outputItemType != null && outputItemCount > 0) {
                BaseItem outputItem = ItemRegistry.createItem(outputItemType, outputItemCount);
                inventory.setOutputItem(outputItem);
            }
            
            SmelterPoweredItem poweredSmelter = new SmelterPoweredItem(1, inventory);
            if (isProcessing) {
                poweredSmelter.setProcessing(true);
                poweredSmelter.setProcessingStartTime(processingStartTime);
            }
            poweredSmelter.setPowerExpireTime(powerExpireTime);
            return poweredSmelter;
        } else {
            // Fallback to regular block creation
            return ItemRegistry.createBlock(blockType);
        }
    }
}