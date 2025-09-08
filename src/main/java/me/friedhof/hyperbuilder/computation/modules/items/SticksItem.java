package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;
/**
 * Represents sticks - non-placeable crafting material.
 * Sticks cannot be placed as blocks and are used for crafting tools and other items.
 */
public class SticksItem extends BaseItem {
    
    public SticksItem(int count) {
        super(Material.STICKS, "Sticks", 64, count);
    }
    
 
    
    @Override
    public BaseItem withCount(int newCount) {
        return new SticksItem(newCount);
    }
    
}