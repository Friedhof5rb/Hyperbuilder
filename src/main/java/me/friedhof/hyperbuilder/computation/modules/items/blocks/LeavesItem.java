package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import java.util.ArrayList;
/**
 * Represents leaves blocks - placeable blocks with low collision resistance.
 */
public class LeavesItem extends Block{
    
    public LeavesItem(int count) {
        super(Material.LEAVES, "Leaves", 999, count);
    }
    public LeavesItem() {
        super(Material.LEAVES, "Leaves", 999, 0);
    }
  
    @Override
    public float getCollisionResistance() {
        return 0.1f; // Leaves are easy to break through
    }

    @Override
    public BaseItem withCount(int newCount) {
       return new LeavesItem(newCount);
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
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
         // Random chance for sapling drop (10% chance)
        if (Math.random() < 0.1) {
            BaseItem saplingItem = ItemRegistry.createItem(Material.SAPLING, 1);
            drops.add(saplingItem);
        }
        // Random chance for sticks drop (20% chance)
        if (Math.random() < 0.2) {
            BaseItem sticksItem = ItemRegistry.createItem(Material.STICKS, 1);
            drops.add(sticksItem);
        }
        return drops;
    }
}