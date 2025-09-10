package me.friedhof.hyperbuilder.computation.modules.items.tools;

import  me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;

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
    public void setDurability(int durability) {
        this.durability = Math.max(0, Math.min(durability, maxDurability));
    }
    @Override
    public boolean canMine(Block block) {
      if(block.getBlockId() == Material.STONE 
      || block.getBlockId() == Material.COAL_ORE 
      || block.getBlockId() == Material.COPPER_ORE
      || block.getBlockId() == Material.SMELTER) {
            return true;
        }
        return false;
    }
    @Override
    public float getMiningSpeed(Block block) {
        if(block.getBlockId() == Material.STONE 
        || block.getBlockId() == Material.COAL_ORE 
        || block.getBlockId() == Material.COPPER_ORE
        || block.getBlockId() == Material.SMELTER) {
            return 5.0f;
        }
        return 1.0f;
    }
    @Override
    public String getToolType() {
        return "pickaxe";
    }
}
