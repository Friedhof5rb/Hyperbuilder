package me.friedhof.hyperbuilder.computation.modules;

/**
 * Represents an item in the game.
 * Items can be blocks, tools, or other objects that can be stored in inventories.
 */
public class Item {
    private final byte type;
    private final int count;
    private final byte metadata;
    
    /**
     * Creates a new item with the specified type and count.
     * 
     * @param type The item type
     * @param count The number of items in this stack
     */
    public Item(byte type, int count) {
        this(type, count, (byte) 0);
    }
    
    /**
     * Creates a new item with the specified type, count, and metadata.
     * 
     * @param type The item type
     * @param count The number of items in this stack
     * @param metadata Additional data about the item
     */
    public Item(byte type, int count, byte metadata) {
        this.type = type;
        this.count = Math.max(1, Math.min(count, getMaxStackSize()));
        this.metadata = metadata;
    }
    
    /**
     * Gets the item type.
     * 
     * @return The item type
     */
    public byte getType() {
        return type;
    }
    
    /**
     * Gets the number of items in this stack.
     * 
     * @return The item count
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Gets the item metadata.
     * 
     * @return The item metadata
     */
    public byte getMetadata() {
        return metadata;
    }
    
    /**
     * Creates a new item stack with the specified count.
     * 
     * @param newCount The new count
     * @return A new item with the same type and metadata but different count
     */
    public Item withCount(int newCount) {
        return new Item(type, newCount, metadata);
    }
    
    /**
     * Gets the maximum stack size for this item type.
     * 
     * @return The maximum stack size
     */
    public int getMaxStackSize() {
        // Tools and special items have a max stack size of 1
        if (isToolOrSpecial()) {
            return 1;
        }
        
        // Regular blocks and items have a max stack size of 64
        return 64;
    }
    
    /**
     * Checks if this item is a tool or special item.
     * 
     * @return true if this is a tool or special item, false otherwise
     */
    private boolean isToolOrSpecial() {
        // For now, we'll consider all items to be stackable blocks
        // In a real implementation, we would check the item type
        return false;
    }
    
    /**
     * Gets the display name of this item.
     * 
     * @return The item name
     */
    public String getName() {
        // For blocks, use the block name
        switch (type) {
            case Block.TYPE_AIR: return "Air";
            case Block.TYPE_DIRT: return "Dirt";
            case Block.TYPE_GRASS: return "Grass";
            case Block.TYPE_STONE: return "Stone";
            case Block.TYPE_WOOD: return "Wood";
            case Block.TYPE_LEAVES: return "Leaves";
            default: return "Unknown";
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Item other = (Item) obj;
        return type == other.type && metadata == other.metadata;
    }
    
    @Override
    public int hashCode() {
        return (type << 8) | metadata;
    }
    
    @Override
    public String toString() {
        return String.format("%s x%d", getName(), count);
    }
}