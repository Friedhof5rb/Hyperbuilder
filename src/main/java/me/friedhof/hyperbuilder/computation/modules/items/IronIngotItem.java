package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

public class IronIngotItem extends BaseItem{
    public IronIngotItem(int count) {
        super(Material.IRON_INGOT, "Iron Ingot", 999, count);
    }
    public IronIngotItem() {
        super(Material.IRON_INGOT, "Iron Ingot", 999, 0);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new IronIngotItem(newCount);
    }
}
