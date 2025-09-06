package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Inventory;
import com.adventure4d.computation.modules.Item;
import com.adventure4d.computation.modules.Block;

import java.awt.*;
import com.adventure4d.rendering.modules.Texture2D;
import com.adventure4d.rendering.modules.TextureManager2D;
import java.awt.image.BufferedImage;

/**
 * Hotbar UI component for displaying and selecting items.
 */
public class Hotbar {
    // Hotbar configuration
    private static final int HOTBAR_SLOTS = 9;
    private static final int SLOT_SIZE = 50;
    private static final int SLOT_PADDING = 2;
    private static final int HOTBAR_PADDING = 10;
    
    // Colors
    private static final Color SLOT_BACKGROUND = new Color(64, 64, 64, 200);
    private static final Color SLOT_BORDER = new Color(128, 128, 128);
    private static final Color SELECTED_BORDER = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color COUNT_COLOR = new Color(255, 255, 0);
    
    // State
    private int selectedSlot = 0;
    private final int width;
    private final int height;
    
    // Item name display
    private static final long DISPLAY_DURATION_MS = 1000; // 3 seconds
    private static final long FADE_DURATION_MS = 500; // 1 second fade
    private long itemNameDisplayStartTime = 0;
    private String displayedItemName = "";
    private boolean showingItemName = false;
    
    /**
     * Creates a new hotbar with the specified screen dimensions.
     * 
     * @param width The screen width
     * @param height The screen height
     */
    public Hotbar(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Renders the hotbar.
     * 
     * @param g The graphics context
     * @param inventory The player's inventory
     */
    public void render(Graphics2D g, Inventory inventory) {
        // Save original settings
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        
        // Calculate hotbar position (centered at bottom)
        int hotbarWidth = HOTBAR_SLOTS * SLOT_SIZE + (HOTBAR_SLOTS - 1) * SLOT_PADDING + 2 * HOTBAR_PADDING;
        int hotbarHeight = SLOT_SIZE + 2 * HOTBAR_PADDING;
        int hotbarX = (width - hotbarWidth) / 2;
        int hotbarY = height - hotbarHeight - 20;
        
        // Draw hotbar background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(hotbarX, hotbarY, hotbarWidth, hotbarHeight, 10, 10);
        
        // Draw hotbar border
        g.setColor(SLOT_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(hotbarX, hotbarY, hotbarWidth, hotbarHeight, 10, 10);
        
        // Draw slots
        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            int slotX = hotbarX + HOTBAR_PADDING + i * (SLOT_SIZE + SLOT_PADDING);
            int slotY = hotbarY + HOTBAR_PADDING;
            
            // Draw slot background
            g.setColor(SLOT_BACKGROUND);
            g.fillRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            
            // Draw slot border (highlighted if selected)
            if (i == selectedSlot) {
                g.setColor(SELECTED_BORDER);
                g.setStroke(new BasicStroke(3));
            } else {
                g.setColor(SLOT_BORDER);
                g.setStroke(new BasicStroke(1));
            }
            g.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            
            // Get item from inventory
            Item item = inventory.getItem(i);
            if (item != null && item.getCount() > 0) {
                // Draw item representation
                drawItem(g, item, slotX, slotY, SLOT_SIZE);
                
                // Draw item count if > 1
                if (item.getCount() > 1) {
                    g.setColor(COUNT_COLOR);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    String countText = String.valueOf(item.getCount());
                    FontMetrics fm = g.getFontMetrics();
                    int textX = slotX + SLOT_SIZE - fm.stringWidth(countText) - 2;
                    int textY = slotY + SLOT_SIZE - 2;
                    g.drawString(countText, textX, textY);
                }
            }
            
            // Draw slot number
            g.setColor(TEXT_COLOR);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            String slotNumber = String.valueOf(i + 1);
            FontMetrics fm = g.getFontMetrics();
            int numberX = slotX + 2;
            int numberY = slotY + fm.getAscent() + 2;
            g.drawString(slotNumber, numberX, numberY);
        }
        
        // Draw item name display if active
        if (showingItemName) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - itemNameDisplayStartTime;
            
            if (elapsedTime < DISPLAY_DURATION_MS + FADE_DURATION_MS) {
                // Calculate alpha for fade effect
                float alpha = 1.0f;
                if (elapsedTime > DISPLAY_DURATION_MS) {
                    // Fade out phase
                    long fadeElapsed = elapsedTime - DISPLAY_DURATION_MS;
                    alpha = 1.0f - (float) fadeElapsed / FADE_DURATION_MS;
                    alpha = Math.max(0.0f, alpha);
                }
                
                // Draw item name below the selected slot
                if (!displayedItemName.isEmpty()) {
                    int selectedSlotX = hotbarX + HOTBAR_PADDING + selectedSlot * (SLOT_SIZE + SLOT_PADDING);
                    int selectedSlotY = hotbarY + HOTBAR_PADDING;
                    
                    // Set font and get metrics
                    Font nameFont = new Font("Arial", Font.BOLD, 16);
                    g.setFont(nameFont);
                    FontMetrics fm = g.getFontMetrics();
                    
                    // Calculate text position (centered below the slot)
                    int textWidth = fm.stringWidth(displayedItemName);
                    int textX = selectedSlotX + (SLOT_SIZE - textWidth) / 2;
                    int textY = selectedSlotY + SLOT_SIZE + 25;
                    
                    // Draw text background with fade
                    int bgAlpha = (int) (150 * alpha);
                    g.setColor(new Color(0, 0, 0, bgAlpha));
                    int padding = 4;
                    g.fillRoundRect(textX - padding, textY - fm.getAscent() - padding, 
                                  textWidth + 2 * padding, fm.getHeight() + 2 * padding, 6, 6);
                    
                    // Draw text with fade
                    int textAlpha = (int) (255 * alpha);
                    g.setColor(new Color(255, 255, 255, textAlpha));
                    g.drawString(displayedItemName, textX, textY);
                }
            } else {
                // Display time is over, hide the name
                showingItemName = false;
            }
        }
        
        // Restore original settings
        g.setFont(originalFont);
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }
    
    /**
     * Draws an item representation in a slot.
     * 
     * @param g The graphics context
     * @param item The item to draw
     * @param x The slot x position
     * @param y The slot y position
     * @param size The slot size
     */
    private void drawItem(Graphics2D g, Item item, int x, int y, int size) {
        int itemSize = size - 8; // Leave some padding
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
            
            // Draw a simple colored rectangle representing the block/item
            g.setColor(itemColor);
            g.fillRect(itemX, itemY, itemSize, itemSize);
            
            // Draw item border
            g.setColor(itemColor.darker());
            g.setStroke(new BasicStroke(1));
            g.drawRect(itemX, itemY, itemSize, itemSize);
        }
        
        // Draw item name (abbreviated)
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("Arial", Font.PLAIN, 8));
        String itemName = getItemAbbreviation(item.getType());
        FontMetrics fm = g.getFontMetrics();
        int textX = itemX + (itemSize - fm.stringWidth(itemName)) / 2;
        int textY = itemY + (itemSize + fm.getAscent()) / 2;
        g.drawString(itemName, textX, textY);
    }
    
    /**
     * Gets the color for an item type.
     * 
     * @param itemType The item type
     * @return The color for the item
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
     * 
     * @param itemType The item type
     * @return The abbreviation for the item
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
    
    /**
     * Sets the selected hotbar slot.
     * 
     * @param slot The slot to select (0-8)
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < HOTBAR_SLOTS) {
            this.selectedSlot = slot;
            // Trigger item name display
            this.itemNameDisplayStartTime = System.currentTimeMillis();
            this.showingItemName = true;
        }
    }
    
    /**
     * Sets the selected hotbar slot and displays the item name.
     * 
     * @param slot The slot to select (0-8)
     * @param inventory The player's inventory to get the item name from
     */
    public void setSelectedSlot(int slot, Inventory inventory) {
        if (slot >= 0 && slot < HOTBAR_SLOTS) {
            this.selectedSlot = slot;
            // Get the item name for display
            Item selectedItem = inventory.getItem(slot);
            if (selectedItem != null && selectedItem.getCount() > 0) {
                this.displayedItemName = selectedItem.getName();
            } else {
                this.displayedItemName = "Empty";
            }
            // Trigger item name display
            this.itemNameDisplayStartTime = System.currentTimeMillis();
            this.showingItemName = true;
        }
    }
    
    /**
     * Gets the currently selected hotbar slot.
     * 
     * @return The selected slot (0-8)
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    /**
     * Gets the selected item from the inventory.
     * 
     * @param inventory The player's inventory
     * @return The selected item, or null if no item is selected
     */
    public Item getSelectedItem(Inventory inventory) {
        return inventory.getItem(selectedSlot);
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
            // Add more texture mappings as needed
            // case Block.TYPE_WOOD:
            //     return TextureManager2D.getTexture2D("Wood.png");
            // case Block.TYPE_LEAVES:
            //     return TextureManager2D.getTexture2D("Leaves.png");
            default:
                return null; // No texture available, will use fallback rendering
        }
    }
}