package me.friedhof.hyperbuilder.computation.modules.items;


import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;


public class Block extends BaseItem implements IsPlaceable{

  

  public Block(Material itemId,String displayName,int maxStackSize, int count) {
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
    public boolean HasTexture() {
        return true;
    }


    /**
     * Gets the unique identifier for this block type.
     * 
     * @return The block ID
     */
    public Material getBlockId() {
        return itemId;
    }
    
    @Override
    public String getBlockTextureName() {
        return itemId.toString();
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