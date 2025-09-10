package me.friedhof.hyperbuilder.rendering.modules;

import java.awt.*;
import java.awt.event.MouseEvent;
import me.friedhof.hyperbuilder.computation.modules.Inventory;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import me.friedhof.hyperbuilder.computation.modules.World;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.SmelterItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.SmelterPoweredItem;
import me.friedhof.hyperbuilder.computation.modules.SmelterInventory;
import me.friedhof.hyperbuilder.computation.modules.SmelterRecipe;
import me.friedhof.hyperbuilder.rendering.modules.InventoryUI;

/**
 * GUI component for the smelter interface.
 * Displays input slot, output slot, and processing progress bar.
 */
public class SmelterGUI {
    // UI Configuration
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_PADDING = 8;
    private static final int GUI_PADDING = 20;
    private static final int PROGRESS_BAR_WIDTH = 24;
    private static final int PROGRESS_BAR_HEIGHT = 16;
    
    // Colors
    private static final Color BACKGROUND_COLOR = new Color(139, 69, 19, 220); // Brown background
    private static final Color SLOT_BACKGROUND = new Color(192, 192, 192, 220);
    private static final Color SLOT_BORDER = new Color(128, 128, 128);
    private static final Color HOVER_BORDER = new Color(255, 255, 255);
    private static final Color PROGRESS_BAR_BACKGROUND = new Color(64, 64, 64);
    private static final Color PROGRESS_BAR_FILL = new Color(255, 165, 0); // Orange
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color COUNT_COLOR = new Color(255, 255, 0);
    
    // Tooltip colors
    private static final Color TOOLTIP_BACKGROUND = new Color(0, 0, 0, 200);
    private static final Color TOOLTIP_BORDER = new Color(255, 255, 255, 150);
    private static final Color TOOLTIP_TEXT = Color.WHITE;
    
    // State
    private boolean visible = false;
    private int screenWidth;
    private int screenHeight;
    
    // GUI bounds
    private int guiX;
    private int guiY;
    private int guiWidth;
    private int guiHeight;
    
    // Slot positions
    private int inputSlotX;
    private int inputSlotY;
    private int outputSlotX;
    private int outputSlotY;
    private int progressBarX;
    private int progressBarY;
    
    // Mouse interaction
    private int mouseX = 0;
    private int mouseY = 0;
    private int hoveredSlot = -1; // -1: none, 0: input, 1: output
    
    // Reference to inventory UI for drag and drop
    private InventoryUI inventoryUI;
    
    private Block smelterBlock;
    private World world;
    private Vector4DInt smelterPosition;
    
    public SmelterGUI(int screenWidth, int screenHeight, InventoryUI inventoryUI) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.inventoryUI = inventoryUI;
        calculateGUIBounds();
    }
    
    /**
     * Sets the smelter block reference for this GUI.
     */
    public void setSmelterBlock(World world, Vector4DInt position) {
        this.world = world;
        this.smelterPosition = position;
        this.smelterBlock = world.getBlock(position);
    }
    
    /**
     * Calculates the GUI bounds and slot positions.
     */
    private void calculateGUIBounds() {
        // Calculate GUI size (2 slots + progress bar + padding)
        guiWidth = 3 * SLOT_SIZE + 2 * SLOT_PADDING + 2 * GUI_PADDING;
        guiHeight = SLOT_SIZE + 2 * GUI_PADDING + 30; // +30 for title
        
        // Position on the right side of the screen, aligned with inventory (same as CraftingUI)
        int inventoryWidth = 9 * (SLOT_SIZE + 8) + 2 * GUI_PADDING; // Approximate inventory width
        guiX = (screenWidth + inventoryWidth) / 2 + 20; // 20px gap from inventory
        guiY = (screenHeight - guiHeight) / 2 - 50;
        
        // Calculate slot positions
        inputSlotX = guiX + GUI_PADDING;
        inputSlotY = guiY + GUI_PADDING + 25; // Account for title
        
        outputSlotX = inputSlotX + 2 * SLOT_SIZE + 2 * SLOT_PADDING;
        outputSlotY = inputSlotY;
        
        // Progress bar between slots
        progressBarX = inputSlotX + SLOT_SIZE + SLOT_PADDING + (SLOT_SIZE - PROGRESS_BAR_WIDTH) / 2;
        progressBarY = inputSlotY + (SLOT_SIZE - PROGRESS_BAR_HEIGHT) / 2;
    }
    
    /**
     * Renders the smelter GUI.
     */
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Save original settings
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        
        // Draw GUI background
        g.setColor(BACKGROUND_COLOR);
        g.fillRoundRect(guiX, guiY, guiWidth, guiHeight, 10, 10);
        
        // Draw GUI border
        g.setColor(SLOT_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(guiX, guiY, guiWidth, guiHeight, 10, 10);
        
        // Draw title
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics titleFm = g.getFontMetrics();
        String title = "Smelter";
        int titleX = guiX + (guiWidth - titleFm.stringWidth(title)) / 2;
        int titleY = guiY + titleFm.getAscent() + 5;
        g.drawString(title, titleX, titleY);
        
        // Get inventory from smelter block
        SmelterInventory inventory = getSmelterInventory();
        BaseItem inputItem = inventory != null ? inventory.getInputItem() : null;
        BaseItem outputItem = inventory != null ? inventory.getOutputItem() : null;
        
        // Draw input slot
        drawSlot(g, inputSlotX, inputSlotY, inputItem, hoveredSlot == 0);
        
        // Draw output slot
        drawSlot(g, outputSlotX, outputSlotY, outputItem, hoveredSlot == 1);
        
        // Draw progress bar
        drawProgressBar(g);
        
        // Draw arrow from input to output
        drawArrow(g);
        
        // Draw tooltip if hovering over an item
        drawTooltip(g, inventory, inputItem, outputItem);
        
        // Restore original settings
        g.setFont(originalFont);
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }
    
    /**
     * Draws a single slot.
     */
    private void drawSlot(Graphics2D g, int x, int y, BaseItem item, boolean hovered) {
        // Draw slot background
        g.setColor(SLOT_BACKGROUND);
        g.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);
        
        // Draw slot border
        g.setColor(hovered ? HOVER_BORDER : SLOT_BORDER);
        g.setStroke(new BasicStroke(hovered ? 2 : 1));
        g.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);
        
        // Draw item if present
        if (item != null && item.getCount() > 0) {
            drawItem(g, x, y, item, false);
        }
    }
    
    /**
     * Draws an item in a slot.
     */
    private void drawItem(Graphics2D g, int x, int y, BaseItem item, boolean transparent) {
        // Get item texture
        Texture2D texture = ItemRegistry.getItemTexture(item.getItemId());
        if (texture != null) {
            // Draw item texture
            if (transparent) {
                Composite oldComposite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g.drawImage(texture.getImage(), x + 2, y + 2, SLOT_SIZE - 4, SLOT_SIZE - 4, null);
                g.setComposite(oldComposite);
            } else {
                g.drawImage(texture.getImage(), x + 2, y + 2, SLOT_SIZE - 4, SLOT_SIZE - 4, null);
            }
        } else {
            // Fallback: draw colored rectangle
            g.setColor(Color.GRAY);
            g.fillRect(x + 4, y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
        }
        
        // Draw item count if > 1
        if (item.getCount() > 1) {
            g.setColor(COUNT_COLOR);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String countStr = String.valueOf(item.getCount());
            FontMetrics fm = g.getFontMetrics();
            int textX = x + SLOT_SIZE - fm.stringWidth(countStr) - 2;
            int textY = y + SLOT_SIZE - 2;
            g.drawString(countStr, textX, textY);
        }
    }
    
    /**
     * Draws the progress bar.
     */
    private void drawProgressBar(Graphics2D g) {
        // Draw progress bar background
        g.setColor(PROGRESS_BAR_BACKGROUND);
        g.fillRect(progressBarX, progressBarY, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        
        // Draw progress bar border
        g.setColor(SLOT_BORDER);
        g.drawRect(progressBarX, progressBarY, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        
        // Draw progress fill
        float progress = getProcessingProgress();
        if (progress > 0) {
            g.setColor(PROGRESS_BAR_FILL);
            int fillWidth = (int) (PROGRESS_BAR_WIDTH * progress);
            g.fillRect(progressBarX + 1, progressBarY + 1, fillWidth - 1, PROGRESS_BAR_HEIGHT - 2);
        }
    }
    
    /**
     * Draws an arrow from input to output.
     */
    private void drawArrow(Graphics2D g) {
        g.setColor(TEXT_COLOR);
        g.setStroke(new BasicStroke(2));
        
        int arrowY = inputSlotY + SLOT_SIZE / 2;
        int arrowStartX = inputSlotX + SLOT_SIZE + 4;
        int arrowEndX = outputSlotX - 4;
        
        // Draw arrow line
        g.drawLine(arrowStartX, arrowY, arrowEndX, arrowY);
        
        // Draw arrow head
        int[] arrowHeadX = {arrowEndX, arrowEndX - 6, arrowEndX - 6};
        int[] arrowHeadY = {arrowY, arrowY - 3, arrowY + 3};
        g.fillPolygon(arrowHeadX, arrowHeadY, 3);
    }
    
    /**
     * Draws tooltip for hovered items.
     */
    private void drawTooltip(Graphics2D g, SmelterInventory inventory, BaseItem inputItem, BaseItem outputItem) {
        if (hoveredSlot == -1) return;
        
        BaseItem hoveredItem = null;
        if (hoveredSlot == 0 && inputItem != null && inputItem.getCount() > 0) {
            hoveredItem = inputItem;
        } else if (hoveredSlot == 1 && outputItem != null && outputItem.getCount() > 0) {
            hoveredItem = outputItem;
        }
        
        if (hoveredItem == null) return;
        
        // Get item name
        String itemName = hoveredItem.getDisplayName();
        if (itemName == null || itemName.isEmpty()) {
            itemName = hoveredItem.getItemId().toString();
        }
        
        // Calculate tooltip size
        Font tooltipFont = new Font("Arial", Font.PLAIN, 12);
        g.setFont(tooltipFont);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(itemName);
        int textHeight = fm.getHeight();
        
        int tooltipWidth = textWidth + 8;
        int tooltipHeight = textHeight + 4;
        
        // Position tooltip near mouse, but keep it on screen
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - tooltipHeight - 5;
        
        // Adjust if tooltip would go off screen
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 10;
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 20;
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
        int textX = tooltipX + 4;
        int textY = tooltipY + fm.getAscent() + 2;
        g.drawString(itemName, textX, textY);
    }
    
    /**
     * Updates mouse position and hovered slot.
     */
    public void updateMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        updateHoveredSlot(x, y);
    }
    
    /**
     * Updates which slot is being hovered.
     */
    private void updateHoveredSlot(int x, int y) {
        hoveredSlot = -1;
        
        if (!visible) return;
        
        // Check input slot
        if (x >= inputSlotX && x < inputSlotX + SLOT_SIZE &&
            y >= inputSlotY && y < inputSlotY + SLOT_SIZE) {
            hoveredSlot = 0;
        }
        // Check output slot
        else if (x >= outputSlotX && x < outputSlotX + SLOT_SIZE &&
                 y >= outputSlotY && y < outputSlotY + SLOT_SIZE) {
            hoveredSlot = 1;
        }
    }
    
    /**
     * Handles mouse click events.
     */
    public boolean handleMouseClick(int x, int y, int button, Inventory playerInventory, InventoryUI inventoryUI) {
        if (!visible) return false;
        
        updateHoveredSlot(x, y);
        
        if (hoveredSlot == -1) return false;
        
        if (button == MouseEvent.BUTTON1) { // Left click
            return handleLeftClick(playerInventory, inventoryUI);
        } else if (button == MouseEvent.BUTTON3) { // Right click
            return handleRightClick(playerInventory);
        }
        
        return false;
    }
    
    /**
     * Handles left click for drag and drop.
     */
    private boolean handleLeftClick(Inventory playerInventory, InventoryUI inventoryUI) {
        // Check if inventory UI has a dragged item to drop into smelter
        BaseItem inventoryDraggedItem = inventoryUI.getDraggedItem();
        if (inventoryDraggedItem != null) {
            // Try to drop the inventory item into the smelter
            return dropInventoryItem(inventoryDraggedItem, inventoryUI);
        }
        
        // No item being dragged, try to pick up item from smelter slot
        SmelterInventory inventory = getSmelterInventory();
        if (inventory == null) return false;
        
        BaseItem item = hoveredSlot == 0 ? inventory.getInputItem() : inventory.getOutputItem();
        if (item != null && item.getCount() > 0) {
            // Start dragging this item in the inventory UI
            inventoryUI.setDraggedItem(item);
            
            // Remove item from smelter slot
            if (hoveredSlot == 0) {
                inventory.setInputItem(null);
                // Stop processing if input slot is now empty
                stopProcessingIfInputEmpty();
            } else {
                inventory.setOutputItem(null);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Handles right click for single item transfer.
     */
    private boolean handleRightClick(Inventory playerInventory) {
        // Check if inventory UI has a dragged item to drop single item into smelter
        BaseItem inventoryDraggedItem = inventoryUI.getDraggedItem();
        if (inventoryDraggedItem != null && inventoryDraggedItem.getCount() > 0) {
            return dropSingleInventoryItem(inventoryDraggedItem, inventoryUI);
        }
        
        // Check if right-clicking on output slot to extract single item
        if (hoveredSlot == 1) {
            return extractSingleItemFromOutput(playerInventory);
        }
        
        return false;
    }
    
    /**
     * Drops a single item from the inventory UI dragged stack into the smelter.
     */
    private boolean dropSingleInventoryItem(BaseItem inventoryDraggedItem, InventoryUI inventoryUI) {
        SmelterInventory inventory = getSmelterInventory();
        if (inventory == null) return false;
        
        // Only allow dropping into input slot (slot 0)
        if (hoveredSlot == 0) {
            // Check if item can be smelted
            if (canSmelt(inventoryDraggedItem)) {
                BaseItem targetItem = inventory.getInputItem();
                
                if (targetItem == null) {
                    // Empty slot - place single item
                    inventory.setInputItem(inventoryDraggedItem.withCount(1));
                    inventoryUI.updateDraggedItemCount(inventoryDraggedItem.getCount() - 1);
                    // Try to start processing automatically
                    tryStartProcessing();
                    return true;
                } else if (targetItem.getItemId() == inventoryDraggedItem.getItemId()) {
                    // Same item - try to add one to stack
                    if (targetItem.getCount() < targetItem.getMaxStackSize()) {
                        inventory.setInputItem(targetItem.withCount(targetItem.getCount() + 1));
                        inventoryUI.updateDraggedItemCount(inventoryDraggedItem.getCount() - 1);
                        // Try to start processing automatically
                        tryStartProcessing();
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Extracts a single item from the smelter output slot into player inventory.
     */
    private boolean extractSingleItemFromOutput(Inventory playerInventory) {
        SmelterInventory inventory = getSmelterInventory();
        if (inventory == null) return false;
        
        BaseItem outputItem = inventory.getOutputItem();
        if (outputItem == null || outputItem.getCount() <= 0) return false;
        
        // Try to add one item to player inventory
        BaseItem singleItem = outputItem.withCount(1);
        if (playerInventory.addItem(singleItem)) {
            // Successfully added to player inventory, remove from smelter
            if (outputItem.getCount() == 1) {
                // Last item, clear the slot
                inventory.setOutputItem(null);
            } else {
                // Reduce count by 1
                inventory.setOutputItem(outputItem.withCount(outputItem.getCount() - 1));
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Drops an item from the inventory UI into the smelter.
     */
    private boolean dropInventoryItem(BaseItem inventoryDraggedItem, InventoryUI inventoryUI) {
        SmelterInventory inventory = getSmelterInventory();
        if (inventory == null) return false;
        
        // Only allow dropping into input slot (slot 0)
        if (hoveredSlot == 0) {
            // Check if item can be smelted
            if (canSmelt(inventoryDraggedItem)) {
                BaseItem targetItem = inventory.getInputItem();
                
                if (targetItem == null) {
                    // Empty slot - place item
                    inventory.setInputItem(inventoryDraggedItem);
                    inventoryUI.clearDraggedItem();
                    // Try to start processing automatically
                    tryStartProcessing();
                    return true;
                } else if (targetItem.getItemId() == inventoryDraggedItem.getItemId()) {
                    // Same item - try to stack
                    int totalCount = targetItem.getCount() + inventoryDraggedItem.getCount();
                    if (totalCount <= targetItem.getMaxStackSize()) {
                        inventory.setInputItem(targetItem.withCount(totalCount));
                        inventoryUI.clearDraggedItem();
                        // Try to start processing automatically
                        tryStartProcessing();
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    

    
    /**
     * Checks if an item can be smelted.
     */
    private boolean canSmelt(BaseItem item) {
        return SmelterRecipe.canSmelt(item);
    }
    
    /**
     * Attempts to start processing on the smelter if conditions are met.
     * Only powered smelters automatically start processing.
     */
    private void tryStartProcessing() {
        // Only powered smelters should automatically start processing
        if (smelterBlock instanceof SmelterPoweredItem) {
            ((SmelterPoweredItem) smelterBlock).startProcessing();
        }
        // Regular smelters require manual activation (no automatic processing)
    }
    
    /**
     * Stops processing if the input slot is empty.
     */
    private void stopProcessingIfInputEmpty() {
        SmelterInventory inventory = getSmelterInventory();
        if (inventory != null && inventory.getInputItem() == null) {
            // Stop processing for both regular and powered smelters
            if (smelterBlock instanceof SmelterItem) {
                ((SmelterItem) smelterBlock).setProcessing(false);
            } else if (smelterBlock instanceof SmelterPoweredItem) {
                ((SmelterPoweredItem) smelterBlock).setProcessing(false);
            }
        }
    }
    
    /**
     * Updates processing progress by delegating to the smelter block.
     */
    public void update(long currentTime) {
        if (!visible || smelterBlock == null) return;
        
        // Update the smelter block's processing logic
        if (smelterBlock instanceof SmelterItem) {
            ((SmelterItem) smelterBlock).update();
        } else if (smelterBlock instanceof SmelterPoweredItem) {
            boolean stillPowered = ((SmelterPoweredItem) smelterBlock).update();
            
            // If power expired, convert back to regular smelter with preserved inventory
            if (!stillPowered) {
                convertPoweredSmelterToRegular();
            }
        }
    }
    
    /**
     * Converts a powered smelter back to a regular smelter when power expires,
     * preserving the inventory contents.
     */
    private void convertPoweredSmelterToRegular() {
        if (world == null || smelterPosition == null || !(smelterBlock instanceof SmelterPoweredItem)) {
            return;
        }
        
        // Get the current inventory to preserve it
        SmelterInventory currentInventory = ((SmelterPoweredItem) smelterBlock).getInventory();
        
        // Create new regular smelter with preserved inventory
        Block regularSmelter = new SmelterItem(1, currentInventory);
        
        // Replace the block in the world
        world.setBlock(smelterPosition, regularSmelter);
        
        // Update our reference
        this.smelterBlock = regularSmelter;
        
        System.out.println("Smelter power expired, converted back to regular smelter!");
    }
    
    /**
     * Gets the smelter inventory from the block.
     */
    private SmelterInventory getSmelterInventory() {
        if (smelterBlock instanceof SmelterItem) {
            return ((SmelterItem) smelterBlock).getInventory();
        } else if (smelterBlock instanceof SmelterPoweredItem) {
            return ((SmelterPoweredItem) smelterBlock).getInventory();
        }
        return null;
    }
    
    /**
     * Gets the processing progress from the smelter block.
     */
    private float getProcessingProgress() {
        if (smelterBlock instanceof SmelterItem) {
            return ((SmelterItem) smelterBlock).getProcessingProgress();
        } else if (smelterBlock instanceof SmelterPoweredItem) {
            return ((SmelterPoweredItem) smelterBlock).getProcessingProgress();
        }
        return 0.0f;
    }
    
    // Getters and setters
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public void toggleVisibility() {
        setVisible(!visible);
    }
    

    
    public boolean isWithinBounds(int x, int y) {
        return x >= guiX && x < guiX + guiWidth && y >= guiY && y < guiY + guiHeight;
    }
    
}