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
        
        // Basic crafting recipes using existing materials
        
        // Sticks from Wood Log
        registerRecipe(new Recipe("sticks_from_wood", "Sticks", Material.STICKS, 4)
            .addIngredient(Material.WOOD_LOG, 1));
        
        // Plant Fiber from Grass (alternative way to get plant fiber)
        registerRecipe(new Recipe("plant_fiber_from_grass", "Plant Fiber", Material.PLANT_FIBER, 2)
            .addIngredient(Material.GRASS, 3));
        
        // Flint tools/items (example advanced recipes)
        registerRecipe(new Recipe("flint_from_stone", "Flint", Material.FLINT, 2)
            .addIngredient(Material.STONE, 2)
            .addIngredient(Material.STICKS, 1));
        
        // Sapling cultivation
        registerRecipe(new Recipe("sapling_cultivation", "Sapling", Material.SAPLING, 1)
            .addIngredient(Material.LEAVES, 4)
            .addIngredient(Material.PLANT_FIBER, 2));
        
        // Grass Block creation
        registerRecipe(new Recipe("grass_block_creation", "Grass Block", Material.GRASS_BLOCK, 1)
            .addIngredient(Material.DIRT, 1)
            .addIngredient(Material.GRASS, 2));
        
        // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
        
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt1", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt2", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt3", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt4", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt5", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt6", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt7", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt8", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt9", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));
          // Advanced recipes requiring multiple materials
        registerRecipe(new Recipe("reinforced_dirt10", "Reinforced Dirt", Material.DIRT, 3)
            .addIngredient(Material.DIRT, 2)
            .addIngredient(Material.PLANT_FIBER, 1)
            .addIngredient(Material.STICKS, 1));



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