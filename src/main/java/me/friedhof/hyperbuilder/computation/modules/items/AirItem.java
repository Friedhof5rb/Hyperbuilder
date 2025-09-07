package me.friedhof.hyperbuilder.computation.modules.items;

/**
 * Represents air blocks - special case item that is not placeable or solid.
 */
public class AirItem extends BaseItem {

    public AirItem(int count) {
        super("air", "Air", 64, count);
    }
    
    @Override
    public String getBlockTextureName() {
        return "air";
    }
    
    @Override
    public BaseItem withCount(int newCount) {
       return new AirItem(newCount);
    }

    @Override
    public boolean HasTexture() {
        return false;
    }
   
    @Override
    public boolean isSolid() {
        return false;
    }
}