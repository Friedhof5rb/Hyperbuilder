package me.friedhof.hyperbuilder.computation.modules;

/**
 * Represents a single block in the 4D voxel world.
 * Each block has a type and can store additional metadata.
 */
public class Block {
    // Block type constants
    public static final byte TYPE_AIR = 0;
    public static final byte TYPE_DIRT = 1;
    public static final byte TYPE_GRASS = 2;
    public static final byte TYPE_STONE = 3;
    public static final byte TYPE_WOOD = 4;
    public static final byte TYPE_LEAVES = 5;
    
    private byte type;
    private byte metadata;
    
    /**
     * Creates a new block with the specified type and default metadata.
     * 
     * @param type The block type
     */
    public Block(byte type) {
        this.type = type;
        this.metadata = 0;
    }
    
    /**
     * Creates a new block with the specified type and metadata.
     * 
     * @param type The block type
     * @param metadata Additional data about the block
     */
    public Block(byte type, byte metadata) {
        this.type = type;
        this.metadata = metadata;
    }
    
    /**
     * Creates an air block.
     */
    public Block() {
        this(TYPE_AIR);
    }
    
    /**
     * Gets the block type.
     * 
     * @return The block type
     */
    public byte getType() {
        return type;
    }
    
    /**
     * Sets the block type.
     * 
     * @param type The new block type
     */
    public void setType(byte type) {
        this.type = type;
    }
    
    /**
     * Gets the block metadata.
     * 
     * @return The block metadata
     */
    public byte getMetadata() {
        return metadata;
    }
    
    /**
     * Sets the block metadata.
     * 
     * @param metadata The new block metadata
     */
    public void setMetadata(byte metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Checks if this block is air (empty space).
     * 
     * @return true if the block is air, false otherwise
     */
    public boolean isAir() {
        return type == TYPE_AIR;
    }
    
    /**
     * Checks if this block is solid (can be collided with).
     * 
     * @return true if the block is solid, false otherwise
     */
    public boolean isSolid() {
        return type != TYPE_AIR;
    }
    
    /**
     * Creates a copy of this block.
     * 
     * @return A new Block with the same type and metadata
     */
    public Block copy() {
        return new Block(type, metadata);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Block other = (Block) obj;
        return type == other.type && metadata == other.metadata;
    }
    
    @Override
    public int hashCode() {
        return (type << 8) | metadata;
    }
    
    @Override
    public String toString() {
        String typeName;
        switch (type) {
            case TYPE_AIR: typeName = "AIR"; break;
            case TYPE_DIRT: typeName = "DIRT"; break;
            case TYPE_GRASS: typeName = "GRASS"; break;
            case TYPE_STONE: typeName = "STONE"; break;
            case TYPE_WOOD: typeName = "WOOD"; break;
            case TYPE_LEAVES: typeName = "LEAVES"; break;
            default: typeName = "UNKNOWN"; break;
        }
        
        return String.format("Block(%s, metadata=%d)", typeName, metadata);
    }
}