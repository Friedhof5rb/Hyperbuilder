package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

/**
 * Represents grass blocks - placeable solid blocks.
 */
public class GrassItem extends Block {
    
    public GrassItem(int count) {
        super(Material.GRASS, "Grass", 64, count);
    }
    public GrassItem() {
        super(Material.GRASS, "Grass", 64, 0);
    }
    
    
  
      @Override
    public BaseItem withCount(int newCount) {
       return new GrassItem(newCount);
    }
   @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isBreakable() {
        return true;
    }
}