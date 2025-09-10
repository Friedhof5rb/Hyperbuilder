package me.friedhof.hyperbuilder.computation.modules;

import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages smelting recipes for the smelter.
 */
public class SmelterRecipe {
    private static final Map<Material, Material> recipes = new HashMap<>();
    
    static {
        // Initialize smelting recipes
        recipes.put(Material.IRON_ORE, Material.IRON_INGOT);
        recipes.put(Material.COPPER_ORE, Material.COPPER_INGOT);
    }
    
    /**
     * Checks if an item can be smelted.
     * 
     * @param input The input item to check
     * @return true if the item can be smelted, false otherwise
     */
    public static boolean canSmelt(BaseItem input) {
        if (input == null) {
            return false;
        }
        return recipes.containsKey(input.getItemId());
    }
    
    /**
     * Gets the smelting result for an input item.
     * 
     * @param input The input item
     * @return The smelted item, or null if no recipe exists
     */
    public static BaseItem getSmeltingResult(BaseItem input) {
        if (input == null || !canSmelt(input)) {
            return null;
        }
        
        Material outputMaterial = recipes.get(input.getItemId());
        return ItemRegistry.createItem(outputMaterial, 1);
    }
    
    /**
     * Gets all available smelting recipes.
     * 
     * @return A map of input materials to output materials
     */
    public static Map<Material, Material> getAllRecipes() {
        return new HashMap<>(recipes);
    }
}