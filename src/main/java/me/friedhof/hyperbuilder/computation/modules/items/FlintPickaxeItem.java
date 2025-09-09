package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;

public class FlintPickaxeItem extends BaseItem implements IsTool{
    private int durability;
    private final int maxDurability = 50;

    public FlintPickaxeItem(int count) {
        super(Material.FLINT_PICKAXE, "Flint Pickaxe", 1, count);
    }

    public FlintPickaxeItem() {
        super(Material.FLINT_PICKAXE, "Flint Pickaxe", 1, 0);
    }
    

    @Override
    public FlintPickaxeItem withCount(int newCount) {
        return new FlintPickaxeItem(newCount);
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
        return 1.0f;
    }

}
