package me.friedhof.hyperbuilder.computation.modules.items.tools;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;

public class FlintPickaxeItem extends BaseItem implements IsTool{
    private int durability;
    private final int maxDurability = 50;

    public FlintPickaxeItem(int count) {
        super(Material.FLINT_PICKAXE, "Flint Pickaxe", 1, count);
        this.durability = maxDurability;
    }

    public FlintPickaxeItem() {
        super(Material.FLINT_PICKAXE, "Flint Pickaxe", 1, 0);
        this.durability = maxDurability;
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
         if(block.getBlockId() == Material.STONE) {
            return 2f;
        }
        return 1.0f;
    }
    @Override
    public String getToolType() {
        return "pickaxe";
    }
}
