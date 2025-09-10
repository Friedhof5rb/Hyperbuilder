package me.friedhof.hyperbuilder.computation.modules;

import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import java.util.Map;
import java.util.HashMap;

/**
 * A specialized recipe that handles tool durability loss during crafting.
 * Tools used in this recipe will lose durability and be returned to the inventory
 * instead of being consumed completely.
 */
public class DurabilityRecipe extends Recipe {
    private final Map<Material, Integer> durabilityLoss;
    
    /**
     * Creates a new durability recipe.
     * 
     * @param id Unique identifier for this recipe
     * @param displayName Display name shown in the crafting UI
     * @param result The material that is produced
     * @param resultCount How many of the result item are produced
     */
    public DurabilityRecipe(String id, String displayName, Material result, int resultCount) {
        super(id, displayName, result, resultCount);
        this.durabilityLoss = new HashMap<>();
    }
    
    /**
     * Adds a tool ingredient that will lose durability instead of being consumed.
     * 
     * @param material The tool material
     * @param count How many of this tool are needed (usually 1)
     * @param durabilityDamage How much durability the tool loses
     * @return This recipe for method chaining
     */
    public DurabilityRecipe addToolIngredient(Material material, int count, int durabilityDamage) {
        addIngredient(material, count);
        durabilityLoss.put(material, durabilityDamage);
        return this;
    }
    
    @Override
    public boolean craft(Inventory inventory) {
        // Check if we can craft
        if (!canCraft(inventory)) {
            return false;
        }
        
        // Check if inventory has space for the result
        if (!inventory.hasSpaceFor(getResult(), getResultCount())) {
            System.out.println("Inventory does not have space for the result");
            return false;
        }
        
        // Handle ingredients with special durability logic
        for (Map.Entry<Material, Integer> ingredient : getIngredients().entrySet()) {
            Material material = ingredient.getKey();
            int requiredCount = ingredient.getValue();
            
            if (durabilityLoss.containsKey(material)) {
                // This is a tool that should lose durability
                int damageAmount = durabilityLoss.get(material);
                
                // Find the tool in inventory
                BaseItem toolItem = null;
                int toolSlot = -1;
                
                for (int i = 0; i < inventory.getSize(); i++) {
                    BaseItem item = inventory.getItem(i);
                    if (item != null && item.getItemId().equals(material) && item.getCount() >= requiredCount) {
                        toolItem = item;
                        toolSlot = i;
                        break;
                    }
                }
                
                if (toolItem == null || !(toolItem instanceof IsTool)) {
                    return false; // Tool not found or not actually a tool
                }
                
                IsTool tool = (IsTool) toolItem;
                
                // Check if tool has enough durability
                if (tool.getCurrentDurability() <= damageAmount) {
                    // Tool would break, remove it completely
                    if (!inventory.removeItem(material, requiredCount)) {
                        return false;
                    }
                } else {
                    // Damage the tool and keep it in inventory
                    tool.damage(damageAmount);
                    // Tool stays in inventory with reduced durability
                }
            } else {
                // Regular ingredient - consume normally
                if (!inventory.removeItem(material, requiredCount)) {
                    return false;
                }
            }
        }
        
        // Add result
        return inventory.addItem(getResult(), getResultCount());
    }
    
    @Override
    public boolean canCraft(Inventory inventory) {
        // Check regular ingredients first
        if (!super.canCraft(inventory)) {
            return false;
        }
        
        // Additional check for tools with sufficient durability
        for (Map.Entry<Material, Integer> ingredient : getIngredients().entrySet()) {
            Material material = ingredient.getKey();
            
            if (durabilityLoss.containsKey(material)) {
                int damageAmount = durabilityLoss.get(material);
                
                // Find the tool and check its durability
                boolean hasUsableTool = false;
                for (int i = 0; i < inventory.getSize(); i++) {
                    BaseItem item = inventory.getItem(i);
                    if (item != null && item.getItemId().equals(material) && item instanceof IsTool) {
                        IsTool tool = (IsTool) item;
                        if (tool.getCurrentDurability() > 0) { // Tool must have at least 1 durability
                            hasUsableTool = true;
                            break;
                        }
                    }
                }
                
                if (!hasUsableTool) {
                    return false;
                }
            }
        }
        
        return true;
    }
}