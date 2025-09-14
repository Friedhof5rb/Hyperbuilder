package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;

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
}
