package me.friedhof.hyperbuilder.computation.modules.items.blocks.ores;

import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;

import java.util.ArrayList;

public class CoalOreItem extends Block{

    public CoalOreItem() {
        super(Material.COAL_ORE, "Coal Ore", 64, 0);
    }

    public CoalOreItem(int count) {
        super(Material.COAL_ORE, "Coal Ore", 64, count);
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new CoalOreItem(newCount);
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
                drops.add(ItemRegistry.createItem(Material.COAL, 1));
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
        return "pickaxe";
    }
}
