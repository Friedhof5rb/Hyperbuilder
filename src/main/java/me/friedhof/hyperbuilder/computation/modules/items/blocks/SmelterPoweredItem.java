package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import java.util.ArrayList;

public class SmelterPoweredItem extends Block{


    public SmelterPoweredItem() {
        super(Material.SMELTER_POWERED, "Smelter", 999, 0);
    }
   
    public SmelterPoweredItem(int count) {
        super(Material.SMELTER_POWERED, "Smelter", 999, count);
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new SmelterPoweredItem(newCount);
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
    public float getCollisionResistance() {
        return 2f;
    }
    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        if(selectedItem instanceof IsTool){
            IsTool tool = (IsTool) selectedItem;
            if(tool.canMine(this)) {
                drops.add(ItemRegistry.createItem(Material.SMELTER, 1));
            }
        }
        return drops;
    }
}
