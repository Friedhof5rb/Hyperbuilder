package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

public class StoneBricksItem extends BaseItem{


public StoneBricksItem(int count) {
        super(Material.STONE_BRICK, "Stone Bricks", 999, count);
    }
    public StoneBricksItem() {
        super(Material.STONE_BRICK, "Stone Bricks", 999, 0);
    }
    
    @Override
    public BaseItem withCount(int newCount) {
        return new StoneBricksItem(newCount);
    }
    

}
