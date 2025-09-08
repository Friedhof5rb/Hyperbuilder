package me.friedhof.hyperbuilder.computation.modules.items;


/**
 * Represents plant fiber - non-placeable crafting material.
 * Plant fiber cannot be placed as blocks and is used for crafting ropes and other items.
 */
public class PlantFiberItem extends BaseItem {
    
    public PlantFiberItem(int count) {
        super("plant_fiber", "Plant Fiber", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "plant_fiber";
    }
    
    @Override
    public BaseItem withCount(int newCount) {
        return new PlantFiberItem(newCount);
    }
    
}