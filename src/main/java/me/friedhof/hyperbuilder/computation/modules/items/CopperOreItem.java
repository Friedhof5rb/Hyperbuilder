package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
import java.util.ArrayList;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;


public class CopperOreItem extends Block {

     public CopperOreItem() {
        super(Material.COPPER_ORE, "Copper Ore", 64, 0);
    }

    public CopperOreItem(int count) {
        super(Material.COPPER_ORE, "Copper Ore", 64, count);
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new CopperOreItem(newCount);
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
        return 3f;
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
