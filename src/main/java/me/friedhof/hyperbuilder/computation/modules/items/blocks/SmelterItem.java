package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;

import java.util.ArrayList;

public class SmelterItem extends Block{
    public SmelterItem() {
        super(Material.SMELTER, "Smelter", 999, 0);
    }
   
    public SmelterItem(int count) {
        super(Material.SMELTER, "Smelter", 999, count);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new SmelterItem(newCount);
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
        return 5f;
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
