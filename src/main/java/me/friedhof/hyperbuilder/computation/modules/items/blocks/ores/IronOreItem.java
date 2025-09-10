package me.friedhof.hyperbuilder.computation.modules.items.blocks.ores;

import me.friedhof.hyperbuilder.computation.modules.Material;
import java.util.ArrayList;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;

public class IronOreItem extends Block {

    public IronOreItem() {
        super(Material.IRON_ORE, "Iron Ore", 64, 0);
    }

    public IronOreItem(int count) {
        super(Material.IRON_ORE, "Iron Ore", 64, count);
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new IronOreItem(newCount);
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
