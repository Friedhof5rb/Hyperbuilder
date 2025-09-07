package me.friedhof.hyperbuilder.rendering.modules;

import javax.swing.*;

import me.friedhof.hyperbuilder.computation.modules.World;

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
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Disable close button during gameplay
        frame.setResizable(true);
        
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
        
        // Set up dynamic block sizing
        SliceRenderer.setDynamicBlockSize(width, height);
        
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
     * Gets the HUD for accessing UI components.
     * 
     * @return The HUD
     */
    public HUD getHUD() {
        return hud;
    }
    
    /**
     * Gets the grid renderer component.
     * 
     * @return The grid renderer component
     */
    public GridRenderer getGridRenderer() {
        return gridRenderer;
    }
    
    /**
     * Updates the renderer dimensions when the window is resized.
     * 
     * @param newWidth The new width
     * @param newHeight The new height
     */
    public void updateDimensions(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        
        // Dispose old graphics and buffer
        if (graphics != null) {
            graphics.dispose();
        }
        
        // Create new buffer with updated dimensions
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = buffer.createGraphics();
        
        // Update HUD dimensions
        hud = new HUD(width, height);
        
        // Update dynamic block sizing
        SliceRenderer.setDynamicBlockSize(width, height);
        
        // Update grid renderer to recreate slices with new block size
        gridRenderer.updateBlockSize();
        
        // Update the panel size and force repaint
        Component panel = frame.getContentPane().getComponent(0);
        if (panel instanceof JPanel) {
            ((JPanel) panel).setPreferredSize(new Dimension(width, height));
            panel.setSize(new Dimension(width, height));
            panel.revalidate();
            panel.repaint();
        }
    }
    
    /**
     * Renders the world using the 2D grid of 2D grids approach.
     * 
     * @param world The world to render
     * @param camera The camera to use for rendering
     * @param player The player to render
     * @param game The game instance for line-of-sight checks
     * @param mouseX The current mouse X coordinate
     * @param mouseY The current mouse Y coordinate
     */
    public void render(World world, Camera camera, me.friedhof.hyperbuilder.computation.modules.Player player, me.friedhof.hyperbuilder.Game game, int mouseX, int mouseY) {
        // Clear the buffer
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        
        // Render the grid of slices
        BufferedImage gridImage = gridRenderer.renderGrid(world, camera, player, game, mouseX, mouseY);
        
        // Calculate the position to center the grid
        int gridX = (width - gridRenderer.getGridSizePixels()) / 2;
        int gridY = (height - gridRenderer.getGridSizePixels()) / 2;
        
        // Draw the grid
        graphics.drawImage(gridImage, gridX, gridY, null);
        
        // Render the HUD
        hud.render(graphics, camera, player, mouseX, mouseY, game);
        
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