package me.friedhof.hyperbuilder.computation.modules.items;



/**
 * Represents grass blocks - placeable solid blocks.
 */
public class GrassItem extends Block {
    
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

    @Override
    public boolean isBreakable() {
        return true;
    }
}