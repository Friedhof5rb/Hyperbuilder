package me.friedhof.hyperbuilder.rendering.modules;

import java.awt.*;
import java.awt.image.BufferedImage;

import me.friedhof.hyperbuilder.computation.modules.Block;
import me.friedhof.hyperbuilder.computation.modules.Inventory;
import me.friedhof.hyperbuilder.computation.modules.Item;
import me.friedhof.hyperbuilder.rendering.modules.Texture2D;
import me.friedhof.hyperbuilder.rendering.modules.TextureManager2D;

import java.awt.event.MouseEvent;

/**
 * Inventory UI component for displaying and managing items.
 * Supports drag and drop, stack splitting, and item transfer.
 */
public class InventoryUI {
    // UI Configuration
    private static final int INVENTORY_ROWS = 4;
    private static final int INVENTORY_COLS = 9;
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_PADDING = 2;
    private static final int UI_PADDING = 20;
    private static final int HOTBAR_INVENTORY_GAP = 30;
    
    // Colors
    private static final Color BACKGROUND_COLOR = new Color(64, 64, 64, 220);
    private static final Color SLOT_BACKGROUND = new Color(32, 32, 32, 200);
    private static final Color SLOT_BORDER = new Color(128, 128, 128);
    private static final Color HOVER_BORDER = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color COUNT_COLOR = new Color(255, 255, 0);
    private static final Color TOOLTIP_BACKGROUND = new Color(0, 0, 0, 200);
    private static final Color TOOLTIP_BORDER = new Color(255, 255, 255, 150);
    private static final Color TOOLTIP_TEXT = Color.WHITE;
    
    // State
    private final int screenWidth;
    private final int screenHeight;
    private boolean visible = false;
    
    // Drag and drop state
    private Item draggedItem = null;
    private int draggedFromSlot = -1;
    private boolean draggedFromHotbar = false;
    private int mouseX = 0;
    private int mouseY = 0;
    private int hoveredSlot = -1;
    private boolean hoveredSlotIsHotbar = false;
    
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
        calculateUIBounds();
    }
    
    /**
     * Calculates the UI bounds for inventory and hotbar display.
     */
    private void calculateUIBounds() {
        // Calculate inventory panel size (3 rows since hotbar is separate)
        inventoryWidth = INVENTORY_COLS * SLOT_SIZE + (INVENTORY_COLS - 1) * SLOT_PADDING + 2 * UI_PADDING;
        inventoryHeight = (INVENTORY_ROWS - 1) * SLOT_SIZE + (INVENTORY_ROWS - 2) * SLOT_PADDING + 2 * UI_PADDING + 30; // +30 for title
        
        // Center inventory on screen
        inventoryX = (screenWidth - inventoryWidth) / 2;
        inventoryY = (screenHeight - inventoryHeight) / 2 - 50;
        
        // Position hotbar UI below inventory
        int hotbarWidth = 9 * SLOT_SIZE + 8 * SLOT_PADDING + 2 * UI_PADDING;
        hotbarUIX = (screenWidth - hotbarWidth) / 2;
        hotbarUIY = inventoryY + inventoryHeight + HOTBAR_INVENTORY_GAP;
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
        int startY = inventoryY + UI_PADDING + 25; // Account for title
        for (int row = 1; row < INVENTORY_ROWS; row++) { // Start from row 1 to skip hotbar
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotIndex = row * INVENTORY_COLS + col;
                int slotX = inventoryX + UI_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
                int slotY = startY + (row - 1) * (SLOT_SIZE + SLOT_PADDING); // Adjust Y position
                
                drawInventorySlot(g, inventory, slotIndex, slotX, slotY, false);
            }
        }
        
        // Draw hotbar UI
        drawHotbarUI(g, inventory, hotbar);
        
        // Draw dragged item
        if (draggedItem != null) {
            drawDraggedItem(g, draggedItem, mouseX, mouseY);
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
        int hotbarHeight = SLOT_SIZE + 2 * UI_PADDING;
        int hotbarWidth = 9 * SLOT_SIZE + 8 * SLOT_PADDING + 2 * UI_PADDING;
        
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
            int slotX = hotbarUIX + UI_PADDING + i * (SLOT_SIZE + SLOT_PADDING);
            int slotY = hotbarUIY + UI_PADDING;
            
            drawInventorySlot(g, inventory, i, slotX, slotY, true);
            
            // Highlight selected slot
            if (i == hotbar.getSelectedSlot()) {
                g.setColor(new Color(255, 255, 255, 100));
                g.setStroke(new BasicStroke(3));
                g.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            }
        }
    }
    
    /**
     * Draws a single inventory slot.
     */
    private void drawInventorySlot(Graphics2D g, Inventory inventory, int slotIndex, int x, int y, boolean isHotbar) {
        // Draw slot background
        g.setColor(SLOT_BACKGROUND);
        g.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);
        
        // Draw slot border (highlight if hovered)
        boolean isHovered = (hoveredSlot == slotIndex && hoveredSlotIsHotbar == isHotbar);
        if (isHovered) {
            g.setColor(HOVER_BORDER);
            g.setStroke(new BasicStroke(2));
        } else {
            g.setColor(SLOT_BORDER);
            g.setStroke(new BasicStroke(1));
        }
        g.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);
        
        // Get item from inventory
        Item item = inventory.getItem(slotIndex);
        if (item != null && item.getCount() > 0 && !(draggedFromSlot == slotIndex && draggedFromHotbar == isHotbar && draggedItem != null)) {
            // Draw item representation
            drawItemInSlot(g, item, x, y, SLOT_SIZE);
        }
    }
    
    /**
     * Draws an item in a slot.
     */
    private void drawItemInSlot(Graphics2D g, Item item, int x, int y, int size) {
        int itemSize = size - 8;
        int itemX = x + 4;
        int itemY = y + 4;
        
        // Try to get the 2D texture for this item type
        Texture2D texture = getTexture2DForItemType(item.getType());
        
        if (texture != null) {
            // Use the 2D PNG texture directly for the item
            BufferedImage textureImage = texture.getImage();
            
            // Draw the texture scaled to fit the item slot
            g.drawImage(textureImage, itemX, itemY, itemSize, itemSize, null);
            
            // Draw a subtle border around the textured item
            g.setColor(new Color(0, 0, 0, 100));
            g.setStroke(new BasicStroke(1));
            g.drawRect(itemX, itemY, itemSize, itemSize);
        } else {
            // Fallback to the old colored rectangle method for items without textures
            Color itemColor = getItemColor(item.getType());
            
            // Draw item background
            g.setColor(itemColor);
            g.fillRect(itemX, itemY, itemSize, itemSize);
            
            // Draw item border
            g.setColor(itemColor.darker());
            g.setStroke(new BasicStroke(1));
            g.drawRect(itemX, itemY, itemSize, itemSize);
            
            // Draw item abbreviation
            g.setColor(TEXT_COLOR);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            String abbrev = getItemAbbreviation(item.getType());
            FontMetrics fm = g.getFontMetrics();
            int textX = itemX + (itemSize - fm.stringWidth(abbrev)) / 2;
            int textY = itemY + (itemSize + fm.getAscent()) / 2;
            g.drawString(abbrev, textX, textY);
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
    private void drawDraggedItem(Graphics2D g, Item item, int x, int y) {
        int size = SLOT_SIZE;
        int drawX = x - size / 2;
        int drawY = y - size / 2;
        
        // Draw semi-transparent background
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(drawX, drawY, size, size);
        
        drawItemInSlot(g, item, drawX, drawY, size);
    }
    
    /**
     * Draws a tooltip showing the name of the hovered item.
     */
    private void drawItemTooltip(Graphics2D g, Inventory inventory) {
        // Get the item from the hovered slot
        Item item = inventory.getItem(hoveredSlot);
        if (item == null || item.getCount() <= 0) {
            return; // No item to show tooltip for
        }
        
        // Get item name
        String itemName = item.getName();
        if (item.getCount() > 1) {
            itemName += " x" + item.getCount();
        }
        
        // Set font for tooltip
        Font tooltipFont = new Font("Arial", Font.PLAIN, 12);
        g.setFont(tooltipFont);
        FontMetrics fm = g.getFontMetrics();
        
        // Calculate tooltip dimensions
        int textWidth = fm.stringWidth(itemName);
        int textHeight = fm.getHeight();
        int tooltipPadding = 6;
        int tooltipWidth = textWidth + 2 * tooltipPadding;
        int tooltipHeight = textHeight + 2 * tooltipPadding;
        
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
        
        // Draw tooltip text
        g.setColor(TOOLTIP_TEXT);
        int textX = tooltipX + tooltipPadding;
        int textY = tooltipY + tooltipPadding + fm.getAscent();
        g.drawString(itemName, textX, textY);
    }
    
    /**
     * Gets the color for an item type.
     */
    private Color getItemColor(byte itemType) {
        switch (itemType) {
            case Block.TYPE_DIRT: return new Color(139, 69, 19);
            case Block.TYPE_GRASS: return new Color(34, 139, 34);
            case Block.TYPE_STONE: return new Color(128, 128, 128);
            case Block.TYPE_WOOD: return new Color(160, 82, 45);
            case Block.TYPE_LEAVES: return new Color(0, 128, 0);
            default: return new Color(64, 64, 64);
        }
    }
    
    /**
     * Gets the abbreviation for an item type.
     */
    private String getItemAbbreviation(byte itemType) {
        switch (itemType) {
            case Block.TYPE_DIRT: return "D";
            case Block.TYPE_GRASS: return "G";
            case Block.TYPE_STONE: return "S";
            case Block.TYPE_WOOD: return "W";
            case Block.TYPE_LEAVES: return "L";
            default: return "?";
        }
    }
    
    // Getters and setters
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void toggleVisibility() { this.visible = !this.visible; }
    
    public void updateMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        updateHoveredSlot(x, y);
    }
    
    /**
     * Updates which slot is currently being hovered.
     */
    private void updateHoveredSlot(int mouseX, int mouseY) {
        hoveredSlot = -1;
        hoveredSlotIsHotbar = false;
        
        // Check inventory slots (skip first row since it's the hotbar)
        int startY = inventoryY + UI_PADDING + 25;
        for (int row = 1; row < INVENTORY_ROWS; row++) { // Start from row 1 to skip hotbar
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotIndex = row * INVENTORY_COLS + col;
                int slotX = inventoryX + UI_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
                int slotY = startY + (row - 1) * (SLOT_SIZE + SLOT_PADDING); // Adjust Y position
                
                if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                    mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                    hoveredSlot = slotIndex;
                    hoveredSlotIsHotbar = false;
                    return;
                }
            }
        }
        
        // Check hotbar slots
        for (int i = 0; i < 9; i++) {
            int slotX = hotbarUIX + UI_PADDING + i * (SLOT_SIZE + SLOT_PADDING);
            int slotY = hotbarUIY + UI_PADDING;
            
            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
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
            Item item = inventory.getItem(hoveredSlot);
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
            Item item = inventory.getItem(hoveredSlot);
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
        Item targetItem = inventory.getItem(hoveredSlot);
        
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
        Item targetItem = inventory.getItem(hoveredSlot);
        
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
     * @param itemType The item type
     * @return The corresponding Texture2D, or null if no texture is available
     */
    private Texture2D getTexture2DForItemType(byte itemType) {
        switch (itemType) {
            case Block.TYPE_DIRT:
                return TextureManager2D.getTexture2D("Dirt.png");
            case Block.TYPE_GRASS:
                return TextureManager2D.getTexture2D("Grass.png");
            case Block.TYPE_STONE:
                return TextureManager2D.getTexture2D("stone.png");
            case Block.TYPE_WOOD:
                return TextureManager2D.getTexture2D("wood_log.png");
            // case Block.TYPE_LEAVES:
            //     return TextureManager2D.getTexture2D("Leaves.png");
            default:
                return null; // No texture available, will use fallback rendering
        }
    }
}