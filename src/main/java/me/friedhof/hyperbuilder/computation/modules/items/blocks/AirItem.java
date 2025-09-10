package me.friedhof.hyperbuilder.computation.modules.items.blocks;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
/**
 * Represents air blocks - special case item that is not placeable or solid.
 */
public class AirItem extends Block {

    public AirItem(int count) {
        super(Material.AIR, "Air", 999, count);
    }
     public AirItem() {
        super(Material.AIR, "Air", 999, 0);
    }
  
    
    @Override
    public BaseItem withCount(int newCount) {
       return new AirItem(newCount);
    }

    @Override
    public boolean hasTexture() {
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