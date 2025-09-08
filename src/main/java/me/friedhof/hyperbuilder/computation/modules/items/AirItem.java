package me.friedhof.hyperbuilder.computation.modules.items;
import me.friedhof.hyperbuilder.computation.modules.Material;
/**
 * Represents air blocks - special case item that is not placeable or solid.
 */
public class AirItem extends Block {

    public AirItem(int count) {
        super(Material.AIR, "Air", 64, count);
    }
    
  
    
    @Override
    public BaseItem withCount(int newCount) {
       return new AirItem(newCount);
    }

    @Override
    public boolean HasTexture() {
        return false;
    }
   
    @Override
    public boolean isSolid() {
        return false;
    }
    
    @Override
    public boolean isBreakable() {
        return false;
    }
}