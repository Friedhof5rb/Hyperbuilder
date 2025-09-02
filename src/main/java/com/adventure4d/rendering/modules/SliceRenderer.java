package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Block;
import com.adventure4d.computation.modules.Vector4D;
import com.adventure4d.computation.modules.Vector4DInt;
import com.adventure4d.computation.modules.World;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders a single 2D slice of the 4D world.
 */
public class SliceRenderer {
    // The size of each block in pixels
    private static final int BLOCK_SIZE = 32;
    
    // The size of the slice in blocks
    private static final int SLICE_SIZE = 7;
    
    // The rendered image
    private BufferedImage sliceImage;
    
    /**
     * Gets the size of the slice in pixels.
     * 
     * @return The size of the slice in pixels
     */
    public static int getSliceSize() {
        return SLICE_SIZE * BLOCK_SIZE;
    }
    
    // Graphics context for drawing
    private Graphics2D graphics;
    
    /**
     * Creates a new slice renderer.
     */
    public SliceRenderer() {
        // Create the image for the slice
        sliceImage = new BufferedImage(
            SLICE_SIZE * BLOCK_SIZE, 
            SLICE_SIZE * BLOCK_SIZE, 
            BufferedImage.TYPE_INT_ARGB
        );
        
        // Get the graphics context
        graphics = sliceImage.createGraphics();
        
        // Enable anti-aliasing
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
    
    /**
     * Renders a slice of the world at the specified 4D coordinates.
     * 
     * @param world The world to render
     * @param sliceX The x-coordinate of the slice in the grid
     * @param sliceY The y-coordinate of the slice in the grid
     * @param playerPos The player's position
     * @return The rendered slice image
     */
    public BufferedImage renderSlice(World world, int sliceX, int sliceY, Vector4D playerPos) {
        // Clear the image
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, sliceImage.getWidth(), sliceImage.getHeight());
        
        // Calculate the w and z coordinates for this slice
        double w = playerPos.getW() + (sliceX - 3);
        double z = playerPos.getZ() + (sliceY - 3);
        
        // Draw the blocks in this slice
        for (int y = 0; y < SLICE_SIZE; y++) {
            for (int x = 0; x < SLICE_SIZE; x++) {
                // Calculate the world coordinates
                int worldX = (int) Math.floor(playerPos.getX()) - 3 + x;
                int worldY = (int) Math.floor(playerPos.getY()) - 3 + y;
                
                // Get the block at this position
                Vector4DInt blockPos = new Vector4DInt(worldX, worldY, (int) Math.floor(z), (int) Math.floor(w));
                Block block = world.getBlock(blockPos);
                
                // Draw the block
                drawBlock(x, y, block);
            }
        }
        
        // Draw a border around the slice
        graphics.setColor(Color.GRAY);
        graphics.drawRect(0, 0, sliceImage.getWidth() - 1, sliceImage.getHeight() - 1);
        
        // If this is the center slice (where the player is), highlight it
        if (sliceX == 3 && sliceY == 3) {
            // Draw a thicker border for the center slice
            graphics.setColor(Color.WHITE);
            graphics.setStroke(new BasicStroke(3));
            graphics.drawRect(0, 0, sliceImage.getWidth() - 1, sliceImage.getHeight() - 1);
            graphics.setStroke(new BasicStroke(1));
            
            // Draw the player in the center
            drawPlayer();
        }
        
        // Add slice coordinates
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        graphics.drawString("W:" + (int)Math.floor(w) + " Z:" + (int)Math.floor(z), 5, 15);
        
        return sliceImage;
    }
    
    /**
     * Draws a block at the specified position.
     * 
     * @param x The x-coordinate in the slice
     * @param y The y-coordinate in the slice
     * @param block The block to draw
     */
    private void drawBlock(int x, int y, Block block) {
        // Calculate the pixel coordinates
        int pixelX = x * BLOCK_SIZE;
        int pixelY = y * BLOCK_SIZE;
        
        // Draw the block based on its type
        if (block != null) {
            switch (block.getType()) {
                case Block.TYPE_AIR:
                    // Air is transparent
                    break;
                    
                case Block.TYPE_DIRT:
                    graphics.setColor(new Color(139, 69, 19));
                    graphics.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
                    
                case Block.TYPE_GRASS:
                    graphics.setColor(new Color(34, 139, 34));
                    graphics.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
                    
                case Block.TYPE_STONE:
                    graphics.setColor(new Color(128, 128, 128));
                    graphics.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
                    
                default:
                    // Unknown block type, draw as purple
                    graphics.setColor(Color.MAGENTA);
                    graphics.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
            }
            
            // Draw a border around the block
            graphics.setColor(Color.BLACK);
            graphics.drawRect(pixelX, pixelY, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
        }
    }
    
    /**
     * Draws the player in the center of the slice.
     */
    private void drawPlayer() {
        // Calculate the center position
        int centerX = (SLICE_SIZE / 2) * BLOCK_SIZE;
        int centerY = (SLICE_SIZE / 2) * BLOCK_SIZE;
        
        // Draw the player as a red circle
        graphics.setColor(Color.RED);
        graphics.fillOval(
            centerX + BLOCK_SIZE / 4, 
            centerY + BLOCK_SIZE / 4, 
            BLOCK_SIZE / 2, 
            BLOCK_SIZE / 2
        );
    }
    
    /**
     * Disposes of the graphics resources.
     */
    public void dispose() {
        graphics.dispose();
    }
}