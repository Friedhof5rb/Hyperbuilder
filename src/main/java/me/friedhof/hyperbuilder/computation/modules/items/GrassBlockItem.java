package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
/**
 * Represents grass blocks - placeable items with no collision.a
 * This is different from the existing grass item - these grass blocks can be placed but have no collision.
 */
public class GrassBlockItem extends Block{
    
    public GrassBlockItem(int count) {
        super(Material.GRASS_BLOCK, "Grass Block", 64, count);
    }
    public GrassBlockItem() {
        super(Material.GRASS_BLOCK, "Grass Block", 64, 0);
    }
    
    
    @Override
    public BaseItem withCount(int newCount) {
        return new GrassBlockItem(newCount);
    }
    
    @Override
    public boolean isSolid() {
        return true; // Grass blocks have no collision - players can walk through them
    }

    @Override
    public boolean isBreakable() {
        return true;
    }
}