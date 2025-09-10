package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;

public class StoneAxeItem extends BaseItem implements IsTool {

    private int durability;
    private final int maxDurability = 100;

    public StoneAxeItem() {
        super(Material.STONE_AXE, "Stone Axe", 1, 0);
        this.durability = maxDurability;
    }

    public StoneAxeItem(int count) {
        super(Material.STONE_AXE, "Stone Axe", 1, count);
        this.durability = maxDurability;
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new StoneAxeItem(newCount);
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
        return this.durability <= 0;
    }
    @Override
    public void setDurability(int durability) {
        this.durability = Math.max(0, Math.min(durability, maxDurability));
    }
    
    @Override
    public boolean canMine(Block block) {
        if(block.getBlockId() == Material.WOOD_LOG) {
            return true;
        }
        return false;
    }

    @Override
    public float getMiningSpeed(Block block) {
        if(block.getBlockId() == Material.WOOD_LOG) {
            return 3.0f;
        }
        return 1.0f;
    }
    @Override
    public String getToolType() {
        return "axe";
    }
}
