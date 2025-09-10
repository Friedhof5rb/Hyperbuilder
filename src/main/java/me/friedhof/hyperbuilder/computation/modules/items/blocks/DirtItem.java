package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import java.util.ArrayList;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
/**
 * Represents dirt blocks - placeable solid blocks.
 */
public class DirtItem extends Block implements IsPlaceable, HasCollision {
    
    public DirtItem(int count) {
        super(Material.DIRT, "Dirt", 64, count);
    }
    public DirtItem() {
        super(Material.DIRT, "Dirt", 64, 0);
    }
    
    
    @Override
    public BaseItem withCount(int newCount) {
       return new DirtItem(newCount);
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