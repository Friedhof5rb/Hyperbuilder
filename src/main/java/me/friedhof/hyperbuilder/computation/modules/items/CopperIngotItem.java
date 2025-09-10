package me.friedhof.hyperbuilder.computation.modules.items;

import  me.friedhof.hyperbuilder.computation.modules.Material;

public class CopperIngotItem extends BaseItem{
    public CopperIngotItem(int count) {
        super(Material.COPPER_INGOT, "Copper Ingot", 999, count);
    }
    public CopperIngotItem() {
        super(Material.COPPER_INGOT, "Copper Ingot", 999, 0);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new CopperIngotItem(newCount);
    }
}
