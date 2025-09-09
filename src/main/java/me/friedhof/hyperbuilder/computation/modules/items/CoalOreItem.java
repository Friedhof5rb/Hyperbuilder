package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

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

}
