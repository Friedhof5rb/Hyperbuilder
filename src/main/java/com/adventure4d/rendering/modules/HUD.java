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
    }
    
    /**
     * Renders the HUD.
     * 
     * @param g The graphics context
     * @param camera The camera
     */
    public void render(Graphics2D g, Camera camera) {
        // Save the original font and color
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        
        // Set the font and color for the HUD
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        
        // Draw camera coordinates (world position)
        drawCoordinates(g, camera);
        
        // W-slice bar removed for cleaner display
        
        // Restore the original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
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
    

}