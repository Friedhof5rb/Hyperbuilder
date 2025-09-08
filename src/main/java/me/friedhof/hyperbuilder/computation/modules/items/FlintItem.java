package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

/**
 * Represents flint - placeable item with no collision.
 * Flint can be placed as blocks but does not block player movement.
 */
public class FlintItem extends Block {
    
    public FlintItem(int count) {
        super(Material.FLINT, "Flint", 64, count);
    }
    public FlintItem() {
        super(Material.FLINT, "Flint", 64, 0);
    }
 
    
    @Override
    public BaseItem withCount(int newCount) {
        return new FlintItem(newCount);
    }
    
    @Override
    public boolean isSolid() {
        return false; // Flint has no collision - players can walk through it
    }
    
    @Override
    public float getCollisionResistance() {
        return 0.0f; // No resistance since it has no collision
    }

    @Override
    public boolean isBreakable() {
        return true;
    }
}