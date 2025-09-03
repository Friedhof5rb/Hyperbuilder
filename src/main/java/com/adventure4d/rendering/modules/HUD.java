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
        
        // Draw hotbar
        hotbar.render(g, player.getInventory());
        
        // W-slice bar removed for cleaner display
        
        // Draw mouse crosshair for debugging
        drawMouseCrosshair(g, mouseX, mouseY);
        
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