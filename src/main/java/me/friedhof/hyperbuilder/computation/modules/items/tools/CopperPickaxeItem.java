package me.friedhof.hyperbuilder.computation.modules.items.tools;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;

public class CopperPickaxeItem extends BaseItem implements IsTool {
    private int durability;
    private final int maxDurability = 500;

    public CopperPickaxeItem() {
        super(Material.COPPER_PICKAXE, "Copper Pickaxe", 1, 0);
        this.durability = maxDurability;
    }

    public CopperPickaxeItem(int count) {
        super(Material.COPPER_PICKAXE, "Copper Pickaxe", 1, count);
        this.durability = maxDurability;
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new CopperPickaxeItem(newCount);
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
        return "pickaxe";
    }


@Override
    public int getBreakTier() {
        return 2;
    }
}
