package me.friedhof.hyperbuilder.computation.modules.items;

import  me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;

public class StonePickaxeItem extends BaseItem implements IsTool {
    private int durability;
    private final int maxDurability = 100;
    public StonePickaxeItem() {
        super(Material.STONE_PICKAXE, "Stone Pickaxe", 1, 0);
        this.durability = maxDurability;
    }

    public StonePickaxeItem(int count) {
        super(Material.STONE_PICKAXE, "Stone Pickaxe", 1, count);
        this.durability = maxDurability;
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new StonePickaxeItem(newCount);
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
      if(block.getBlockId() == Material.STONE) {
            return true;
        }
        return false;
    }
    @Override
    public float getMiningSpeed(Block block) {
        return 2.0f;
    }

}
