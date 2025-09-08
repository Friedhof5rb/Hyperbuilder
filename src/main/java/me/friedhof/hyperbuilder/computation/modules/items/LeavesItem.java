package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
/**
 * Represents leaves blocks - placeable blocks with low collision resistance.
 */
public class LeavesItem extends Block{
    
    public LeavesItem(int count) {
        super(Material.LEAVES, "Leaves", 64, count);
    }
    
  
    // HasCollision implementation with low resistance
    @Override
    public float getCollisionResistance() {
        return 0.5f; // Leaves are easy to break through
    }

    @Override
    public BaseItem withCount(int newCount) {
       return new LeavesItem(newCount);
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