package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Vector4D;
import com.adventure4d.computation.modules.World;
import com.adventure4d.computation.modules.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main renderer class that handles all rendering operations.
 * Implements the "2D grid of 2D grids" rendering approach.
 */
public class Renderer {
    // The rendering canvas
    private JFrame frame;
    private BufferedImage buffer;
    private Graphics2D graphics;
    
    // Dimensions
    private int width;
    private int height;
    
    // Grid renderer
    private GridRenderer gridRenderer;
    
    // HUD elements
    private HUD hud;
    
    /**
     * Creates a new renderer with the specified dimensions.
     * 
     * @param width The width of the rendering window
     * @param height The height of the rendering window
     * @param title The title of the window
     */
    public Renderer(int width, int height, String title) {
        this.width = width;
        this.height = height;
        
        // Create the frame
        frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        // Create the buffer
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = buffer.createGraphics();
        
        // Create a panel to display the buffer
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(buffer, 0, 0, null);
            }
        };
        panel.setPreferredSize(new Dimension(width, height));
        frame.add(panel);
        
        // Create the grid renderer
        gridRenderer = new GridRenderer();
        
        // Create the HUD
        hud = new HUD(width, height);
        
        // Pack and center the frame
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Shows the rendering window.
     */
    public void show() {
        frame.setVisible(true);
    }
    
    /**
     * Gets the JFrame used by this renderer.
     * 
     * @return The JFrame instance
     */
    public JFrame getFrame() {
        return frame;
    }
    
    /**
     * Renders the world using the 2D grid of 2D grids approach.
     * 
     * @param world The world to render
     * @param player The player
     */
    public void render(World world, Player player) {
        // Clear the buffer
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        
        // Render the grid of slices
        BufferedImage gridImage = gridRenderer.renderGrid(world, player.getPosition());
        
        // Calculate the position to center the grid
        int gridX = (width - GridRenderer.getGridSizePixels()) / 2;
        int gridY = (height - GridRenderer.getGridSizePixels()) / 2;
        
        // Draw the grid
        graphics.drawImage(gridImage, gridX, gridY, null);
        
        // Render the HUD
        hud.render(graphics, player);
        
        // Update the display
        frame.repaint();
    }
    
    /**
     * Disposes of the renderer resources.
     */
    public void dispose() {
        graphics.dispose();
        gridRenderer.dispose();
        frame.dispose();
    }
}