package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

/**
 * Represents saplings - placeable items with no collision.
 * Saplings can be placed as blocks but do not block player movement.
 * They can grow into trees over time.
 */
public class SaplingItem extends Block {
    
    public SaplingItem(int count) {
        super(Material.SAPLING, "Sapling", 64, count);
    }
    public SaplingItem() {
        super(Material.SAPLING, "Sapling", 64, 0);
    }
    
    
    @Override
    public BaseItem withCount(int newCount) {
        return new SaplingItem(newCount);
    }
    
    @Override
    public boolean isSolid() {
        return false; // Saplings have no collision - players can walk through them
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