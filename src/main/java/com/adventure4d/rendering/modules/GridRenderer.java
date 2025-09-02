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
    private static final int GRID_SIZE = 7;
    
    // The size of each slice in pixels
    private static final int SLICE_SIZE_PIXELS;
    
    // The padding between slices
    private static final int SLICE_PADDING = 5;
    
    // The total size of the grid in pixels
    private static final int GRID_SIZE_PIXELS;
    
    // The slice renderer
    private SliceRenderer sliceRenderer;
    
    // The rendered image
    private BufferedImage gridImage;
    
    // Graphics context for drawing
    private Graphics2D graphics;
    
    static {
        // Calculate the size of each slice in pixels
        SLICE_SIZE_PIXELS = SliceRenderer.getSliceSize();
        
        // Calculate the total size of the grid in pixels
        GRID_SIZE_PIXELS = GRID_SIZE * SLICE_SIZE_PIXELS + (GRID_SIZE - 1) * SLICE_PADDING;
    }
    
    /**
     * Creates a new grid renderer.
     */
    public GridRenderer() {
        // Create the slice renderer
        sliceRenderer = new SliceRenderer();
        
        // Create the image for the grid
        gridImage = new BufferedImage(
            GRID_SIZE_PIXELS, 
            GRID_SIZE_PIXELS, 
            BufferedImage.TYPE_INT_ARGB
        );
        
        // Get the graphics context
        graphics = gridImage.createGraphics();
        
        // Enable anti-aliasing
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
    
    /**
     * Renders the grid of slices.
     * 
     * @param world The world to render
     * @param playerPos The player's position
     * @return The rendered grid image
     */
    public BufferedImage renderGrid(World world, Vector4D playerPos) {
        // Clear the image
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, gridImage.getWidth(), gridImage.getHeight());
        
        // Draw each slice
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                // Render the slice
                BufferedImage sliceImage = sliceRenderer.renderSlice(world, x, y, playerPos);
                
                // Calculate the position to draw the slice
                int drawX = x * (SLICE_SIZE_PIXELS + SLICE_PADDING);
                int drawY = y * (SLICE_SIZE_PIXELS + SLICE_PADDING);
                
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
    public static int getGridSizePixels() {
        return GRID_SIZE_PIXELS;
    }
    
    /**
     * Disposes of the graphics resources.
     */
    public void dispose() {
        graphics.dispose();
        sliceRenderer.dispose();
    }
}