package me.friedhof.hyperbuilder.computation.modules.items;


/**
 * Represents grass blocks - placeable items with no collision.
 * This is different from the existing grass item - these grass blocks can be placed but have no collision.
 */
public class GrassBlockItem extends Block{
    
    public GrassBlockItem(int count) {
        super("grass_block", "Grass Block", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "grass_block";
    }
    
    @Override
    public BaseItem withCount(int newCount) {
        return new GrassBlockItem(newCount);
    }
    
    @Override
    public boolean isSolid() {
        return false; // Grass blocks have no collision - players can walk through them
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