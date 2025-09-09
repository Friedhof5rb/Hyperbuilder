package me.friedhof.hyperbuilder.rendering.modules;

import java.awt.*;
import java.awt.image.BufferedImage;

import me.friedhof.hyperbuilder.computation.modules.Inventory;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import java.awt.event.MouseEvent;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.RecipeManager;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;


/**
 * Inventory UI component for displaying and managing items.
 * Supports drag and drop, stack splitting, and item transfer.
 */
public class InventoryUI {
    // UI Configuration
    private static final int INVENTORY_ROWS = 4;
    private static final int INVENTORY_COLS = 9;
    
    // Dynamic sizing based on screen dimensions
    private int slotSize;
    private int slotPadding;
    private int uiPadding;
    private int hotbarInventoryGap;
    
    // Colors
    private static final Color BACKGROUND_COLOR = new Color(192, 192, 192, 220);
    private static final Color SLOT_BACKGROUND = new Color(192, 192, 192, 220);
    private static final Color SLOT_BORDER = new Color(128, 128, 128);
    private static final Color HOVER_BORDER = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color COUNT_COLOR = new Color(255, 255, 0);
    private static final Color TOOLTIP_BACKGROUND = new Color(0, 0, 0, 200);
    private static final Color TOOLTIP_BORDER = new Color(255, 255, 255, 150);
    private static final Color TOOLTIP_TEXT = Color.WHITE;
    
    // State
    private int screenWidth;
    private int screenHeight;
    private boolean visible = false;
    
    // Drag and drop state
    private BaseItem draggedItem = null;
    private int draggedFromSlot = -1;
    private boolean draggedFromHotbar = false;
    private int mouseX = 0;
    private int mouseY = 0;
    private int hoveredSlot = -1;
    private boolean hoveredSlotIsHotbar = false;
    
    // Crafting UI
    private CraftingUI craftingUI;
    
    /**
     * Checks if the given coordinates are within the inventory UI bounds.
     */
    public boolean isWithinInventoryBounds(int x, int y) {
        // Check main inventory bounds
        if (x >= inventoryX && x <= inventoryX + inventoryWidth &&
            y >= inventoryY && y <= inventoryY + inventoryHeight) {
            return true;
        }
        
        // Check hotbar UI bounds
        int hotbarWidth = 9 * slotSize + 8 * slotPadding + 2 * uiPadding;
        int hotbarHeight = slotSize + 2 * uiPadding;
        if (x >= hotbarUIX && x <= hotbarUIX + hotbarWidth &&
            y >= hotbarUIY && y <= hotbarUIY + hotbarHeight) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets the currently dragged item (for external handling).
     */
    public BaseItem getDraggedItem() {
        return draggedItem;
    }
    
    /**
     * Clears the dragged item (for external handling).
     */
    public void clearDraggedItem() {
        draggedItem = null;
        draggedFromSlot = -1;
        draggedFromHotbar = false;
    }
    
    /**
     * Updates the dragged item count (for external handling).
     * If count becomes 0 or less, clears the dragged item.
     */
    public void updateDraggedItemCount(int newCount) {
        if (draggedItem != null) {
            if (newCount <= 0) {
                clearDraggedItem();
            } else {
                draggedItem = draggedItem.withCount(newCount);
            }
        }
    }
    
    // UI bounds
    private int inventoryX;
    private int inventoryY;
    private int inventoryWidth;
    private int inventoryHeight;
    private int hotbarUIX;
    private int hotbarUIY;
    
    /**
     * Creates a new inventory UI.
     * 
     * @param screenWidth The screen width
     * @param screenHeight The screen height
     */
    public InventoryUI(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        calculateDynamicSizes();
        calculateUIBounds();
        
        // Initialize crafting UI
        this.craftingUI = new CraftingUI(screenWidth, screenHeight);
        
        // Initialize recipe manager with default recipes
        RecipeManager.initializeDefaultRecipes();
    }
    
    /**
     * Updates the screen dimensions and recalculates UI bounds.
     */
    public void updateDimensions(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        calculateDynamicSizes();
        calculateUIBounds();
        
        // Update crafting UI dimensions
        if (craftingUI != null) {
            craftingUI.updateScreenSize(screenWidth, screenHeight);
        }
    }
    
    /**
     * Calculates dynamic sizes based on screen dimensions.
     */
    private void calculateDynamicSizes() {
        // Base slot size scales more aggressively with screen size
        // Use both width and height for better scaling
        int baseSize = Math.min(screenWidth, screenHeight) / 20;
        slotSize = Math.max(25, Math.min(80, baseSize));
        
        // Padding scales proportionally
        slotPadding = Math.max(1, slotSize / 15);
        uiPadding = Math.max(8, slotSize / 3);
        hotbarInventoryGap = Math.max(10, slotSize / 2);
    }
    
    /**
     * Calculates the UI bounds for inventory and hotbar display.
     */
    private void calculateUIBounds() {
        // Calculate inventory panel size (3 rows since hotbar is separate)
        inventoryWidth = INVENTORY_COLS * slotSize + (INVENTORY_COLS - 1) * slotPadding + 2 * uiPadding;
        inventoryHeight = (INVENTORY_ROWS - 1) * slotSize + (INVENTORY_ROWS - 2) * slotPadding + 2 * uiPadding + 30; // +30 for title
        
        // Center inventory on screen
        inventoryX = (screenWidth - inventoryWidth) / 2;
        inventoryY = (screenHeight - inventoryHeight) / 2 - 50;
        
        // Position hotbar UI below inventory
        int hotbarWidth = 9 * slotSize + 8 * slotPadding + 2 * uiPadding;
        hotbarUIX = (screenWidth - hotbarWidth) / 2;
        hotbarUIY = inventoryY + inventoryHeight + hotbarInventoryGap;
    }
    
    /**
     * Renders the inventory UI.
     * 
     * @param g The graphics context
     * @param inventory The player's inventory
     * @param hotbar The hotbar component
     */
    public void render(Graphics2D g, Inventory inventory, Hotbar hotbar) {
        if (!visible) return;
        
        // Save original settings
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        
        // Draw inventory background
        g.setColor(BACKGROUND_COLOR);
        g.fillRoundRect(inventoryX, inventoryY, inventoryWidth, inventoryHeight, 10, 10);
        
        // Draw inventory border
        g.setColor(SLOT_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(inventoryX, inventoryY, inventoryWidth, inventoryHeight, 10, 10);
        
        // Draw inventory title
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics titleFm = g.getFontMetrics();
        String title = "Inventory";
        int titleX = inventoryX + (inventoryWidth - titleFm.stringWidth(title)) / 2;
        int titleY = inventoryY + titleFm.getAscent() + 5;
        g.drawString(title, titleX, titleY);
        
        // Draw inventory slots (skip first row since it's the hotbar)
        int startY = inventoryY + uiPadding + 25; // Account for title
        for (int row = 1; row < INVENTORY_ROWS; row++) { // Start from row 1 to skip hotbar
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotIndex = row * INVENTORY_COLS + col;
                int slotX = inventoryX + uiPadding + col * (slotSize + slotPadding);
                int slotY = startY + (row - 1) * (slotSize + slotPadding); // Adjust Y position
                
                drawInventorySlot(g, inventory, slotIndex, slotX, slotY, false);
            }
        }
        
        // Draw hotbar UI
        drawHotbarUI(g, inventory, hotbar);
        
        // Draw dragged item
        if (draggedItem != null) {
            drawDraggedItem(g, draggedItem, mouseX, mouseY);
        }
        
        // Draw crafting UI
        if (craftingUI != null) {
            craftingUI.render(g, inventory);
        }
        
        // Draw item tooltip if hovering over a slot
        if (hoveredSlot != -1) {
            drawItemTooltip(g, inventory);
        }
        
        // Restore original settings
        g.setFont(originalFont);
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }
    
    /**
     * Draws the hotbar UI section.
     */
    private void drawHotbarUI(Graphics2D g, Inventory inventory, Hotbar hotbar) {
        // Draw hotbar background
        int hotbarHeight = slotSize + 2 * uiPadding;
        int hotbarWidth = 9 * slotSize + 8 * slotPadding + 2 * uiPadding;
        
        g.setColor(BACKGROUND_COLOR);
        g.fillRoundRect(hotbarUIX, hotbarUIY, hotbarWidth, hotbarHeight, 10, 10);
        
        g.setColor(SLOT_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(hotbarUIX, hotbarUIY, hotbarWidth, hotbarHeight, 10, 10);
        
        // Draw hotbar title
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String hotbarTitle = "Hotbar";
        int titleX = hotbarUIX + (hotbarWidth - fm.stringWidth(hotbarTitle)) / 2;
        int titleY = hotbarUIY - 5;
        g.drawString(hotbarTitle, titleX, titleY);
        
        // Draw hotbar slots
        for (int i = 0; i < 9; i++) {
            int slotX = hotbarUIX + uiPadding + i * (slotSize + slotPadding);
            int slotY = hotbarUIY + uiPadding;
            
            drawInventorySlot(g, inventory, i, slotX, slotY, true);
            
        }
    }
    
    /**
     * Draws a single inventory slot.
     */
    private void drawInventorySlot(Graphics2D g, Inventory inventory, int slotIndex, int x, int y, boolean isHotbar) {
        // Draw slot background
        g.setColor(SLOT_BACKGROUND);
        g.fillRect(x, y, slotSize, slotSize);
        
        // Draw slot border (highlight if hovered)
        boolean isHovered = (hoveredSlot == slotIndex && hoveredSlotIsHotbar == isHotbar);
        if (isHovered) {
            g.setColor(HOVER_BORDER);
            g.setStroke(new BasicStroke(2));
        } else {
            g.setColor(SLOT_BORDER);
            g.setStroke(new BasicStroke(1));
        }
        g.drawRect(x, y, slotSize, slotSize);
        
        // Get item from inventory
        me.friedhof.hyperbuilder.computation.modules.items.BaseItem item = inventory.getItem(slotIndex);
        if (item != null && item.getCount() > 0 && !(draggedFromSlot == slotIndex && draggedFromHotbar == isHotbar && draggedItem != null)) {
            // Draw item representation
            drawItemInSlot(g, item, x, y, slotSize);
        }
    }
    
    /**
     * Draws an item in a slot.
     */
    private void drawItemInSlot(Graphics2D g, me.friedhof.hyperbuilder.computation.modules.items.BaseItem item, int x, int y, int size) {
        int itemSize = size - 8;
        int itemX = x + 4;
        int itemY = y + 4;
        
        // Try to get the 2D texture for this item type
        Texture2D texture = getTexture2DForItemType(item.getItemId());
        
        if (texture != null) {
            // Use the 2D PNG texture directly for the item
            BufferedImage textureImage = texture.getImage();
            
            // Draw the texture scaled to fit the item slot
            g.drawImage(textureImage, itemX, itemY, itemSize, itemSize, null);
            
            // Draw a subtle border around the textured item
            g.setColor(new Color(192, 192, 192, 220));
            g.setStroke(new BasicStroke(1));
            g.drawRect(itemX, itemY, itemSize, itemSize);
        } else {
            // Fallback to the old colored rectangle method for items without textures
            Color itemColor = getItemColor();
            
            // Draw item background
            g.setColor(itemColor);
            g.fillRect(itemX, itemY, itemSize, itemSize);
            
            // Draw item border
            g.setColor(itemColor.darker());
            g.setStroke(new BasicStroke(1));
            g.drawRect(itemX, itemY, itemSize, itemSize);
        }
        
        // Draw item count if > 1
        if (item.getCount() > 1) {
            g.setColor(COUNT_COLOR);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            String countText = String.valueOf(item.getCount());
            FontMetrics countFm = g.getFontMetrics();
            int countX = x + size - countFm.stringWidth(countText) - 2;
            int countY = y + size - 2;
            g.drawString(countText, countX, countY);
        }
    }
    
    /**
     * Draws the currently dragged item.
     */
    private void drawDraggedItem(Graphics2D g, me.friedhof.hyperbuilder.computation.modules.items.BaseItem item, int x, int y) {
        int size = slotSize;
        int drawX = x - size / 2;
        int drawY = y - size / 2;
        
        // Draw semi-transparent background
        g.setColor(new Color(0,0,0,0));
        g.fillRect(drawX, drawY, size, size);
        
        drawItemInSlot(g, item, drawX, drawY, size);
    }
    
    /**
     * Draws a tooltip showing the name of the hovered item.
     */
    private void drawItemTooltip(Graphics2D g, Inventory inventory) {
        // Get the item from the hovered slot
        me.friedhof.hyperbuilder.computation.modules.items.BaseItem item = inventory.getItem(hoveredSlot);
        if (item == null || item.getCount() <= 0) {
            return; // No item to show tooltip for
        }
        
        // Get item name
        String itemName = item.getDisplayName();
        if (item.getCount() > 1) {
            itemName += " x" + item.getCount();
        }
        
        // Check if item is a tool and get durability info
        String durabilityText = null;
        if (item instanceof IsTool) {
            IsTool tool = (IsTool) item;
            int currentDurability = tool.getCurrentDurability();
            int maxDurability = tool.getMaxDurability();
            durabilityText = "Durability: " + currentDurability + "/" + maxDurability;
        }
        
        // Set fonts for tooltip
        Font nameFont = new Font("Arial", Font.BOLD, 12);
        Font durabilityFont = new Font("Arial", Font.PLAIN, 10);
        
        // Calculate text dimensions
        g.setFont(nameFont);
        FontMetrics nameFm = g.getFontMetrics();
        int nameWidth = nameFm.stringWidth(itemName);
        int nameHeight = nameFm.getHeight();
        
        int durabilityWidth = 0;
        int durabilityHeight = 0;
        FontMetrics durabilityFm = null;
        if (durabilityText != null) {
            g.setFont(durabilityFont);
            durabilityFm = g.getFontMetrics();
            durabilityWidth = durabilityFm.stringWidth(durabilityText);
            durabilityHeight = durabilityFm.getHeight();
        }
        
        // Calculate tooltip dimensions
        int tooltipPadding = 6;
        int textWidth = Math.max(nameWidth, durabilityWidth);
        int totalTextHeight = nameHeight + (durabilityText != null ? durabilityHeight + 2 : 0); // +2 for spacing
        int tooltipWidth = textWidth + 2 * tooltipPadding;
        int tooltipHeight = totalTextHeight + 2 * tooltipPadding;
        
        // Position tooltip near mouse cursor, but keep it on screen
        int tooltipX = mouseX + 10; // Offset from cursor
        int tooltipY = mouseY - tooltipHeight - 5; // Above cursor
        
        // Keep tooltip on screen
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 10; // Move to left of cursor
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 20; // Move below cursor
        }
        if (tooltipY + tooltipHeight > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 5;
        }
        
        // Draw tooltip background
        g.setColor(TOOLTIP_BACKGROUND);
        g.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, 4);
        
        // Draw tooltip border
        g.setColor(TOOLTIP_BORDER);
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, 4);
        
        // Draw item name
        g.setColor(TOOLTIP_TEXT);
        g.setFont(nameFont);
        int nameX = tooltipX + tooltipPadding;
        int nameY = tooltipY + tooltipPadding + nameFm.getAscent();
        g.drawString(itemName, nameX, nameY);
        
        // Draw durability text if available
        if (durabilityText != null && durabilityFm != null) {
            g.setFont(durabilityFont);
            g.setColor(new Color(200, 200, 200)); // Slightly dimmed color for subtext
            int durabilityX = tooltipX + tooltipPadding;
            int durabilityY = nameY + durabilityFm.getAscent() + 2; // +2 for spacing
            g.drawString(durabilityText, durabilityX, durabilityY);
        }
    }
    
    /**
     * Gets the color for an item type.
     */
    private Color getItemColor() {
      return new Color(255, 0, 220);
    }
    
 
    
    // Getters and setters
    public boolean isVisible() { return visible; }
    public CraftingUI getCraftingUI() { return craftingUI; }
    public void setVisible(boolean visible) { 
        this.visible = visible; 
        // Reset crafting UI state when inventory is closed
        if (!visible && craftingUI != null) {
            craftingUI.resetState();
        }
    }
    public void toggleVisibility() { 
        this.visible = !this.visible; 
        // Reset crafting UI state when inventory is closed
        if (!visible && craftingUI != null) {
            craftingUI.resetState();
        }
    }
    
    public void updateMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        updateHoveredSlot(x, y);
        
        // Update crafting UI mouse position
        if (craftingUI != null && visible) {
            craftingUI.handleMouseMove(new MouseEvent(
                new java.awt.Component() {}, 
                MouseEvent.MOUSE_MOVED, 
                System.currentTimeMillis(), 
                0, x, y, 0, false
            ), null);
        }
    }
    
    /**
     * Updates which slot is currently being hovered.
     */
    private void updateHoveredSlot(int mouseX, int mouseY) {
        hoveredSlot = -1;
        hoveredSlotIsHotbar = false;
        
        // Check inventory slots (skip first row since it's the hotbar)
        int startY = inventoryY + uiPadding + 25;
        for (int row = 1; row < INVENTORY_ROWS; row++) { // Start from row 1 to skip hotbar
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotIndex = row * INVENTORY_COLS + col;
                int slotX = inventoryX + uiPadding + col * (slotSize + slotPadding);
                int slotY = startY + (row - 1) * (slotSize + slotPadding); // Adjust Y position
                
                if (mouseX >= slotX && mouseX < slotX + slotSize &&
                    mouseY >= slotY && mouseY < slotY + slotSize) {
                    hoveredSlot = slotIndex;
                    hoveredSlotIsHotbar = false;
                    return;
                }
            }
        }
        
        // Check hotbar slots
        for (int i = 0; i < 9; i++) {
            int slotX = hotbarUIX + uiPadding + i * (slotSize + slotPadding);
            int slotY = hotbarUIY + uiPadding;
            
            if (mouseX >= slotX && mouseX < slotX + slotSize &&
                mouseY >= slotY && mouseY < slotY + slotSize) {
                hoveredSlot = i;
                hoveredSlotIsHotbar = true;
                return;
            }
        }
    }
    
    /**
     * Handles mouse click events.
     */
    public boolean handleMouseClick(int x, int y, int button, Inventory inventory) {
        if (!visible) return false;
        
        updateHoveredSlot(x, y);
        
        // Check if crafting UI handles the click first
        if (craftingUI != null) {
            boolean craftingHandled = craftingUI.handleMouseClick(new MouseEvent(
                new java.awt.Component() {}, 
                MouseEvent.MOUSE_CLICKED, 
                System.currentTimeMillis(), 
                0, x, y, 1, false, button
            ), inventory);
            
            if (craftingHandled) {
                return true;
            }
        }
        
        if (hoveredSlot == -1) return false;
        
        if (button == MouseEvent.BUTTON1) { // Left click - drag and drop
            return handleLeftClick(inventory);
        } else if (button == MouseEvent.BUTTON3) { // Right click - split stack
            return handleRightClick(inventory);
        }
        
        return false;
    }
    
    /**
     * Handles left click for drag and drop.
     */
    private boolean handleLeftClick(Inventory inventory) {
        if (draggedItem == null) {
            // Start dragging
            me.friedhof.hyperbuilder.computation.modules.items.BaseItem item = inventory.getItem(hoveredSlot);
            if (item != null && item.getCount() > 0) {
                draggedItem = item;
                draggedFromSlot = hoveredSlot;
                draggedFromHotbar = hoveredSlotIsHotbar;
                
                // Remove item from inventory temporarily
                inventory.setItem(hoveredSlot, null);
                return true;
            }
        } else {
            // Drop item
            return dropItem(inventory);
        }
        return false;
    }
    
    /**
     * Handles right click for stack splitting.
     */
    private boolean handleRightClick(Inventory inventory) {
        if (draggedItem == null) {
            me.friedhof.hyperbuilder.computation.modules.items.BaseItem item = inventory.getItem(hoveredSlot);
            if (item != null && item.getCount() > 1) {
                // Split stack in half
                int halfCount = item.getCount() / 2;
                int remainingCount = item.getCount() - halfCount;
                
                // Update original stack
                inventory.setItem(hoveredSlot, item.withCount(remainingCount));
                

                // Start dragging half
                draggedItem = item.withCount(halfCount);
                draggedFromSlot = -1;
                draggedFromHotbar = false;
                
                return true;
            }
        } else {
            // Drop single item from dragged stack
            return dropSingleItem(inventory);
        }
        return false;
    }
    
    /**
     * Drops the dragged item.
     */
    private boolean dropItem(Inventory inventory) {
        BaseItem targetItem = inventory.getItem(hoveredSlot);
        
        if (targetItem == null) {
            // Empty slot - place item
            inventory.setItem(hoveredSlot, draggedItem);
            draggedItem = null;
            draggedFromSlot = -1;
            draggedFromHotbar = false;
            return true;
        } else if (targetItem.equals(draggedItem)) {
            // Same item type - try to stack
            int maxStack = targetItem.getMaxStackSize();
            int currentCount = targetItem.getCount();
            int draggedCount = draggedItem.getCount();
            
            if (currentCount < maxStack) {
                int spaceAvailable = maxStack - currentCount;
                int amountToAdd = Math.min(spaceAvailable, draggedCount);
                
                inventory.setItem(hoveredSlot, targetItem.withCount(currentCount + amountToAdd));
                
                if (amountToAdd >= draggedCount) {
                    // All items stacked
                    draggedItem = null;
                    draggedFromSlot = -1;
                    draggedFromHotbar = false;
                } else {
                    // Some items remain
                    draggedItem = draggedItem.withCount(draggedCount - amountToAdd);
                }
                return true;
            }
        } else {
            // Different item - swap
            inventory.setItem(hoveredSlot, draggedItem);
            draggedItem = targetItem;
            return true;
        }
        
        // Couldn't drop - return to original slot
        inventory.setItem(draggedFromSlot, draggedItem);
        draggedItem = null;
        draggedFromSlot = -1;
        draggedFromHotbar = false;
        return true;
    }
    
    /**
     * Drops a single item from the dragged stack.
     */
    private boolean dropSingleItem(Inventory inventory) {
        BaseItem targetItem = inventory.getItem(hoveredSlot);
        
        if (targetItem == null) {
            // Empty slot - place single item
            inventory.setItem(hoveredSlot, draggedItem.withCount(1));
            
            if (draggedItem.getCount() > 1) {
                draggedItem = draggedItem.withCount(draggedItem.getCount() - 1);
            } else {
                draggedItem = null;
                draggedFromSlot = -1;
                draggedFromHotbar = false;
            }
            return true;
        } else if (targetItem.equals(draggedItem) && targetItem.getCount() < targetItem.getMaxStackSize()) {
            // Same item with space - add one
            inventory.setItem(hoveredSlot, targetItem.withCount(targetItem.getCount() + 1));
            
            if (draggedItem.getCount() > 1) {
                draggedItem = draggedItem.withCount(draggedItem.getCount() - 1);
            } else {
                draggedItem = null;
                draggedFromSlot = -1;
                draggedFromHotbar = false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Cancels current drag operation.
     */
    public void cancelDrag(Inventory inventory) {
        if (draggedItem != null) {
            inventory.setItem(draggedFromSlot, draggedItem);
            draggedItem = null;
        }
    }
    
    /**
     * Gets the corresponding 2D texture for an item type.
     * This allows items to use the same PNG textures as their block counterparts.
     * 
     * @param itemId The item ID
     * @return The corresponding Texture2D, or null if no texture is available
     */
    private Texture2D getTexture2DForItemType(Material itemId) {
       return ItemRegistry.getItemTexture(itemId);
    }
}