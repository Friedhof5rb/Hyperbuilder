package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;

import java.util.ArrayList;

public class Water extends Block {
    public Water() {
        super(Material.WATER, "Water", 1, 1);
    }
    public Water(int count) {
        super(Material.WATER, "Water", 1, count);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new Water(newCount);
    }

     @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isBreakable() {
        return false;
    }
    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        return drops;
    }

    @Override
    public int getBreakTier() {
        return 0;
    }
    @Override
    public String getBreakType() {
        return "bucket";
    }
}
