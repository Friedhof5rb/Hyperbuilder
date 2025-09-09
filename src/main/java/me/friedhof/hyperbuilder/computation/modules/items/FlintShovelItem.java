package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;

public class FlintShovelItem extends BaseItem implements IsTool {
    private int durability;
    private final int maxDurability = 50;
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
    public boolean canMine(Block block) {
       if(block.getBlockId() == Material.GRASS_BLOCK || block.getBlockId() == Material.DIRT) {
            return true;
        }
        return false;
    }
    @Override
    public float getMiningSpeed(Block block) {
        return 1.0f;
    }
}
