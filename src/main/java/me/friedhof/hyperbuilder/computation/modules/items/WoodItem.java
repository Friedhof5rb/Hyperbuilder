package me.friedhof.hyperbuilder.computation.modules.items;


/**
 * Represents wood blocks - placeable solid blocks.
 */
public class WoodItem extends Block {
    
    public WoodItem(int count) {
        super("wood_log", "Wood Log", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "wood_log";
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