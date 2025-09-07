package me.friedhof.hyperbuilder.computation.modules;

import java.rmi.registry.Registry;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasTexture;
import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
/**
 * Represents a block in the world that can be placed and interacted with.
 * Blocks are the building components of the world structure.
 */
public class Block implements HasTexture, HasCollision {
    private final String blockId;
    
    /**
     * Creates a new block with the specified properties.
     * 
     * @param blockId The unique identifier for this block type
     */
    public Block(String blockId) {
        this.blockId = blockId;
    
    }
    
    @Override
    public boolean HasTexture() {
        return true;
    }


    /**
     * Gets the unique identifier for this block type.
     * 
     * @return The block ID
     */
    public String getBlockId() {
        return blockId;
    }
    
    @Override
    public String getBlockTextureName() {
        return blockId;
    }
    

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Block block = (Block) obj;
        return blockId.equals(block.blockId);
    }
    
    @Override
    public int hashCode() {
        return blockId.hashCode();
    }
    
    @Override
    public String toString() {
        return "Block{" +
                "blockId='" + blockId + '\'' +
                '}';
    }

    @Override
    public boolean isSolid() {
        return ItemRegistry.itemFactories.get(blockId).isSolid();
    }

}