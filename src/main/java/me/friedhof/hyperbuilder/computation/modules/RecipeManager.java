package me.friedhof.hyperbuilder.computation.modules;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages all crafting recipes in the game.
 * Provides methods to register recipes, get available recipes, and handle crafting.
 */
public class RecipeManager {
    private static final Map<String, Recipe> recipes = new HashMap<>();
    private static final List<Recipe> recipeList = new ArrayList<>();
    private static boolean initialized = false;
    
    /**
     * Registers a recipe with the manager.
     * 
     * @param recipe The recipe to register
     */
    public static void registerRecipe(Recipe recipe) {
        recipes.put(recipe.getId(), recipe);
        recipeList.add(recipe);
    }
    
    /**
     * Gets a recipe by its ID.
     * 
     * @param id The recipe ID
     * @return The recipe, or null if not found
     */
    public static Recipe getRecipe(String id) {
        return recipes.get(id);
    }
    
    /**
     * Gets all registered recipes.
     * 
     * @return List of all recipes
     */
    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipeList); // Return copy to prevent modification
    }
    
    /**
     * Gets all recipes that can be crafted with the given inventory.
     * 
     * @param inventory The inventory to check against
     * @return List of craftable recipes
     */
    public static List<Recipe> getCraftableRecipes(Inventory inventory) {
        List<Recipe> craftable = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            if (recipe.canCraft(inventory)) {
                craftable.add(recipe);
            }
        }
        return craftable;
    }
    
    /**
     * Gets all recipes that cannot be crafted with the given inventory.
     * 
     * @param inventory The inventory to check against
     * @return List of non-craftable recipes
     */
    public static List<Recipe> getNonCraftableRecipes(Inventory inventory) {
        List<Recipe> nonCraftable = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            if (!recipe.canCraft(inventory)) {
                nonCraftable.add(recipe);
            }
        }
        return nonCraftable;
    }
    
    /**
     * Attempts to craft a recipe using the given inventory.
     * 
     * @param recipeId The ID of the recipe to craft
     * @param inventory The inventory to craft with
     * @return true if crafting was successful, false otherwise
     */
    public static boolean craftRecipe(String recipeId, Inventory inventory) {
        Recipe recipe = recipes.get(recipeId);
        if (recipe == null) {
            return false;
        }
        
        return recipe.craft(inventory);
    }
    
    /**
     * Checks if the inventory is full (no space for any new items).
     * 
     * @param inventory The inventory to check
     * @return true if the inventory is full, false otherwise
     */
    public static boolean isInventoryFull(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                return false; // Found an empty slot
            }
        }
        return true; // No empty slots found
    }
    
    /**
     * Initializes the recipe manager with default recipes.
     * This should be called once during game initialization.
     */
    public static void initializeDefaultRecipes() {
        if (initialized) {
            return;
        }
        
        // Sticks from Wood Log
        registerRecipe(new Recipe("sticks_from_wood", "Sticks", Material.STICKS, 4)
            .addIngredient(Material.WOOD_LOG, 1));
        
        registerRecipe(new Recipe("flint_pickaxe", "Flint Pickaxe", Material.FLINT_PICKAXE, 1)
            .addIngredient(Material.FLINT, 3)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

        // Flint Shovel
        registerRecipe(new Recipe("flint_shovel", "Flint Shovel", Material.FLINT_SHOVEL, 1)
            .addIngredient(Material.FLINT, 1)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

        // Flint Axe
        registerRecipe(new Recipe("flint_axe", "Flint Axe", Material.FLINT_AXE, 1)
            .addIngredient(Material.FLINT, 3)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));


        // Stone Pickaxe
        registerRecipe(new Recipe("stone_pickaxe", "Stone Pickaxe", Material.STONE_PICKAXE, 1)
            .addIngredient(Material.STONE, 3)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

        // Stone Shovel
        registerRecipe(new Recipe("stone_shovel", "Stone Shovel", Material.STONE_SHOVEL, 1)
            .addIngredient(Material.STONE, 1)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

        // Stone Axe
        registerRecipe(new Recipe("stone_axe", "Stone Axe", Material.STONE_AXE, 1)
            .addIngredient(Material.STONE, 3)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

        // Smelter
        registerRecipe(new Recipe("smelter", "Smelter", Material.SMELTER, 1)
            .addIngredient(Material.STONE_BRICK, 80));

        // Stone Brick - uses chisel durability instead of consuming it
        registerRecipe(new DurabilityRecipe("stone_brick", "Stone Brick", Material.STONE_BRICK, 6)
            .addToolIngredient(Material.STONE_CHISEL, 1, 1)// Chisel loses 1 durability
            .addIngredient(Material.STONE, 1));
    

        // Stone Chisel
        registerRecipe(new Recipe("stone_chisel", "Stone Chisel", Material.STONE_CHISEL, 1)
            .addIngredient(Material.STONE, 1)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));
 
        // Copper Pickaxe
        registerRecipe(new Recipe("copper_pickaxe", "Copper Pickaxe", Material.COPPER_PICKAXE, 1)
            .addIngredient(Material.COPPER_INGOT, 3)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

        // Copper Shovel
        registerRecipe(new Recipe("copper_shovel", "Copper Shovel", Material.COPPER_SHOVEL, 1)
            .addIngredient(Material.COPPER_INGOT, 1)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));

          // Copper Axe
        registerRecipe(new Recipe("copper_axe", "Copper Axe", Material.COPPER_AXE, 1)
            .addIngredient(Material.COPPER_INGOT, 3)
            .addIngredient(Material.STICKS, 1)
            .addIngredient(Material.PLANT_FIBER, 1));




        initialized = true;
        System.out.println("RecipeManager initialized with " + recipeList.size() + " recipes");
    }
    
    /**
     * Gets the number of registered recipes.
     * 
     * @return The recipe count
     */
    public static int getRecipeCount() {
        return recipeList.size();
    }
    
    /**
     * Clears all registered recipes (mainly for testing).
     */
    public static void clearRecipes() {
        recipes.clear();
        recipeList.clear();
        initialized = false;
    }
}