package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
/**
 * Represents wood blocks - placeable solid blocks.
 */
public class WoodItem extends Block {
    
    public WoodItem(int count) {
        super(Material.WOOD_LOG, "Wood Log", 64, count);
    }
    
 
    
  
    
    // HasCollision implementation with moderate resistance
    @Override
    public float getCollisionResistance() {
        return 1.5f; // Wood is slightly harder than dirt/grass but softer than stone
    }

    @Override
    public BaseItem withCount(int newCount) {
       return new WoodItem(newCount);
    }
    @Override
    public boolean isSolid() {
        return true;
    }
    @Override
    public boolean isBreakable() {
        return true;
    }
}