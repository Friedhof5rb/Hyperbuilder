package me.friedhof.hyperbuilder.computation.modules.interfaces;

import me.friedhof.hyperbuilder.computation.modules.Player;

/**
 * Interface for items that can be consumed by the player.
 * Items implementing this interface can be right-clicked to consume them.
 */
public interface IsConsumable {
    /**
     * Consumes this item and applies its effects to the player.
     * 
     * @param player The player consuming the item
     * @return true if the item was successfully consumed, false otherwise
     */
    boolean consume(Player player);
    
    /**
     * Gets the time it takes to consume this item in milliseconds.
     * 
     * @return The consumption time in milliseconds
     */
    default long getConsumptionTime() {
        return 1000; // Default 1 second consumption time
    }
    
    /**
     * Checks if this item can be consumed by the specified player.
     * 
     * @param player The player attempting to consume the item
     * @return true if the item can be consumed, false otherwise
     */
    default boolean canConsume(Player player) {
        return true; // Default allows consumption by any player
    }
}