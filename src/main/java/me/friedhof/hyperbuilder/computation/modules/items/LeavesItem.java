package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;

/**
 * Represents leaves blocks - placeable blocks with low collision resistance.
 */
public class LeavesItem extends BaseItem implements IsPlaceable, HasCollision {
    
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
}