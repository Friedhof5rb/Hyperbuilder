package me.friedhof.hyperbuilder.computation.modules.items;


import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;



public class Block extends BaseItem implements IsPlaceable, HasCollision{

    public Block(String itemId) {
        super(itemId, ItemRegistry.itemFactories.get(itemId).getDisplayName(), 64, 0);
    }

  public Block(String itemId,String displayName,int maxStackSize, int count) {
        super(itemId,displayName,maxStackSize,count);
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new Block(itemId,displayName,maxStackSize,newCount);
    }


    @Override
    public boolean isBreakable() {
        return true;
    }
    @Override
    public boolean canPlaceAt(int x, int y, int z, int w) {
        return IsPlaceable.super.canPlaceAt(x, y, z, w);
    }

@Override
    public boolean HasTexture() {
        return true;
    }


    /**
     * Gets the unique identifier for this block type.
     * 
     * @return The block ID
     */
    public String getBlockId() {
        return itemId;
    }
    
    @Override
    public String getBlockTextureName() {
        return itemId;
    }
    

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Block block = (Block) obj;
        return itemId.equals(block.itemId);
    }
    
    @Override
    public int hashCode() {
        return itemId.hashCode();
    }
    
    @Override
    public String toString() {
        return "Block{" +
                "blockId='" + itemId + '\'' +
                '}';
    }

    @Override
    public boolean isSolid() {
        return ItemRegistry.itemFactories.get(itemId).isSolid();
    }
}