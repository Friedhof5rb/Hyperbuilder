package me.friedhof.hyperbuilder.computation.modules.items;


import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import me.friedhof.hyperbuilder.computation.modules.World;
import java.util.ArrayList;

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
    public boolean hasTexture() {
        return true;
    }

    public boolean canPlaceAt(int x, int y, int z, int w, World world) {
        Material material = world.getBlock(new Vector4DInt(x,y,z,w)).getBlockId();

        if(material== Material.AIR){
            return true;
        }
        return false;
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

    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        drops.add(this);
        return drops;
    }

}