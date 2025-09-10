package me.friedhof.hyperbuilder.computation.modules;

import me.friedhof.hyperbuilder.rendering.modules.TextureManager;
import me.friedhof.hyperbuilder.rendering.modules.TextureManager2D;
import me.friedhof.hyperbuilder.computation.modules.interfaces.HasTexture;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;
import me.friedhof.hyperbuilder.computation.modules.items.*;
import me.friedhof.hyperbuilder.rendering.modules.Texture4D;
import me.friedhof.hyperbuilder.rendering.modules.Texture2D;
import me.friedhof.hyperbuilder.computation.modules.items.Block;
import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all items in the game.
 * Manages item creation, texture loading, and provides factory methods.
 */
public class ItemRegistry {
  
    public static Map<Material, BaseItem> itemFactories = new HashMap<>();
    



    
    /**
     * Registers an item factory with the registry.
     * 
     * @param itemID The unique identifier for the item
     * @param factory The factory function to create items
     */
    public static void registerItem( BaseItem factory) {
        itemFactories.put(factory.getItemId(), factory);
    }

    public static BaseItem createItem(Material itemID, int count) {
        BaseItem item = itemFactories.get(itemID);
        if (item != null) {
            return item.withCount(count);
        }
        return null;
    }
  
    
    /**
     * Gets the 4D texture for an item's block representation.
     * 
     * @param itemID The item to get the texture for
     * @return The 4D texture, or null if not available
     */
    public static Texture4D getBlockTexture(Material itemID) {
        BaseItem item = createItem(itemID, 1);
        if (item instanceof HasTexture) {
            String textureName = ((HasTexture) item).getBlockTextureName();
            return TextureManager.getTexture(textureName + ".png");
        }
        return null;
    }
    
   
 
        
    /**
     * Gets the 4D texture for an item's block representation.
     * 
     * @param iteID The item to get the texture for
     * @return The 4D texture, or null if not available
     */
    public static Texture2D getItemTexture(Material itemID) {
        BaseItem item = createItem(itemID, 1);
        if (item instanceof HasTexture) {
            String textureName = ((HasTexture) item).getBlockTextureName();
            return TextureManager2D.getTexture2D(textureName + ".png");
        }
        return null;
    }
    
    /**
     * Creates a block from a placeable item using the item's properties.
     * 
     * @param item The placeable item to create a block from
     * @return The corresponding block, or null if the item is not placeable
     */
    public static Block createBlock(Material id) {

        BaseItem item = ItemRegistry.createItem(id, 1);
        if (!(item instanceof IsPlaceable)) {
            return null;
        }
    
        return (Block) item;
    }
    
    /**
     * Registers all default items in the game.
     */
    public static void registerDefaultItems() {
        
        registerItem(new AirItem());
        registerItem(new DirtItem());
        registerItem(new FlintItem());
        registerItem(new GrassBlockItem());
        registerItem(new GrassItem());
        registerItem(new LeavesItem());
        registerItem(new PlantFiberItem());
        registerItem(new SaplingItem());
        registerItem(new SticksItem());
        registerItem(new StoneItem());
        registerItem(new WoodItem());
        registerItem(new FlintPickaxeItem());
        registerItem(new FlintAxeItem());
        registerItem(new FlintShovelItem());
        registerItem(new StonePickaxeItem());
        registerItem(new StoneAxeItem());
        registerItem(new StoneShovelItem());
        registerItem(new CoalOreItem());
        registerItem(new CoalItem());
        registerItem(new CopperOreItem());
        //registerItem(new CopperIngotItem());
        registerItem(new IronOreItem());
        //registerItem(new IronIngotItem());
        registerItem(new SmelterItem());


    }
    
    
}