package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;

/**
 * Represents stone blocks - placeable solid blocks with higher resistance.
 */
public class StoneItem extends Block{
    
    public StoneItem(int count) {
        super("stone", "Stone", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "stone";
    }
    // HasCollision implementation with higher resistance
    @Override
    public float getCollisionResistance() {
        return 2.0f; // Stone is harder than dirt/grass
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new StoneItem(newCount);
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