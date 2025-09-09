package me.friedhof.hyperbuilder.rendering.modules;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.awt.event.MouseEvent;

import me.friedhof.hyperbuilder.computation.modules.Inventory;
import me.friedhof.hyperbuilder.computation.modules.Recipe;
import me.friedhof.hyperbuilder.computation.modules.RecipeManager;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;

/**
 * Crafting UI component that displays recipes in a Factorio-style interface.
 * Shows on the right side of the inventory with scrolling support.
 */
public class CraftingUI {
    // UI Configuration
    private static final int RECIPE_ROWS = 6;
    private static final int RECIPE_COLS = 4;
    private static final int RECIPES_PER_PAGE = RECIPE_ROWS * RECIPE_COLS;
    
    // Dynamic sizing
    private int recipeSlotSize;
    private int recipeSlotPadding;
    private int uiPadding;
    
    // Colors
    private static final Color BACKGROUND_COLOR = new Color(192, 192, 192, 220);
    private static final Color RECIPE_BACKGROUND = new Color(160, 160, 160, 220);
    private static final Color RECIPE_BORDER = new Color(128, 128, 128);
    private static final Color RECIPE_HOVER_BORDER = new Color(255, 255, 255);
    private static final Color RECIPE_CRAFTABLE_BORDER = new Color(0, 255, 0);
    private static final Color RECIPE_UNAVAILABLE_BORDER = new Color(255, 0, 0);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color UNAVAILABLE_OVERLAY = new Color(128, 128, 128, 150);
    private static final Color TOOLTIP_BACKGROUND = new Color(0, 0, 0, 200);
    private static final Color TOOLTIP_BORDER = new Color(255, 255, 255, 150);
    private static final Color TOOLTIP_TEXT = Color.WHITE;
    
    // Scroll bar colors
    private static final Color SCROLLBAR_BACKGROUND = new Color(100, 100, 100, 200);
    private static final Color SCROLLBAR_THUMB = new Color(200, 200, 200, 200);
    private static final Color SCROLLBAR_THUMB_HOVER = new Color(220, 220, 220, 220);
    private static final Color SCROLLBAR_THUMB_DRAGGING = new Color(255, 255, 255, 220);
    
    // State
    private int screenWidth;
    private int screenHeight;
    private int scrollOffset = 0;
    private int hoveredRecipeIndex = -1;
    private int mouseX = 0;
    private int mouseY = 0;
    
    // UI bounds
    private int craftingX;
    private int craftingY;
    private int craftingWidth;
    private int craftingHeight;
    
    // Scroll bar
    private int scrollBarX;
    private int scrollBarY;
    private int scrollBarWidth;
    private int scrollBarHeight;
    private int scrollBarThumbY;
    private int scrollBarThumbHeight;
    private boolean isDraggingScrollBar = false;
    private int scrollBarDragOffset = 0;
    private boolean isHoveringScrollBar = false;
    
    /**
     * Creates a new crafting UI.
     * 
     * @param screenWidth The screen width
     * @param screenHeight The screen height
     */
    public CraftingUI(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        calculateDynamicSizes();
        calculateUIBounds();
    }
    
    /**
     * Updates the screen dimensions and recalculates UI bounds.
     */
    public void updateScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        calculateDynamicSizes();
        calculateUIBounds();
    }
    
    /**
     * Calculates dynamic sizes based on screen dimensions.
     */
    private void calculateDynamicSizes() {
        // Scale UI elements based on screen size
        int baseSize = Math.min(screenWidth, screenHeight);
        recipeSlotSize = Math.max(32, baseSize / 25); // Smaller than inventory slots
        recipeSlotPadding = Math.max(2, recipeSlotSize / 16);
        uiPadding = Math.max(8, recipeSlotSize / 4);
    }
    
    /**
     * Calculates the UI bounds for the crafting panel.
     */
    private void calculateUIBounds() {
        // Calculate crafting panel size
        craftingWidth = RECIPE_COLS * recipeSlotSize + (RECIPE_COLS - 1) * recipeSlotPadding + 2 * uiPadding + 20; // +20 for scroll bar
        craftingHeight = RECIPE_ROWS * recipeSlotSize + (RECIPE_ROWS - 1) * recipeSlotPadding + 2 * uiPadding + 30; // +30 for title
        
        // Position on the right side of the screen, aligned with inventory
        int inventoryWidth = 9 * (recipeSlotSize + 8) + 2 * uiPadding; // Approximate inventory width
        craftingX = (screenWidth + inventoryWidth) / 2 + 20; // 20px gap from inventory
        craftingY = (screenHeight - craftingHeight) / 2 - 50;
        
        // Scroll bar bounds
        scrollBarWidth = 16;
        scrollBarX = craftingX + craftingWidth - scrollBarWidth - uiPadding;
        scrollBarY = craftingY + uiPadding + 25; // Account for title
        scrollBarHeight = craftingHeight - 2 * uiPadding - 30; // Account for title
    }
    
    /**
     * Renders the crafting UI.
     * 
     * @param g The graphics context
     * @param inventory The player's inventory
     */
    public void render(Graphics2D g, Inventory inventory) {
        // Save original settings
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        
        // Draw crafting background
        g.setColor(BACKGROUND_COLOR);
        g.fillRoundRect(craftingX, craftingY, craftingWidth, craftingHeight, 10, 10);
        
        // Draw crafting border
        g.setColor(RECIPE_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(craftingX, craftingY, craftingWidth, craftingHeight, 10, 10);
        
        // Draw crafting title
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics titleFm = g.getFontMetrics();
        String title = "Crafting";
        int titleX = craftingX + (craftingWidth - titleFm.stringWidth(title)) / 2;
        int titleY = craftingY + titleFm.getAscent() + 5;
        g.drawString(title, titleX, titleY);
        
        // Get all recipes
        List<Recipe> allRecipes = RecipeManager.getAllRecipes();
        boolean inventoryFull = RecipeManager.isInventoryFull(inventory);
        
        // Calculate scroll bounds
        int maxScroll = Math.max(0, allRecipes.size() - RECIPES_PER_PAGE);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        // Draw recipe slots
        int startY = craftingY + uiPadding + 25; // Account for title
        int recipeIndex = 0;
        
        for (int row = 0; row < RECIPE_ROWS; row++) {
            for (int col = 0; col < RECIPE_COLS; col++) {
                int globalRecipeIndex = scrollOffset + recipeIndex;
                
                if (globalRecipeIndex < allRecipes.size()) {
                    Recipe recipe = allRecipes.get(globalRecipeIndex);
                    
                    int slotX = craftingX + uiPadding + col * (recipeSlotSize + recipeSlotPadding);
                    int slotY = startY + row * (recipeSlotSize + recipeSlotPadding);
                    
                    drawRecipeSlot(g, recipe, inventory, slotX, slotY, globalRecipeIndex, inventoryFull);
                }
                
                recipeIndex++;
            }
        }
        
        // Draw scroll bar if needed
        if (allRecipes.size() > RECIPES_PER_PAGE) {
            drawScrollBar(g, allRecipes.size());
        }
        
        // Draw recipe tooltip if hovering
        if (hoveredRecipeIndex >= 0 && hoveredRecipeIndex < allRecipes.size()) {
            drawRecipeTooltip(g, allRecipes.get(hoveredRecipeIndex), inventory);
        }
        
        // Restore original settings
        g.setFont(originalFont);
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }
    
    /**
     * Draws a single recipe slot.
     */
    private void drawRecipeSlot(Graphics2D g, Recipe recipe, Inventory inventory, int x, int y, int recipeIndex, boolean inventoryFull) {
        boolean canCraft = recipe.canCraft(inventory) && !inventoryFull;
        boolean isHovered = (hoveredRecipeIndex == recipeIndex);
        
        // Draw slot background
        g.setColor(RECIPE_BACKGROUND);
        g.fillRect(x, y, recipeSlotSize, recipeSlotSize);
        
        // Draw slot border with appropriate color
        if (isHovered) {
            g.setColor(RECIPE_HOVER_BORDER);
            g.setStroke(new BasicStroke(2));
        } else if (canCraft) {
            g.setColor(RECIPE_CRAFTABLE_BORDER);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(RECIPE_UNAVAILABLE_BORDER);
            g.setStroke(new BasicStroke(1));
        }
        g.drawRect(x, y, recipeSlotSize, recipeSlotSize);
        
        // Draw result item representation
        BaseItem resultItem = ItemRegistry.createItem(recipe.getResult(), recipe.getResultCount());
        if (resultItem != null) {
            drawItemInRecipeSlot(g, resultItem, x, y, recipeSlotSize);
        }
        
        // Draw unavailable overlay if recipe can't be crafted
        if (!canCraft) {
            g.setColor(UNAVAILABLE_OVERLAY);
            g.fillRect(x, y, recipeSlotSize, recipeSlotSize);
        }
        
        // Draw result count if > 1
        if (recipe.getResultCount() > 1) {
            g.setColor(TEXT_COLOR);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            String countText = String.valueOf(recipe.getResultCount());
            FontMetrics fm = g.getFontMetrics();
            int textX = x + recipeSlotSize - fm.stringWidth(countText) - 2;
            int textY = y + recipeSlotSize - 2;
            g.drawString(countText, textX, textY);
        }
    }
    
    /**
     * Draws an item in a recipe slot (similar to inventory but smaller).
     */
    private void drawItemInRecipeSlot(Graphics2D g, BaseItem item, int x, int y, int size) {
        int itemSize = size - 4;
        int itemX = x + 2;
        int itemY = y + 2;
        
        // Try to get the 2D texture for this item type
        Texture2D texture = getTexture2DForItemType(item.getItemId());
        
        if (texture != null) {
            // Use the 2D PNG texture directly for the item
            BufferedImage textureImage = texture.getImage();
            
            // Draw the texture scaled to fit the item slot
            g.drawImage(textureImage, itemX, itemY, itemSize, itemSize, null);
        } else {
            // Fallback to colored rectangle
            Color itemColor = getItemColor();
            
            // Draw item background
            g.setColor(itemColor);
            g.fillRect(itemX, itemY, itemSize, itemSize);
            
            // Draw item border
            g.setColor(itemColor.darker());
            g.setStroke(new BasicStroke(1));
            g.drawRect(itemX, itemY, itemSize, itemSize);

        }
    }
    
    /**
     * Draws the scroll bar.
     */
    private void drawScrollBar(Graphics2D g, int totalRecipes) {
        // Draw scroll bar background
        g.setColor(SCROLLBAR_BACKGROUND);
        g.fillRect(scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight);
        
        // Calculate thumb position and size
        double visibleRatio = (double) RECIPES_PER_PAGE / totalRecipes;
        scrollBarThumbHeight = Math.max(20, (int) (scrollBarHeight * visibleRatio));
        
        double scrollRatio = (double) scrollOffset / (totalRecipes - RECIPES_PER_PAGE);
        scrollBarThumbY = scrollBarY + (int) ((scrollBarHeight - scrollBarThumbHeight) * scrollRatio);
        
        // Draw scroll bar thumb with appropriate color based on state
        Color thumbColor;
        if (isDraggingScrollBar) {
            thumbColor = SCROLLBAR_THUMB_DRAGGING;
        } else if (isHoveringScrollBar) {
            thumbColor = SCROLLBAR_THUMB_HOVER;
        } else {
            thumbColor = SCROLLBAR_THUMB;
        }
        
        g.setColor(thumbColor);
        g.fillRect(scrollBarX, scrollBarThumbY, scrollBarWidth, scrollBarThumbHeight);
        
        // Draw scroll bar border
        g.setColor(RECIPE_BORDER);
        g.setStroke(new BasicStroke(1));
        g.drawRect(scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight);
        
        // Draw thumb border for better visibility when dragging
        if (isDraggingScrollBar) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRect(scrollBarX, scrollBarThumbY, scrollBarWidth, scrollBarThumbHeight);
        }
    }
    
    /**
     * Draws a tooltip showing recipe details.
     */
    private void drawRecipeTooltip(Graphics2D g, Recipe recipe, Inventory inventory) {
        // Build tooltip text
        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(recipe.getDisplayName());
        
        if (recipe.getResultCount() > 1) {
            tooltipText.append(" x").append(recipe.getResultCount());
        }
        
        tooltipText.append("\n\nIngredients:");
        
        for (var ingredient : recipe.getIngredients().entrySet()) {
            Material material = ingredient.getKey();
            int required = ingredient.getValue();
            int available = 0;
            
            // Count available materials
            for (int i = 0; i < inventory.getSize(); i++) {
                BaseItem item = inventory.getItem(i);
                if (item != null && item.getItemId().equals(material)) {
                    available += item.getCount();
                }
            }
            
            tooltipText.append("\n- ").append(getDisplayName(material))
                      .append(": ").append(available).append("/").append(required);
        }
        
        // Draw tooltip
        String[] lines = tooltipText.toString().split("\n");
        
        Font tooltipFont = new Font("Arial", Font.PLAIN, 12);
        g.setFont(tooltipFont);
        FontMetrics fm = g.getFontMetrics();
        
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        
        int tooltipPadding = 8;
        int tooltipWidth = maxWidth + 2 * tooltipPadding;
        int tooltipHeight = lines.length * fm.getHeight() + 2 * tooltipPadding;
        
        // Position tooltip
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - tooltipHeight - 5;
        
        // Keep tooltip on screen
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 10;
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 20;
        }
        
        // Draw tooltip background
        g.setColor(TOOLTIP_BACKGROUND);
        g.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 5, 5);
        
        // Draw tooltip border
        g.setColor(TOOLTIP_BORDER);
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 5, 5);
        
        // Draw tooltip text
        g.setColor(TOOLTIP_TEXT);
        int textY = tooltipY + tooltipPadding + fm.getAscent();
        for (String line : lines) {
            g.drawString(line, tooltipX + tooltipPadding, textY);
            textY += fm.getHeight();
        }
    }
    
    /**
     * Handles mouse movement for hover detection.
     */
    public void handleMouseMove(MouseEvent e, Inventory inventory) {
        mouseX = e.getX();
        mouseY = e.getY();
        
        // Handle scroll bar dragging
        if (isDraggingScrollBar) {
            List<Recipe> allRecipes = RecipeManager.getAllRecipes();
            int maxScroll = allRecipes.size() - RECIPES_PER_PAGE;
            
            int newThumbY = mouseY - scrollBarDragOffset;
            double scrollRatio = (double) (newThumbY - scrollBarY) / (scrollBarHeight - scrollBarThumbHeight);
            scrollOffset = (int) (maxScroll * scrollRatio);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            return;
        }
        
        // Check if hovering over scroll bar thumb
        List<Recipe> allRecipes = RecipeManager.getAllRecipes();
        if (allRecipes.size() > RECIPES_PER_PAGE) {
            isHoveringScrollBar = (mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth &&
                                 mouseY >= scrollBarThumbY && mouseY < scrollBarThumbY + scrollBarThumbHeight);
        } else {
            isHoveringScrollBar = false;
        }
        
        // Check if hovering over a recipe slot
        hoveredRecipeIndex = -1;
        
        int startY = craftingY + uiPadding + 25;
        int recipeIndex = 0;
        
        for (int row = 0; row < RECIPE_ROWS; row++) {
            for (int col = 0; col < RECIPE_COLS; col++) {
                int globalRecipeIndex = scrollOffset + recipeIndex;
                
                if (globalRecipeIndex < allRecipes.size()) {
                    int slotX = craftingX + uiPadding + col * (recipeSlotSize + recipeSlotPadding);
                    int slotY = startY + row * (recipeSlotSize + recipeSlotPadding);
                    
                    if (mouseX >= slotX && mouseX < slotX + recipeSlotSize &&
                        mouseY >= slotY && mouseY < slotY + recipeSlotSize) {
                        hoveredRecipeIndex = globalRecipeIndex;
                        return;
                    }
                }
                
                recipeIndex++;
            }
        }
    }
    
    /**
     * Handles mouse clicks for crafting.
     */
    public boolean handleMouseClick(MouseEvent e, Inventory inventory) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return false;
        }
        
        // Check if clicking on a recipe
        if (hoveredRecipeIndex >= 0) {
            List<Recipe> allRecipes = RecipeManager.getAllRecipes();
            if (hoveredRecipeIndex < allRecipes.size()) {
                Recipe recipe = allRecipes.get(hoveredRecipeIndex);
                boolean success = RecipeManager.craftRecipe(recipe.getId(), inventory);
                if (success) {
                    System.out.println("Crafted: " + recipe.getDisplayName());
                }
                return true;
            }
        }
        
        // Check if clicking on scroll bar
        List<Recipe> allRecipes = RecipeManager.getAllRecipes();
        if (allRecipes.size() > RECIPES_PER_PAGE) {
            if (mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth &&
                mouseY >= scrollBarY && mouseY < scrollBarY + scrollBarHeight) {
                
                if (mouseY >= scrollBarThumbY && mouseY < scrollBarThumbY + scrollBarThumbHeight) {
                    // Start dragging scroll bar
                    isDraggingScrollBar = true;
                    scrollBarDragOffset = mouseY - scrollBarThumbY;
                    return true;
                } else {
                    // Click on scroll bar track - jump to position
                    double clickRatio = (double) (mouseY - scrollBarY) / scrollBarHeight;
                    int maxScroll = allRecipes.size() - RECIPES_PER_PAGE;
                    scrollOffset = (int) (maxScroll * clickRatio);
                    scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Handles mouse drag for scroll bar.
     */
    public boolean handleMouseDrag(MouseEvent e, Inventory inventory) {
        // Mouse drag is now handled in handleMouseMove for smoother dragging
        return isDraggingScrollBar;
    }
    
    /**
     * Handles mouse release for scroll bar.
     */
    public boolean handleMouseRelease(MouseEvent e) {
        if (isDraggingScrollBar) {
            isDraggingScrollBar = false;
            isHoveringScrollBar = false; // Reset hover state when releasing
            return true;
        }
        return false;
    }
    
    /**
     * Resets the UI state when the crafting menu is closed or hidden.
     * This prevents the slider from staying in a pressed state.
     */
    public void resetState() {
        isDraggingScrollBar = false;
        isHoveringScrollBar = false;
        hoveredRecipeIndex = -1;
        scrollBarDragOffset = 0;
    }
    
    /**
     * Handles mouse wheel scrolling.
     */
    public boolean handleMouseWheel(int wheelRotation, Inventory inventory) {
        // Check if mouse is over the crafting UI
        if (mouseX >= craftingX && mouseX < craftingX + craftingWidth &&
            mouseY >= craftingY && mouseY < craftingY + craftingHeight) {
            
            List<Recipe> allRecipes = RecipeManager.getAllRecipes();
            int maxScroll = Math.max(0, allRecipes.size() - RECIPES_PER_PAGE);
            
            scrollOffset += wheelRotation * 2; // Scroll 2 recipes at a time
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            
            return true;
        }
        
        return false;
    }
    
    // Helper methods (similar to InventoryUI)
    private Texture2D getTexture2DForItemType(Material itemId) {
        return ItemRegistry.getItemTexture(itemId);
    }
    
    private Color getItemColor() {
        return new Color(139, 69, 19); // Brown color for items without textures
    }
    
    
    private String getDisplayName(Material material) {
        BaseItem item = ItemRegistry.createItem(material, 1);
        return item != null ? item.getDisplayName() : material.getId();
    }
}