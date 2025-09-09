package me.friedhof.hyperbuilder.computation.modules.interfaces;

import me.friedhof.hyperbuilder.computation.modules.Vector4D;

/**
 * Interface for entities that should prevent block placement when they are in the way.
 * Entities implementing this interface will be checked during block placement attempts,
 * and if they occupy the same space as the intended block position, the placement will be blocked.
 */
public interface EntityInWay {
    
    /**
     * Checks if this entity is occupying or intersecting with the specified block position.
     * This method should return true if placing a block at the given coordinates would
     * interfere with this entity's position or collision box.
     * 
     * @param blockX The X coordinate of the block position
     * @param blockY The Y coordinate of the block position
     * @param blockZ The Z coordinate of the block position
     * @param blockW The W coordinate of the block position
     * @return true if this entity is in the way of block placement at the specified position
     */
    boolean isInWayOfBlock(int blockX, int blockY, int blockZ, int blockW);
    
    /**
     * Gets the position of this entity for collision calculations.
     * 
     * @return The current position of this entity
     */
    Vector4D getEntityPosition();
    
    /**
     * Gets the size dimensions of this entity for collision calculations.
     * 
     * @return An array containing [sizeX, sizeY, sizeZ, sizeW]
     */
    double[] getEntitySize();
}