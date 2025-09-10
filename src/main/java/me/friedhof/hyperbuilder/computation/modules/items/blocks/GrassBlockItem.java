package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import java.util.ArrayList;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
/**
 * Represents grass blocks - placeable items with no collision.a
 * This is different from the existing grass item - these grass blocks can be placed but have no collision.
 */
public class GrassBlockItem extends Block{
    
    public GrassBlockItem(int count) {
        super(Material.GRASS_BLOCK, "Grass Block", 999, count);
    }
    public GrassBlockItem() {
        super(Material.GRASS_BLOCK, "Grass Block", 999, 0);
    }
    
    
    @Override
    public BaseItem withCount(int newCount) {
        return new GrassBlockItem(newCount);
    }
    
    @Override
    public boolean isSolid() {
        return true; // Grass blocks have no collision - players can walk through them
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