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
    // The size of each slice (7x7 blocks)
    private static final int SLICE_SIZE = 7;
    
    // The size of each block in pixels (calculated dynamically)
    private static int BLOCK_SIZE = 32;
    
    // The rendered image
    private BufferedImage sliceImage;
    
    /**
     * Gets the size of a slice in pixels.
     * 
     * @return The size of a slice in pixels
     */
    public static int getSliceSize() {
        return SLICE_SIZE * BLOCK_SIZE;
    }
    
    /**
     * Sets the block size based on window dimensions.
     * This ensures the 7x7 grid of slices fits properly in the window.
     * 
     * @param windowWidth The width of the window
     * @param windowHeight The height of the window
     */
    public static void setDynamicBlockSize(int windowWidth, int windowHeight) {
        // Calculate the available space for the 7x7 grid (maximize window usage)
        int availableWidth = (int)(windowWidth * 0.98); // 98% of window width
        int availableHeight = (int)(windowHeight * 0.95); // 95% of window height (minimal HUD space)
        
        // Calculate block size based on the smaller dimension to ensure everything fits
        // Use smaller padding (3 instead of 5) to make slices bigger
        int maxBlockSizeFromWidth = availableWidth / (7 * SLICE_SIZE + 6 * 3); // 7 slices + 6 paddings
        int maxBlockSizeFromHeight = availableHeight / (7 * SLICE_SIZE + 6 * 3); // 7 slices + 6 paddings
        
        BLOCK_SIZE = Math.min(maxBlockSizeFromWidth, maxBlockSizeFromHeight);
        
        // Ensure minimum block size for visibility
        if (BLOCK_SIZE < 8) {
            BLOCK_SIZE = 8;
        }
    }
    
    // Graphics context for drawing
    private Graphics2D graphics;
    
    /**
     * Creates a new slice renderer.
     */
    public SliceRenderer() {
        createSliceImage();
    }
    
    /**
     * Creates or recreates the slice image based on current block size.
     */
    private void createSliceImage() {
        // Dispose of existing graphics if they exist
        if (graphics != null) {
            graphics.dispose();
        }
        
        // Create the image for the slice
        sliceImage = new BufferedImage(
            SLICE_SIZE * BLOCK_SIZE, 
            SLICE_SIZE * BLOCK_SIZE, 
            BufferedImage.TYPE_INT_ARGB
        );
        
        // Create the graphics context
        graphics = sliceImage.createGraphics();
        
        // Enable anti-aliasing
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
    
    /**
     * Updates the slice renderer when block size changes.
     */
    public void updateBlockSize() {
        createSliceImage();
    }
    
    /**
     * Renders a slice of the world at the specified 4D coordinates.
     * 
     * @param world The world to render
     * @param sliceX The x-coordinate of the slice in the grid
     * @param sliceY The y-coordinate of the slice in the grid
     * @param camera The camera to use for rendering
     * @param player The player to render (if in this slice)
     * @return The rendered slice image
     */
    public BufferedImage renderSlice(World world, int sliceX, int sliceY, Camera camera, com.adventure4d.computation.modules.Player player) {
        // Create a fresh image for each slice to avoid artifacts
        BufferedImage freshSliceImage = new BufferedImage(
            sliceImage.getWidth(), 
            sliceImage.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D freshGraphics = freshSliceImage.createGraphics();
        
        // Enable anti-aliasing
        freshGraphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        
        // Clear the image with transparent background
        freshGraphics.setColor(new Color(0, 0, 0, 0));
        freshGraphics.fillRect(0, 0, freshSliceImage.getWidth(), freshSliceImage.getHeight());
        
        // Set clipping rectangle to prevent blocks from drawing outside slice boundaries
        freshGraphics.setClip(0, 0, freshSliceImage.getWidth(), freshSliceImage.getHeight());
        
        // Get the world coordinates for the center of this slice
        Vector4D sliceCenterWorld = camera.getSliceCenterWorldCoord(sliceX, sliceY);
        
        // Calculate fractional offsets for smooth movement
        double fracX = sliceCenterWorld.getX() - Math.floor(sliceCenterWorld.getX());
        double fracY = sliceCenterWorld.getY() - Math.floor(sliceCenterWorld.getY());
        
        // Draw the blocks in this slice, including partial blocks at edges
        // Draw one extra block in each direction to fill gaps from fractional movement
        for (int y = -1; y <= SLICE_SIZE; y++) {
            for (int x = -1; x <= SLICE_SIZE; x++) {
                // Calculate the world coordinates (integer positions)
                int worldX = (int) Math.floor(sliceCenterWorld.getX()) - 3 + x;
                // Fix upside-down rendering: higher Y values should be at the top of the screen
                int worldY = (int) Math.floor(sliceCenterWorld.getY()) + 3 - y;
                
                // Get the block at this position
                Vector4DInt blockPos = new Vector4DInt(worldX, worldY, (int) Math.floor(sliceCenterWorld.getZ()), (int) Math.floor(sliceCenterWorld.getW()));
                Block block = world.getBlock(blockPos);
                
                // Draw the block with fractional offset for smooth movement
                // The clipping will be handled by the graphics context
                drawBlockWithOffset(freshGraphics, x, y, block, fracX, fracY);
            }
        }
        
        // Draw a border around the slice
        freshGraphics.setColor(Color.GRAY);
        freshGraphics.drawRect(0, 0, freshSliceImage.getWidth() - 1, freshSliceImage.getHeight() - 1);
        
        // Check if the player should be drawn in this slice
        Vector4D playerWorldPos = player.getPosition();
        Vector4D playerViewPos = camera.worldToView(playerWorldPos);
        
        // Calculate which slice the player should appear in
        int playerSliceX = (int) Math.round(playerViewPos.getW()) + 3;
        int playerSliceY = (int) Math.round(playerViewPos.getZ()) + 3;
        
        // If this is the slice containing the player, draw the player
        if (sliceX == playerSliceX && sliceY == playerSliceY && 
            playerSliceX >= 0 && playerSliceX < 7 && playerSliceY >= 0 && playerSliceY < 7) {
            
            // Draw a thicker border for the slice containing the player
            freshGraphics.setColor(Color.WHITE);
            freshGraphics.setStroke(new BasicStroke(3));
            freshGraphics.drawRect(0, 0, freshSliceImage.getWidth() - 1, freshSliceImage.getHeight() - 1);
            freshGraphics.setStroke(new BasicStroke(1));
            
            // Draw the player at their relative position within this slice
            drawPlayerOnGraphics(freshGraphics, playerViewPos);
        }
        
        // Clean up graphics resources
        freshGraphics.dispose();
        
        return freshSliceImage;
    }
    
    /**
     * Draws a block at the specified position.
     * 
     * @param x The x-coordinate in the slice
     * @param y The y-coordinate in the slice
     * @param block The block to draw
     */
    private void drawBlock(int x, int y, Block block) {
        drawBlockOnGraphics(graphics, x, y, block);
    }
    
    /**
     * Draws a block at the specified position using the provided graphics context.
     * 
     * @param g The graphics context to draw on
     * @param x The x-coordinate in the slice
     * @param y The y-coordinate in the slice
     * @param block The block to draw
     */
    private void drawBlockOnGraphics(Graphics2D g, int x, int y, Block block) {
        drawBlockWithOffset(g, x, y, block, 0.0, 0.0);
    }
    
    /**
     * Draws a block at the specified position with fractional offset for smooth movement.
     * 
     * @param g The graphics context to draw on
     * @param x The x-coordinate in the slice
     * @param y The y-coordinate in the slice
     * @param block The block to draw
     * @param fracX The fractional X offset (0.0 to 1.0)
     * @param fracY The fractional Y offset (0.0 to 1.0)
     */
    private void drawBlockWithOffset(Graphics2D g, int x, int y, Block block, double fracX, double fracY) {
        // Calculate the pixel coordinates with fractional offset
        int pixelX = (int)(x * BLOCK_SIZE - fracX * BLOCK_SIZE);
        int pixelY = (int)(y * BLOCK_SIZE + fracY * BLOCK_SIZE); // Note: + because Y is inverted
        
        // Draw the block based on its type
        if (block != null) {
            switch (block.getType()) {
                case Block.TYPE_AIR:
                    // Air is transparent
                    break;
                    
                case Block.TYPE_DIRT:
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
                    
                case Block.TYPE_GRASS:
                    g.setColor(new Color(34, 139, 34));
                    g.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
                    
                case Block.TYPE_STONE:
                    g.setColor(new Color(128, 128, 128));
                    g.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
                    
                default:
                    // Unknown block type, draw as purple
                    g.setColor(Color.MAGENTA);
                    g.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    break;
            }
            
            // Draw a border around the block
            g.setColor(Color.BLACK);
            g.drawRect(pixelX, pixelY, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
        }
    }
    
    /**
     * Draws the player at their exact position within the slice.
     */
    private void drawPlayer(Vector4D playerPos) {
        drawPlayerOnGraphics(graphics, playerPos);
    }
    
    /**
     * Draws the player at their exact position within the slice using the provided graphics context.
     * 
     * @param g The graphics context to draw on
     * @param playerPos The player's position (unused - player is always centered)
     */
    private void drawPlayerOnGraphics(Graphics2D g, Vector4D playerPos) {
        // Player is always drawn at the center of the slice (position 3,3)
        // This ensures the player never moves visually - only the world moves around them
        int pixelX = (int)(3.5 * BLOCK_SIZE); // Center of the middle block
        int pixelY = (int)(3.5 * BLOCK_SIZE); // Center of the middle block
        
        // Draw the player as a red circle
        g.setColor(Color.RED);
        g.fillOval(
            pixelX - BLOCK_SIZE / 4, 
            pixelY - BLOCK_SIZE / 4, 
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