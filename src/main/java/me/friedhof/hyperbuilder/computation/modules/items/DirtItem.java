package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;

/**
 * Represents dirt blocks - placeable solid blocks.
 */
public class DirtItem extends Block implements IsPlaceable, HasCollision {
    
    public DirtItem(int count) {
        super("dirt", "Dirt", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "dirt";
    }
    
    @Override
    public BaseItem withCount(int newCount) {
       return new DirtItem(newCount);
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