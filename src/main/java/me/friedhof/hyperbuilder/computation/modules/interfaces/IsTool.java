package me.friedhof.hyperbuilder.computation.modules.interfaces;

import me.friedhof.hyperbuilder.computation.modules.Block;

/**
 * Interface for items that function as tools.
 * Tools have durability and can be used to break blocks more efficiently.
 */
public interface IsTool {
    /**
     * Gets the maximum durability of this tool.
     * 
     * @return The maximum durability points
     */
    int getMaxDurability();
    
    /**
     * Gets the current durability of this tool.
     * 
     * @return The current durability points
     */
    int getCurrentDurability();
    
    /**
     * Damages the tool by the specified amount.
     * 
     * @param damage The amount of damage to apply
     * @return true if the tool is still usable, false if it broke
     */
    boolean damage(int damage);
    
    /**
     * Gets the mining speed multiplier for this tool against the specified block.
     * 
     * @param block The block being mined
     * @return The speed multiplier (1.0 = normal speed, 2.0 = twice as fast, etc.)
     */
    default float getMiningSpeed(Block block) {
        return 1.0f; // Default normal mining speed
    }
    
    /**
     * Checks if this tool can effectively mine the specified block type.
     * 
     * @param block The block to check
     * @return true if this tool can mine the block, false otherwise
     */
    default boolean canMine(Block block) {
        return true; // Default allows mining any block
    }
    
    /**
     * Gets the tool type for determining effectiveness against blocks.
     * 
     * @return The tool type identifier
     */
    default String getToolType() {
        return "generic";
    }
}