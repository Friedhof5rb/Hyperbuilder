package me.friedhof.hyperbuilder.computation.modules.items;


/**
 * Represents flint - placeable item with no collision.
 * Flint can be placed as blocks but does not block player movement.
 */
public class FlintItem extends Block {
    
    public FlintItem(int count) {
        super("flint", "Flint", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "flint";
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