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
     * @param player The player
     */
    public void render(Graphics2D g, Player player) {
        // Save the original font and color
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        
        // Set the font and color for the HUD
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        
        // Draw the player coordinates
        drawCoordinates(g, player);
        
        // Draw the player health
        drawHealth(g, player);
        
        // Draw the current w-slice
        drawWSlice(g, player);
        
        // Restore the original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
    }
    
    /**
     * Draws the player coordinates.
     * 
     * @param g The graphics context
     * @param player The player
     */
    private void drawCoordinates(Graphics2D g, Player player) {
        Vector4D position = player.getPosition();
        
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
     * Draws the player health.
     * 
     * @param g The graphics context
     * @param player The player
     */
    private void drawHealth(Graphics2D g, Player player) {
        // Get the player health
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        
        // Calculate the health percentage
        int healthPercentage = (int) (health / maxHealth * 100);
        
        // Draw the health bar in the top-right corner
        int barWidth = 150;
        int barHeight = 20;
        int x = width - barWidth - 10;
        int y = 20;
        
        // Draw the background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barWidth, barHeight);
        
        // Draw the health
        g.setColor(getHealthColor(healthPercentage));
        g.fillRect(x, y, (int) (barWidth * health / maxHealth), barHeight);
        
        // Draw the border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
        
        // Draw the text
        String healthText = "Health: " + healthPercentage + "%";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(healthText)) / 2;
        int textY = y + barHeight / 2 + fm.getAscent() / 2;
        g.drawString(healthText, textX, textY);
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
     * Gets the color for the health bar based on the health percentage.
     * 
     * @param percentage The health percentage
     * @return The color
     */
    private Color getHealthColor(int percentage) {
        if (percentage > 70) {
            return Color.GREEN;
        } else if (percentage > 30) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }
}