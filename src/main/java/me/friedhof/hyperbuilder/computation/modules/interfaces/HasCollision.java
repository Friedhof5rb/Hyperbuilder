package me.friedhof.hyperbuilder.computation.modules.interfaces;

/**
 * Interface for items that have collision properties when placed as blocks.
 * Items implementing this interface will block player movement and other entities.
 */
public interface HasCollision {
    /**
     * Checks if this item blocks movement when placed as a block.
     * 
     * @return true if this item has solid collision, false otherwise
     */
    boolean isSolid();
    
    boolean isBreakable();






    /**
     * Gets the collision resistance of this item.
     * Higher values mean the item is harder to break through.
     * 
     * @return The collision resistance (0.0 = no resistance, 1.0 = maximum resistance)
     */
    default float getCollisionResistance() {
        return 1.0f; // Default full resistance
    }
}