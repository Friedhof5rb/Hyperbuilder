package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import java.util.ArrayList;
/**
 * Represents wood blocks - placeable solid blocks.
 */
public class WoodItem extends Block {
    
    public WoodItem(int count) {
        super(Material.WOOD_LOG, "Wood Log", 64, count);
    }
    public WoodItem() {
        super(Material.WOOD_LOG, "Wood Log", 64, 0);
    }
 
    
  
    
    // HasCollision implementation with moderate resistance
    @Override
    public float getCollisionResistance() {
        return 1.5f; // Wood is slightly harder than dirt/grass but softer than stone
    }

    @Override
    public BaseItem withCount(int newCount) {
       return new WoodItem(newCount);
    }
    @Override
    public boolean isSolid() {
        return true;
    }
    @Override
    public boolean isBreakable() {
        return true;
    }

    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        if(selectedItem instanceof IsTool){
            IsTool tool = (IsTool) selectedItem;
            if(tool.canMine(this)) {
                drops.add(this);
            }
        }
        return drops;
    }
}