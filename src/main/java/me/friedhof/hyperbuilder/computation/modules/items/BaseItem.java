package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import me.friedhof.hyperbuilder.computation.modules.interfaces.HasTexture;
import me.friedhof.hyperbuilder.computation.modules.Material;

/**
 * Abstract base class for all items in the game.
 * Provides common functionality and properties that all items share.
 */
public abstract class BaseItem implements HasTexture, HasCollision{
    protected final Material itemId;
    protected final String displayName;
    protected final int maxStackSize;
    protected int count;
    protected byte metadata;
    
    /**
     * Creates a new base item with the specified properties.
     * 
     * @param itemId The unique identifier for this item type
     * @param displayName The human-readable name for this item
     * @param maxStackSize The maximum number of items that can be stacked
     * @param count The current number of items in this stack
     */
    protected BaseItem(Material itemId, String displayName, int maxStackSize, int count) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
        this.count = Math.max(1, Math.min(count, maxStackSize));
        this.metadata = 0;
    }
    
    /**
     * Creates a new base item with metadata.
     * 
     * @param itemId The unique identifier for this item type
     * @param displayName The human-readable name for this item
     * @param maxStackSize The maximum number of items that can be stacked
     * @param count The current number of items in this stack
     * @param metadata Additional data about the item
     */
    protected BaseItem(Material itemId, String displayName, int maxStackSize, int count, byte metadata) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
        this.count = Math.max(1, Math.min(count, maxStackSize));
        this.metadata = metadata;
    }

    @Override
    public String getBlockTextureName() {
        return itemId.toString();
    }

    
    /**
     * Gets the unique identifier for this item type.
     * 
     * @return The item ID
     */
    public Material getItemId() {
        return itemId;
    }
    
    /**
     * Gets the display name of this item.
     * 
     * @return The item name
     */
    public String getDisplayName() {
        return displayName;
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
     * Sets the number of items in this stack.
     * 
     * @param count The new count
     */
    public void setCount(int count) {
        this.count = Math.max(0, Math.min(count, maxStackSize));
    }
    
    /**
     * Gets the maximum stack size for this item type.
     * 
     * @return The maximum stack size
     */
    public int getMaxStackSize() {
        return maxStackSize;
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
     * Sets the item metadata.
     * 
     * @param metadata The new metadata
     */
    public void setMetadata(byte metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Creates a new item stack with the specified count.
     * 
     * @param newCount The new count
     * @return A new item with the same properties but different count
     */
    public abstract BaseItem withCount(int newCount);
    


    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BaseItem other = (BaseItem) obj;
        return itemId.equals(other.itemId) && metadata == other.metadata;
    }
    
    @Override
    public int hashCode() {
        return itemId.hashCode() ^ (metadata << 8);
    }
    
    @Override
    public String toString() {
        return String.format("%s x%d", displayName, count);
    }


    @Override
    public boolean hasTexture() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isBreakable() {
        return false;
    }


}