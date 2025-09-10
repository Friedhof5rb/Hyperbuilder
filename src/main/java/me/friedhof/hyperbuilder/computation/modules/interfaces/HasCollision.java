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
    


}