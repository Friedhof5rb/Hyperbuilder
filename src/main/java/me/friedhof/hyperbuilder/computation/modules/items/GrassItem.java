package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import java.util.ArrayList;
/**
 * Represents grass blocks - placeable solid blocks.
 */
public class GrassItem extends Block {
    
    public GrassItem(int count) {
        super(Material.GRASS, "Grass", 999, count);
    }
    public GrassItem() {
        super(Material.GRASS, "Grass", 999, 0);
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
    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        if (Math.random() < 0.7) {
            BaseItem plantFiberItem = ItemRegistry.createItem(Material.PLANT_FIBER, 1);
            drops.add(plantFiberItem);
        }
        return drops;
    }
}