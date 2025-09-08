package me.friedhof.hyperbuilder.computation.modules.items;


/**
 * Represents leaves blocks - placeable blocks with low collision resistance.
 */
public class LeavesItem extends Block{
    
    public LeavesItem(int count) {
        super("leaves", "Leaves", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "leaves";
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