package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

public class CoalItem extends BaseItem{

    public CoalItem() {
        super(Material.COAL, "Coal", 64, 0);
    }
    public CoalItem(int count) {
        super(Material.COAL, "Coal", 64, count);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new CoalItem(newCount);
    }


}
