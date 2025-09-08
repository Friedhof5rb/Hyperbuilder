package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

/**
 * Represents plant fiber - non-placeable crafting material.
 * Plant fiber cannot be placed as blocks and is used for crafting ropes and other items.
 */
public class PlantFiberItem extends BaseItem {
    
    public PlantFiberItem(int count) {
        super(Material.PLANT_FIBER, "Plant Fiber", 64, count);
    }
    public PlantFiberItem() {
        super(Material.PLANT_FIBER, "Plant Fiber", 64, 0);
    }
    
    @Override
    public BaseItem withCount(int newCount) {
        return new PlantFiberItem(newCount);
    }
    
}