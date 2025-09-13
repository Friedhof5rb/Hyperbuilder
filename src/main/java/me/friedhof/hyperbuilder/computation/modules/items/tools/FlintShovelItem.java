package me.friedhof.hyperbuilder.computation.modules.items.tools;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;

public class FlintShovelItem extends BaseItem implements IsTool {
    private int durability;
    private final int maxDurability = 100;
    public FlintShovelItem() {
        super(Material.FLINT_SHOVEL, "Flint Shovel", 1, 0);
        this.durability = maxDurability;
    }

    public FlintShovelItem(int count) {
        super(Material.FLINT_SHOVEL, "Flint Shovel", 1, count);
        this.durability = maxDurability;
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new FlintShovelItem(newCount);
    }
    @Override
    public int getMaxDurability() {
        return maxDurability;
    }
    @Override
    public int getCurrentDurability() {
        return this.durability;
    }
    @Override
    public boolean damage(int damage) {
        this.durability -= damage;
        return this.durability > 0;
    }
    @Override
    public void setDurability(int durability) {
        this.durability = Math.max(0, Math.min(durability, maxDurability));
    }
  
    @Override
    public String getToolType() {
        return "shovel";
    }
    @Override
    public int getBreakTier() {
        return 1;
    }
}
