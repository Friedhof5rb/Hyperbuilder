package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Vector4D;
import com.adventure4d.computation.modules.World;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders a 7x7 grid of 2D slices representing the 4D world.
 */
public class GridRenderer {
    // The size of the grid (7x7)
    private static final int GRID_SIZE = SliceRenderer.getSliceSize();
    
    // The padding between slices (minimal padding to maximize slice size)
    private static final int SLICE_PADDING = 0;
    
    // The slice renderer
    private SliceRenderer sliceRenderer;
    
    // The rendered image
    private BufferedImage gridImage;
    
    // Graphics context for drawing
    private Graphics2D graphics;
    
    // Dynamic sizing variables
    private int sliceSizePixels;
    private int gridSizePixels;
    
    /**
     * Creates a new grid renderer.
     */
    public GridRenderer() {
        // Create the slice renderer
        sliceRenderer = new SliceRenderer();
        
        // Initialize sizing
        updateSizing();
        
        // Create the initial grid image
        createGridImage();
    }
    
    /**
     * Updates the sizing calculations.
     */
    private void updateSizing() {
        sliceSizePixels = SliceRenderer.getSliceSizeTimesBlockSize();
        gridSizePixels = GRID_SIZE * sliceSizePixels + (GRID_SIZE - 1) * SLICE_PADDING;
    }
    
    /**
     * Creates or recreates the grid image.
     */
    private void createGridImage() {
        // Dispose of existing graphics if they exist
        if (graphics != null) {
            graphics.dispose();
        }
        
        // Create the image for the grid
        gridImage = new BufferedImage(
            gridSizePixels, 
            gridSizePixels, 
            BufferedImage.TYPE_INT_ARGB
        );
        
        // Create the graphics context
        graphics = gridImage.createGraphics();
        
        // Enable anti-aliasing
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
    
    /**
     * Updates the grid renderer when block size changes.
     */
    public void updateBlockSize() {
        updateSizing();
        sliceRenderer.updateBlockSize();
        createGridImage();
    }
    
    /**
     * Renders the grid of slices.
     * 
     * @param world The world to render
     * @param camera The camera to use for rendering
     * @param player The player to render
     * @return The rendered grid image
     */
    public BufferedImage renderGrid(World world, Camera camera, com.adventure4d.computation.modules.Player player) {
        // Clear the image
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, gridImage.getWidth(), gridImage.getHeight());
        
        // Draw each slice
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                // Render the slice
                BufferedImage sliceImage = sliceRenderer.renderSlice(world, x, y, camera, player);
                
                // Calculate the position to draw the slice
                int drawX = x * (sliceSizePixels + SLICE_PADDING);
                int drawY = y * (sliceSizePixels + SLICE_PADDING);
                
                // Draw the slice
                graphics.drawImage(sliceImage, drawX, drawY, null);
            }
        }
        
        return gridImage;
    }
    
    /**
     * Gets the size of the grid in pixels.
     * 
     * @return The size of the grid in pixels
     */
    public int getGridSizePixels() {
        return gridSizePixels;
    }
    
    /**
     * Disposes of the graphics resources.
     */
    public void dispose() {
        graphics.dispose();
        sliceRenderer.dispose();
    }
}