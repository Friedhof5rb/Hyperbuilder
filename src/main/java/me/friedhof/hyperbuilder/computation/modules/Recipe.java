package me.friedhof.hyperbuilder.computation.modules;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a crafting recipe with ingredients and result.
 * Used by the crafting system to define what items can be crafted and what they require.
 */
public class Recipe {
    private final String id;
    private final String displayName;
    private final Map<Material, Integer> ingredients;
    private final Material result;
    private final int resultCount;
    
    /**
     * Creates a new recipe.
     * 
     * @param id Unique identifier for this recipe
     * @param displayName Display name shown in the crafting UI
     * @param result The material that is produced
     * @param resultCount How many of the result item are produced
     */
    public Recipe(String id, String displayName, Material result, int resultCount) {
        this.id = id;
        this.displayName = displayName;
        this.result = result;
        this.resultCount = resultCount;
        this.ingredients = new HashMap<>();
    }
    
    /**
     * Adds an ingredient requirement to this recipe.
     * 
     * @param material The required material
     * @param count How many of this material are needed
     * @return This recipe for method chaining
     */
    public Recipe addIngredient(Material material, int count) {
        ingredients.put(material, count);
        return this;
    }
    
    /**
     * Gets the unique identifier for this recipe.
     * 
     * @return The recipe ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the display name for this recipe.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the ingredients required for this recipe.
     * 
     * @return Map of material to required count
     */
    public Map<Material, Integer> getIngredients() {
        return new HashMap<>(ingredients); // Return copy to prevent modification
    }
    
    /**
     * Gets the result material of this recipe.
     * 
     * @return The result material
     */
    public Material getResult() {
        return result;
    }
    
    /**
     * Gets how many result items are produced.
     * 
     * @return The result count
     */
    public int getResultCount() {
        return resultCount;
    }
    
    /**
     * Checks if the given inventory has enough materials to craft this recipe.
     * 
     * @param inventory The inventory to check
     * @return true if the recipe can be crafted, false otherwise
     */
    public boolean canCraft(Inventory inventory) {
        for (Map.Entry<Material, Integer> ingredient : ingredients.entrySet()) {
            if (!inventory.hasItem(ingredient.getKey(), ingredient.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Attempts to craft this recipe using the given inventory.
     * Consumes the required ingredients and adds the result to the inventory.
     * 
     * @param inventory The inventory to craft with
     * @return true if crafting was successful, false otherwise
     */
    public boolean craft(Inventory inventory) {
        // Check if we can craft
        if (!canCraft(inventory)) {
            return false;
        }
        
        // Check if inventory has space for the result
        if (!inventory.hasSpaceFor(result, resultCount)) {
            System.out.println("Inventory does not have space for the result");	
            return false;
        }
        
        // Consume ingredients
        for (Map.Entry<Material, Integer> ingredient : ingredients.entrySet()) {
            if (!inventory.removeItem(ingredient.getKey(), ingredient.getValue())) {
                // This shouldn't happen if canCraft returned true, but just in case
                return false;
            }
        }
        
        // Add result
        return inventory.addItem(result, resultCount);
    }
    
    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", result=" + result +
                ", resultCount=" + resultCount +
                ", ingredients=" + ingredients +
                '}';
    }
}