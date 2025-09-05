package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Player;
import com.adventure4d.computation.modules.Vector4D;

import java.awt.*;
import java.text.DecimalFormat;

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
    }
    
    /**
     * Renders the HUD.
     * 
     * @param g The graphics context
     * @param camera The camera
     * @param player The player
     */
    public void render(Graphics2D g, Camera camera, Player player) {
        render(g, camera, player, 0, 0);
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
    public void render(Graphics2D g, Camera camera, Player player, int mouseX, int mouseY) {
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
        
        // Draw controls information
        drawControls(g);
        
        // Draw hotbar
        hotbar.render(g, player.getInventory());
        
        // Update inventory UI mouse position and render
        inventoryUI.updateMousePosition(mouseX, mouseY);
        inventoryUI.render(g, player.getInventory(), hotbar);
        
        // W-slice bar removed for cleaner display
        
        // Draw mouse crosshair for debugging
        //drawMouseCrosshair(g, mouseX, mouseY);
        
        // Restore the original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
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
            "Right Click - Place Block",
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
     * Draws the current w-slice.
     * 
     * @param g The graphics context
     * @param player The player
     */
    private void drawWSlice(Graphics2D g, Player player) {
        // Get the player position
        Vector4D position = player.getPosition();
        
        // Draw the w-slice indicator at the bottom of the screen
        int barWidth = width - 100;
        int barHeight = 10;
        int x = 50;
        int y = height - 30;
        
        // Draw the background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barWidth, barHeight);
        
        // Draw the w-slice indicator
        g.setColor(Color.CYAN);
        int sliceX = x + (int) ((position.getW() + 10) / 20 * barWidth); // Assuming w ranges from -10 to 10
        g.fillRect(sliceX - 2, y - 5, 4, barHeight + 10);
        
        // Draw the border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
        
        // Draw the text
        String sliceText = "W-Slice: " + df.format(position.getW());
        g.drawString(sliceText, x, y - 10);
    }
    
    /**
     * Draws a crosshair at the mouse position for debugging.
     * 
     * @param g The graphics context
     * @param mouseX The mouse X coordinate
     * @param mouseY The mouse Y coordinate
     */
    private void drawMouseCrosshair(Graphics2D g, int mouseX, int mouseY) {
        if (mouseX == 0 && mouseY == 0) {
            return; // Don't draw if no mouse position is set
        }
        
        // Save original stroke and color
        Stroke originalStroke = g.getStroke();
        Color originalColor = g.getColor();
        
        // Set crosshair style
        g.setStroke(new BasicStroke(2));

        g.setColor(Color.RED);
        
        // Draw crosshair lines
        int crosshairSize = 10;
        
        // Horizontal line
        g.drawLine(mouseX - crosshairSize, mouseY, mouseX + crosshairSize, mouseY);
        
        // Vertical line
        g.drawLine(mouseX, mouseY - crosshairSize, mouseX, mouseY + crosshairSize);
        
        // Draw a small circle at the center
        g.drawOval(mouseX - 2, mouseY - 2, 4, 4);
        
        // Draw mouse coordinates text
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        String coordText = "Mouse: (" + mouseX + ", " + mouseY + ")";
        g.drawString(coordText, mouseX + 15, mouseY - 5);
        
        // Restore original stroke and color
        g.setStroke(originalStroke);
        g.setColor(originalColor);
    }
    

}