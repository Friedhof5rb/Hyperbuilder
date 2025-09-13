package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import java.util.ArrayList;
/**
 * Represents wood blocks - placeable solid blocks.
 */
public class WoodItem extends Block {
    
    public WoodItem(int count) {
        super(Material.WOOD_LOG, "Wood Log", 999, count);
    }
    public WoodItem() {
        super(Material.WOOD_LOG, "Wood Log", 999, 0);
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
    @Override
    public int getBreakTier() {
        return 1;
    }
    @Override
    public String getBreakType() {
        return "axe";
    }
}