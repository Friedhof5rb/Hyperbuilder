package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import java.util.ArrayList;
/**
 * Represents stone blocks - placeable solid blocks with higher resistance.
 */
public class StoneItem extends Block{
    
    public StoneItem(int count) {
        super(Material.STONE, "Stone", 64, count);
    }
    public StoneItem() {
        super(Material.STONE, "Stone", 64, 0);
    }
  
    // HasCollision implementation with higher resistance
    @Override
    public float getCollisionResistance() {
        return 2.0f; // Stone is harder than dirt/grass
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new StoneItem(newCount);
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