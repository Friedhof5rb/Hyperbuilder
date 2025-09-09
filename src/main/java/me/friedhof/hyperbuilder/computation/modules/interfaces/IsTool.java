package me.friedhof.hyperbuilder.computation.modules.interfaces;

import me.friedhof.hyperbuilder.computation.modules.items.Block;

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
     * Sets the current durability of this tool.
     * 
     * @param durability The new durability value
     */
    void setDurability(int durability);
    
    /**
     * Gets the mining speed multiplier for this tool against the specified block.
     * 
     * @param block The block being mined
     * @return The speed multiplier (1.0 = normal speed, 2.0 = twice as fast, etc.)
     */
    float getMiningSpeed(Block block);
    
    /**
     * Checks if this tool can effectively mine the specified block type.
     * 
     * @param block The block to check
     * @return true if this tool can mine the block, false otherwise
     */
    boolean canMine(Block block);
    
    String getToolType();


}