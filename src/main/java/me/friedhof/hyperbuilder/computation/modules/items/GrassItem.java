package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;

/**
 * Represents grass blocks - placeable solid blocks.
 */
public class GrassItem extends BaseItem implements IsPlaceable, HasCollision {
    
    public GrassItem(int count) {
        super("grass", "Grass", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "grass";
    }
    
  
      @Override
    public BaseItem withCount(int newCount) {
       return new GrassItem(newCount);
    }
   @Override
    public boolean isSolid() {
        return true;
    }
}