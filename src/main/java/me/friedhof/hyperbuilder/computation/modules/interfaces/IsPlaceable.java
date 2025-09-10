package me.friedhof.hyperbuilder.computation.modules.interfaces;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import me.friedhof.hyperbuilder.computation.modules.World;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import java.util.ArrayList;

/**
 * Interface for items that can be placed in the world as blocks.
 * Items implementing this interface can be right-clicked to place them.
 */
public interface IsPlaceable {
    
    /**
     * Checks if this item can be placed at the specified position.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     * @return true if the item can be placed at this position
     */
    boolean canPlaceAt(int x, int y, int z, int w, World world);

    ArrayList<BaseItem> drops(BaseItem selectedItem);



}