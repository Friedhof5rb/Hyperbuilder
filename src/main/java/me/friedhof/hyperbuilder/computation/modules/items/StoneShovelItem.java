package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;

public class StoneShovelItem extends BaseItem implements IsTool {
    private int durability;
    private final int maxDurability = 100;

    public StoneShovelItem() {
        super(Material.STONE_SHOVEL, "Stone Shovel", 1, 0);
        this.durability = maxDurability;
    }

    public StoneShovelItem(int count) {
        super(Material.STONE_SHOVEL, "Stone Shovel", 1, count);
        this.durability = maxDurability;
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new StoneShovelItem(newCount);
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
    public boolean canMine(Block block) {
       if(block.getBlockId() == Material.STONE) {
            return true;
        }
        return false;
    }
    @Override
    public float getMiningSpeed(Block block) {
        if(block.getBlockId() == Material.DIRT || block.getBlockId() == Material.GRASS_BLOCK) {
            return 2.0f;
        }
        return 1.0f;
    }
    @Override
    public String getToolType() {
        return "shovel";
    }
}
