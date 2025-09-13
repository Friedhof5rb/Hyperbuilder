package me.friedhof.hyperbuilder.rendering.modules;

import java.awt.*;
import java.text.DecimalFormat;

import me.friedhof.hyperbuilder.computation.modules.Player;
import me.friedhof.hyperbuilder.computation.modules.Vector4D;

/**
 * Heads-Up Display for showing player information.
 */
public class HUD {
    // Dimensions
    private final int width;
    private final int height;
    
    // Formatting for coordinates
    private final DecimalFormat df;
    
    // Hotbar component
    private final Hotbar hotbar;
    
    // Inventory UI component
    private final InventoryUI inventoryUI;
    
    // Smelter GUI component
    private final SmelterGUI smelterGUI;
    
    // FPS tracking
    private int currentFPS = 0;
    
    /**
     * Creates a new HUD with the specified dimensions.
     * 
     * @param width The width of the display
     * @param height The height of the display
     */
    public HUD(int width, int height) {
        this.width = width;
        this.height = height;
        
        // Format coordinates to 2 decimal places
        this.df = new DecimalFormat("0.00");
        
        // Initialize hotbar
        this.hotbar = new Hotbar(width, height);
        
        // Initialize inventory UI
        this.inventoryUI = new InventoryUI(width, height);
        
        // Initialize smelter GUI
        this.smelterGUI = new SmelterGUI(width, height, inventoryUI);
    }
    
    /**
     * Renders the HUD.
     * 
     * @param g The graphics context
     * @param camera The camera
     * @param player The player
     * @param mouseX The current mouse X coordinate
     * @param mouseY The current mouse Y coordinate
     */
    public void render(Graphics2D g, Camera camera, Player player, int mouseX, int mouseY, me.friedhof.hyperbuilder.Game game) {
        // Save the original font and color
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        
        // Set the font and color for the HUD
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        
        // Draw camera coordinates (world position)
        drawCoordinates(g, camera);
        
        // Draw dimension information
        drawDimensionInfo(g, camera);
        
        // Draw direction indicator
        drawDirectionIndicator(g, camera);
        
        // Draw slice size information
        drawSliceSizeInfo(g);
        
        // Draw controls information
        drawControls(g);
        
        // Render hotbar only if inventory is not visible
        if (!inventoryUI.isVisible()) {
            hotbar.render(g, player.getInventory());
        }
        
        // Update inventory UI mouse position and render
        inventoryUI.updateMousePosition(mouseX, mouseY);
        inventoryUI.render(g, player.getInventory(), hotbar, smelterGUI.isVisible());
        
        // Update smelter GUI mouse position, update logic, and render
        smelterGUI.updateMousePosition(mouseX, mouseY);
        smelterGUI.update(System.currentTimeMillis());
        smelterGUI.render(g);
        
        // Draw block breaking progress if breaking a block
        if (game.isBreakingBlock()) {
            drawBlockBreakingProgress(g, game.getBreakingProgress(), game);
        }
        
        // Draw dragged item on top of everything (if any)
        if (inventoryUI.getDraggedItem() != null) {
            drawDraggedItem(g, inventoryUI.getDraggedItem(), mouseX, mouseY);
        }
        
        // Restore the original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
    }
    
    /**
     * Draws the dragged item at the specified coordinates.
     */
    private void drawDraggedItem(Graphics2D g, me.friedhof.hyperbuilder.computation.modules.items.BaseItem item, int x, int y) {
        if (item != null) {
            inventoryUI.renderDraggedItem(g, x, y);
        }
    }
    
    /**
     * Gets the hotbar component.
     * 
     * @return The hotbar
     */
    public Hotbar getHotbar() {
        return hotbar;
    }
    
    /**
     * Gets the inventory UI component.
     * 
     * @return The inventory UI
     */
    public InventoryUI getInventoryUI() {
        return inventoryUI;
    }
    
    /**
     * Gets the smelter GUI component.
     * 
     * @return The smelter GUI
     */
    public SmelterGUI getSmelterGUI() {
        return smelterGUI;
    }
    
    /**
     * Updates the FPS counter.
     * 
     * @param fps The current frames per second
     */
    public void updateFPS(int fps) {
        this.currentFPS = fps;
    }
    
    /**
     * Updates the HUD dimensions and recalculates component bounds.
     * 
     * @param width The new width
     * @param height The new height
     */
    public void updateDimensions(int width, int height) {
        inventoryUI.updateDimensions(width, height);
        hotbar.updateDimensions(width, height);
        smelterGUI.updateScreenSize(width, height);
    }

    /**
     * Draws the camera coordinates (world position).
     * 
     * @param g The graphics context
     * @param camera The camera
     */
    private void drawCoordinates(Graphics2D g, Camera camera) {
        Vector4D position = camera.getWorldOffset();
        
        // Create the coordinate strings
        String xCoord = "X: " + df.format(position.getX());
        String yCoord = "Y: " + df.format(position.getY());
        String zCoord = "Z: " + df.format(position.getZ());
        String wCoord = "W: " + df.format(position.getW());
        
        // Draw the coordinates in the top-left corner
        int x = 10;
        int y = 20;
        int lineHeight = 20;
        
        g.drawString(xCoord, x, y);
        g.drawString(yCoord, x, y + lineHeight);
        g.drawString(zCoord, x, y + 2 * lineHeight);
        g.drawString(wCoord, x, y + 3 * lineHeight);
        
        // Draw FPS counter below coordinates
        String fpsText = "FPS: " + currentFPS;
        g.drawString(fpsText, x, y + 4 * lineHeight);
    }
    
    /**
     * Draws dimension cycling information.
     * 
     * @param g The graphics context
     * @param camera The camera
     */
    private void drawDimensionInfo(Graphics2D g, Camera camera) {
        Camera.HorizontalDimension current = camera.getHorizontalDimension();
        Camera.HorizontalDimension next = current.getNext();
        Camera.HorizontalDimension previous = current.getPrevious();
        
        // Draw dimension info in the top-right corner
        int rightMargin = 10;
        int y = 20;
        
        String currentDimText = next.getDisplayName() + " -> (" + current.getDisplayName() + ")-> " + previous.getDisplayName();
       
        
        // Calculate x position to right-align the text
        FontMetrics fm = g.getFontMetrics();
        int currentWidth = fm.stringWidth(currentDimText);
       
        
        int currentX = width - rightMargin - currentWidth;
        
        g.drawString(currentDimText, currentX, y);
       
    }
    
    /**
     * Draws the direction indicator showing current coordinate axes.
     * 
     * @param g The graphics context
     * @param camera The camera
     */
    private void drawDirectionIndicator(Graphics2D g, Camera camera) {
        Camera.HorizontalDimension current = camera.getHorizontalDimension();
        
        // Position below the mode indicator
        int rightMargin = 10;
        int startY = 50; // Below the mode line
        
        // Save original settings
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        
        // Set font for labels
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        
        // Determine axis labels based on current mode
        String sliceHorizontalLabel, sliceVerticalLabel, gridHorizontalLabel, gridVerticalLabel;
        switch (current) {
            case X:
                // X mode: viewing X-Y plane, grid represents Z and W
                sliceHorizontalLabel = "X";
                sliceVerticalLabel = "Y";
                gridHorizontalLabel = "Z";
                gridVerticalLabel = "W";
                break;
            case Z:
                // Z mode: viewing Z-Y plane, grid represents X and W
                sliceHorizontalLabel = "Z";
                sliceVerticalLabel = "Y";
                gridHorizontalLabel = "X";
                gridVerticalLabel = "W";
                break;
            case W:
                // W mode: viewing W-Y plane, grid represents X and Z
                sliceHorizontalLabel = "W";
                sliceVerticalLabel = "Y";
                gridHorizontalLabel = "X";
                gridVerticalLabel = "Z";
                break;
            default:
                sliceHorizontalLabel = "X";
                sliceVerticalLabel = "Y";
                gridHorizontalLabel = "Z";
                gridVerticalLabel = "W";
                break;
        }
        
        // Small coordinate system (slice coordinates)
        int smallArrowLength = 20;
        int smallCenterX = width - rightMargin - 100;
        int smallCenterY = startY + 35;
        
        // Draw small horizontal arrow (right)
        g.drawLine(smallCenterX, smallCenterY, smallCenterX + smallArrowLength, smallCenterY);
        g.drawLine(smallCenterX + smallArrowLength, smallCenterY, smallCenterX + smallArrowLength - 4, smallCenterY - 2);
        g.drawLine(smallCenterX + smallArrowLength, smallCenterY, smallCenterX + smallArrowLength - 4, smallCenterY + 2);
        g.drawString(sliceHorizontalLabel, smallCenterX + smallArrowLength + 3, smallCenterY + 4);
        
        // Draw small vertical arrow (up)
        g.drawLine(smallCenterX, smallCenterY, smallCenterX, smallCenterY - smallArrowLength);
        g.drawLine(smallCenterX, smallCenterY - smallArrowLength, smallCenterX - 2, smallCenterY - smallArrowLength + 4);
        g.drawLine(smallCenterX, smallCenterY - smallArrowLength, smallCenterX + 2, smallCenterY - smallArrowLength + 4);
        g.drawString(sliceVerticalLabel, smallCenterX - 10, smallCenterY - smallArrowLength - 3);
        
        // Large coordinate system (grid coordinates) - positioned to the left and below
        int largeArrowLength = 35;
        int largeCenterX = width - rightMargin - 120;
        int largeCenterY = startY + 60;
        
        // Draw large horizontal arrow (right)
        g.drawLine(largeCenterX, largeCenterY, largeCenterX + largeArrowLength, largeCenterY);
        g.drawLine(largeCenterX + largeArrowLength, largeCenterY, largeCenterX + largeArrowLength - 6, largeCenterY - 3);
        g.drawLine(largeCenterX + largeArrowLength, largeCenterY, largeCenterX + largeArrowLength - 6, largeCenterY + 3);
        g.drawString(gridHorizontalLabel, largeCenterX + largeArrowLength + 5, largeCenterY + 4);
        
        // Draw large vertical arrow (up)
        g.drawLine(largeCenterX, largeCenterY, largeCenterX, largeCenterY - largeArrowLength);
        g.drawLine(largeCenterX, largeCenterY - largeArrowLength, largeCenterX - 3, largeCenterY - largeArrowLength + 6);
        g.drawLine(largeCenterX, largeCenterY - largeArrowLength, largeCenterX + 3, largeCenterY - largeArrowLength + 6);
        g.drawString(gridVerticalLabel, largeCenterX - 12, largeCenterY - largeArrowLength - 5);
        
        // Restore original settings
        g.setFont(originalFont);
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }
    
    /**
     * Draws the slice size information.
     * 
     * @param g The graphics context
     */
    private void drawSliceSizeInfo(Graphics2D g) {
        // Position on the left side, below coordinates
        int leftMargin = 20;
        int x = leftMargin;
        int startY = 120; // Below coordinates info
        
        // Get current slice size
        int sliceSize = SliceRenderer.getSliceSize();
        
        // Draw slice size info
        String sliceSizeText = "Slice Size: " + sliceSize + "x" + sliceSize;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(sliceSizeText);
        
        // Draw semi-transparent background
        Color originalColor = g.getColor();
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(x - 5, startY - 15, textWidth + 15, 25);
        
        // Draw the text
        g.setColor(Color.CYAN);
        g.drawString(sliceSizeText, x, startY);
        
        // Draw zoom controls hint
        String zoomHint = "Ctrl+Scroll: Zoom";
        int hintWidth = fm.stringWidth(zoomHint);
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(x - 5, startY + 10, hintWidth + 15, 20);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(zoomHint, x, startY + 25);
        
        // Restore original color
        g.setColor(originalColor);
    }
    
    /**
     * Draws the game controls information.
     * 
     * @param g The graphics context
     */
    private void drawControls(Graphics2D g) {
        // Set smaller font for controls
        Font originalFont = g.getFont();
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Controls text
        String[] controls = {
            "CONTROLS:",
            "WS - Move (W Axis) ",
            "AD - Move (X Axis)",
            "QE - Move (Z Axis)",
            "SPACE - Jump (Y Axis)",
            "TAB - Cycle View Dimension",
            "1-9 - Select Hotbar Slot",
            "Left Click - Destroy Block",
            "Right Click - Place Block/Use",
            "I - Inventory",
            "C - Drop Item",
            "ESC - Exit Game"
        };
        
        // Position controls in the bottom-left corner
        int x = 10;
        int startY = height - (controls.length * 15) - 20;
        int lineHeight = 15;
        
        // Draw semi-transparent background
        Color originalColor = g.getColor();
        g.setColor(new Color(0, 0, 0, 128)); // Semi-transparent black
        FontMetrics fm = g.getFontMetrics();
        
        // Find the widest line for background width
        int maxWidth = 0;
        for (String control : controls) {
            int width = fm.stringWidth(control);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        
        // Draw background rectangle
        g.fillRect(x - 5, startY - 15, maxWidth + 10, controls.length * lineHeight + 10);
        
        // Draw controls text
        g.setColor(Color.WHITE);
        for (int i = 0; i < controls.length; i++) {
            if (i == 0) {
                // Make the title slightly brighter
                g.setColor(Color.YELLOW);
                g.drawString(controls[i], x, startY + i * lineHeight);
                g.setColor(Color.WHITE);
            } else {
                g.drawString(controls[i], x, startY + i * lineHeight);
            }
        }
        
        // Restore original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
    }
    
    /**
     * Draws a circular progress bar for block breaking.
     * 
     * @param g The graphics context
     * @param progress The breaking progress (0.0 to 1.0)
     * @param game The game instance to get breaking block position
     */
    private void drawBlockBreakingProgress(Graphics2D g, float progress, me.friedhof.hyperbuilder.Game game) {
        // Get the world position of the breaking block
        me.friedhof.hyperbuilder.computation.modules.Vector4DInt breakingBlockPos = game.getBreakingBlockPosition();
        if (breakingBlockPos == null) {
            return; // No block being broken
        }
        
        // Convert world coordinates to screen coordinates
        java.awt.Point screenPos = game.worldToScreenCoordinates(breakingBlockPos);
        if (screenPos == null) {
            return; // Block not visible on screen
        }
        
        int centerX = screenPos.x;
        int centerY = screenPos.y;
        
        if (progress <= 0.0f) {
            return; // Don't draw anything if no progress
        }
        
        // Save original rendering hints and stroke
        RenderingHints originalHints = g.getRenderingHints();
        Stroke originalStroke = g.getStroke();
        Color originalColor = g.getColor();
        
        // Enable anti-aliasing for smooth circles
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int radius = 30;
        
        // Set stroke width for the progress ring
        g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw background circle (semi-transparent dark)
        g.setColor(new Color(0, 0, 0, 100));
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Calculate the arc angle based on progress (0-360 degrees)
        int arcAngle = (int) (progress * 360);
        
        // Draw progress arc (starting from top, going clockwise)
        // Color changes from red to yellow to green based on progress
        Color progressColor;
        if (progress < 0.5f) {
            // Red to yellow (0.0 to 0.5)
            float ratio = progress * 2.0f;
            progressColor = new Color(255, (int) (255 * ratio), 0, 200);
        } else {
            // Yellow to green (0.5 to 1.0)
            float ratio = (progress - 0.5f) * 2.0f;
            progressColor = new Color((int) (255 * (1.0f - ratio)), 255, 0, 200);
        }
        
        g.setColor(progressColor);
        g.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -arcAngle);
        
        // Draw a small center dot
        g.setColor(new Color(255, 255, 255, 150));
        int dotRadius = 3;
        g.fillOval(centerX - dotRadius, centerY - dotRadius, dotRadius * 2, dotRadius * 2);
        
        // Restore original rendering settings
        g.setRenderingHints(originalHints);
        g.setStroke(originalStroke);
        g.setColor(originalColor);
    }

}