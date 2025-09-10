package me.friedhof.hyperbuilder.rendering.modules;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;
import me.friedhof.hyperbuilder.computation.modules.Player;
import me.friedhof.hyperbuilder.computation.modules.Vector4D;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import me.friedhof.hyperbuilder.computation.modules.World;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsPlaceable;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.Entity;
import me.friedhof.hyperbuilder.computation.modules.DroppedItem;
import me.friedhof.hyperbuilder.rendering.modules.Texture2D;


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
    
    private HashMap<Material, Texture4D> texturelist = new HashMap<>();
   
    
    /**
     * Gets the size of a slice in pixels.
     * 
     * @return The size of a slice in pixels
     */
    public static int getSliceSizeTimesBlockSize() {
        return SLICE_SIZE * BLOCK_SIZE;
    }
     /**
     * Gets the center of a slice.
     * 
     * @return The center of a slice
     */
    public static int getSliceSize() {
        return SLICE_SIZE;
    }
     public static int getSliceCenter() {
        return (int) Math.floor(SLICE_SIZE/2);
    }
    /**
     * Sets the block size based on window dimensions.
     * This ensures the 7x7 grid of slices fills as much of the window as possible.
     * 
     * @param windowWidth The width of the window
     * @param windowHeight The height of the window
     */
    public static void setDynamicBlockSize(int windowWidth, int windowHeight) {
        // Calculate the available space for the 7x7 grid (maximize window usage)
        int availableWidth = (int)(windowWidth * 0.98); // 98% of window width
        int availableHeight = windowHeight - 20; // Reserve minimal 20 pixels for HUD at top
        
        // Calculate block size based on the smaller dimension to ensure everything fits
        // Remove padding entirely to maximize block size
        int maxBlockSizeFromWidth = availableWidth / (SLICE_SIZE * SLICE_SIZE); // 7 slices, no padding
        int maxBlockSizeFromHeight = availableHeight / (SLICE_SIZE * SLICE_SIZE); // 7 slices, no padding
        
        BLOCK_SIZE = Math.min(maxBlockSizeFromWidth, maxBlockSizeFromHeight);
        
        // Ensure minimum block size for visibility
        if (BLOCK_SIZE < 8) {
            BLOCK_SIZE = 8;
        }
        /* 
        System.out.println("Window size: " + windowWidth + "x" + windowHeight);
        System.out.println("Available space: " + availableWidth + "x" + availableHeight);
        System.out.println("Max block size from width: " + maxBlockSizeFromWidth);
        System.out.println("Max block size from height: " + maxBlockSizeFromHeight);
        System.out.println("Dynamic block size calculated: " + BLOCK_SIZE + " pixels");
        System.out.println("Total grid size will be: " + (SLICE_SIZE * SLICE_SIZE * BLOCK_SIZE) + " pixels");
*/


    }
    
    // Graphics context for drawing
    private Graphics2D graphics;
    
    /**
     * Creates a new slice renderer.
     */
    public SliceRenderer() {
        createSliceImage();
        loadTextures();
    }
    
    /**
     * Loads the textures needed for rendering.
     */
    private void loadTextures() {
        
           for(Material s : ItemRegistry.itemFactories.keySet()){
                if(ItemRegistry.itemFactories.get(s) instanceof IsPlaceable){
                    try {
                        texturelist.put(s, TextureManager.loadTexture4D(s + ".png"));
                    } catch (IOException e) {
                        System.err.println("Failed to load texture for item " + s + ": " + e.getMessage());
                         texturelist.put(s, null);
                    }       
                }
            }
        
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
     * @param sliceHorizontal The horizontal coordinate of the slice in the grid
     * @param sliceVertical The vertical coordinate of the slice in the grid
     * @param camera The camera to use for rendering
     * @param player The player to render (if in this slice)
     * @param game The game instance for line-of-sight checks
     * @param mouseX The mouse X coordinate for hover detection
     * @param mouseY The mouse Y coordinate for hover detection
     * @return The rendered slice image
     */
    public BufferedImage renderSlice(World world, int sliceHorizontal, int sliceVertical, Camera camera, me.friedhof.hyperbuilder.computation.modules.Player player, me.friedhof.hyperbuilder.Game game, int mouseX, int mouseY) {
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
         Vector4D sliceCenterWorld = camera.getSliceCenterWorldCoord(sliceHorizontal, sliceVertical);

        // Calculate fractional offsets for smooth movement
        double fracHorizontal;
        double fracY; // Y is always the within-slice vertical dimension
        
        // Calculate fractional coordinates for outer dimensions (for texture transitions)
        double fracZ, fracW;
        
        switch (camera.getHorizontalDimension()) {
            case X:
                // X mode: horizontal=X, vertical=Y
                fracHorizontal = sliceCenterWorld.getX() - Math.floor(sliceCenterWorld.getX());
                fracZ = sliceCenterWorld.getZ() - Math.floor(sliceCenterWorld.getZ());
                fracW = sliceCenterWorld.getW() - Math.floor(sliceCenterWorld.getW());
                fracY = sliceCenterWorld.getY() - Math.floor(sliceCenterWorld.getY());
                break;
            case Z:
                // Z mode: horizontal=Z, vertical=Y
                fracHorizontal = sliceCenterWorld.getZ() - Math.floor(sliceCenterWorld.getZ());
                fracZ = sliceCenterWorld.getX() - Math.floor(sliceCenterWorld.getX());
                fracW = sliceCenterWorld.getW() - Math.floor(sliceCenterWorld.getW());
                fracY = sliceCenterWorld.getY() - Math.floor(sliceCenterWorld.getY());
                break;
            case W:
                // W mode: horizontal=W, vertical=Y
                fracHorizontal = sliceCenterWorld.getW() - Math.floor(sliceCenterWorld.getW());
                fracZ = sliceCenterWorld.getX() - Math.floor(sliceCenterWorld.getX());
                fracW = sliceCenterWorld.getZ() - Math.floor(sliceCenterWorld.getZ());
                fracY = sliceCenterWorld.getY() - Math.floor(sliceCenterWorld.getY());
                break;
            default:
                // Default to X mode: horizontal=X, vertical=Y
                fracHorizontal = sliceCenterWorld.getX() - Math.floor(sliceCenterWorld.getX());
                fracZ = sliceCenterWorld.getZ() - Math.floor(sliceCenterWorld.getZ());
                fracW = sliceCenterWorld.getW() - Math.floor(sliceCenterWorld.getW());
                fracY = sliceCenterWorld.getY() - Math.floor(sliceCenterWorld.getY());
                break;
        }
        
        // Draw the blocks in this slice, including partial blocks at edges
        // Draw one extra block in each direction to fill gaps from fractional movement
        for (int y = -1; y <= SLICE_SIZE; y++) {
            for (int x = -1; x <= SLICE_SIZE; x++) {


                Vector4DInt blockPos;
                 // Fix upside-down rendering: higher Y values should be at the top of the screen
                int worldY = (int) Math.floor(sliceCenterWorld.getY()) + getSliceCenter() - y;


                switch (camera.getHorizontalDimension()) {
                    case X:
                        // X mode: x varies along X dimension, y varies along Y dimension
                        int worldX = (int) Math.floor(sliceCenterWorld.getX()) - getSliceCenter() + x;
                        blockPos = new Vector4DInt(worldX, worldY, (int) Math.floor(sliceCenterWorld.getZ()), (int) Math.floor(sliceCenterWorld.getW()));
                        
                        break;
                    case Z:
                        // Z mode: x varies along Z dimension, y varies along Y dimension
                        int worldZ = (int) Math.floor(sliceCenterWorld.getZ()) - getSliceCenter() + x;
                        blockPos = new Vector4DInt((int) Math.floor(sliceCenterWorld.getX()), worldY, worldZ, (int) Math.floor(sliceCenterWorld.getW()));
                        
                        break;
                    case W:
                        // W mode: x varies along W dimension, y varies along Y dimension
                        int worldW = (int) Math.floor(sliceCenterWorld.getW()) - getSliceCenter() + x;
                        blockPos = new Vector4DInt((int) Math.floor(sliceCenterWorld.getX()), worldY, (int) Math.floor(sliceCenterWorld.getZ()), worldW);
                        
                        break;
                
                    default:
                        // Default to X mode
                        worldX = (int) Math.floor(sliceCenterWorld.getX()) - getSliceCenter() + x;
                        blockPos = new Vector4DInt(worldX, worldY, (int) Math.floor(sliceCenterWorld.getZ()), (int) Math.floor(sliceCenterWorld.getW()));
                        
                        break;
                }
              
        
                Block block = world.getBlock(blockPos);

                

                // Draw the block with fractional offset for smooth movement
                // The clipping will be handled by the graphics context
                drawBlockWithOffset(freshGraphics, x, y, block, fracHorizontal, fracY, fracZ, fracW, blockPos, game, sliceHorizontal, sliceVertical, mouseX, mouseY);
            }
        }
        
        // Draw a border around the slice
        freshGraphics.setColor(Color.WHITE);
        freshGraphics.drawRect(0, 0, freshSliceImage.getWidth() - 1, freshSliceImage.getHeight() - 1);
        
        // Check if the player should be drawn in this slice
        Vector4D playerWorldPos = player.getPosition();
        Vector4D playerViewPos = camera.worldToView(playerWorldPos);
        
        // Calculate which slice the player should appear in based on camera mode
        // Must match the coordinate mapping in Camera.getSliceCenterWorldCoord
        // The grid represents the two dimensions orthogonal to the viewing plane
        int playerSliceHorizontal, playerSliceVertical;
        switch (camera.getHorizontalDimension()) {
            case X:
                // X mode: viewing X-Y plane, grid represents Z (horizontal) and W (vertical)
                playerSliceHorizontal = (int) Math.round(playerViewPos.getZ()) + getSliceCenter();
                playerSliceVertical = (int) Math.round(playerViewPos.getW()) + getSliceCenter();
                break;
            case Z:
                // Z mode: viewing Z-Y plane, grid represents X (horizontal) and W (vertical)
                playerSliceHorizontal = (int) Math.round(playerViewPos.getX()) + getSliceCenter();
                playerSliceVertical = (int) Math.round(playerViewPos.getW()) + getSliceCenter();
                break;
            case W:
                // W mode: viewing W-Y plane, grid represents X (horizontal) and Z (vertical)
                playerSliceHorizontal = (int) Math.round(playerViewPos.getX()) + getSliceCenter();
                playerSliceVertical = (int) Math.round(playerViewPos.getZ()) + getSliceCenter();
                break;
            default:
                // Default to X mode: viewing X-Y plane, grid represents Z and W
                playerSliceHorizontal = (int) Math.round(playerViewPos.getZ()) + getSliceCenter();
                playerSliceVertical = (int) Math.round(playerViewPos.getW()) + getSliceCenter();
                break;
        }
        
        // If this is the slice containing the player, draw the player
        if (sliceHorizontal == playerSliceHorizontal && sliceVertical == playerSliceVertical && 
            playerSliceHorizontal >= 0 && playerSliceHorizontal < SLICE_SIZE && playerSliceVertical >= 0 && playerSliceVertical < 7) {
            
            // Draw a thicker border for the slice containing the player
            freshGraphics.setColor(Color.WHITE);
            freshGraphics.setStroke(new BasicStroke(3));
            freshGraphics.drawRect(0, 0, freshSliceImage.getWidth() - 1, freshSliceImage.getHeight() - 1);
            freshGraphics.setStroke(new BasicStroke(1));
            
            // Draw the player at their relative position within this slice
            drawPlayerOnGraphics(freshGraphics, player,camera);
        }
        
        // Draw dropped items in this slice
        drawDroppedItemsInSlice(freshGraphics, world, sliceHorizontal, sliceVertical, camera);
        
        // Clean up graphics resources
        freshGraphics.dispose();
        
        return freshSliceImage;
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
     * @param fracZ The fractional Z coordinate for texture transitions
     * @param fracW The fractional W coordinate for texture transitions
     * @param blockPos The world position of the block
     * @param game The game instance for line-of-sight checks
     * @param sliceHorizontal The horizontal slice coordinate
     * @param sliceVertical The vertical slice coordinate
     * @param mouseX The mouse X coordinate
     * @param mouseY The mouse Y coordinate
     */
    private void drawBlockWithOffset(Graphics2D g, int x, int y, Block block, double fracX, double fracY, double fracZ, double fracW, Vector4DInt blockPos, me.friedhof.hyperbuilder.Game game, int sliceHorizontal, int sliceVertical, int mouseX, int mouseY) {
        // Calculate the pixel coordinates with fractional offset
        // Add 0.5 * BLOCK_SIZE to center blocks on the grid
        int pixelX = (int)((x + 0.5) * BLOCK_SIZE - fracX * BLOCK_SIZE);
        int pixelY = (int)((y - 0.5) * BLOCK_SIZE + fracY * BLOCK_SIZE); // Note: + because Y is inverted
        
        // Draw the block based on its type
        if (block != null && !block.getBlockId().equals(Material.AIR)) {
            Material blockId = block.getBlockId();

            for(Material s : texturelist.keySet()){
                if(s.equals(blockId)){
                    if(texturelist.get(s) != null){
                        drawTexturedBlock(g, pixelX, pixelY, texturelist.get(s), blockPos, fracZ, fracW);
                    }else{
                        g.setColor(new Color(255, 0, 220));
                        g.fillRect(pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE);
                    }
                }
            }
        }
        // Add visual indicator for line-of-sight
        
        boolean canDestroy = game.isInSightOfPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getW());
        
        // Check if this block is being hovered over
        boolean isHovered = isBlockHovered(x, y, sliceHorizontal, sliceVertical, mouseX, mouseY, game);
        
        // Set outline color based on line-of-sight
        Color outlineColor;
        int outlineThickness;

        if (canDestroy && (!block.getBlockId().equals(Material.AIR) || game.hasAdjacentBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getW()))) {
            // For air blocks, check if the selected item can be placed
            if (block.getBlockId().equals(Material.AIR)) {
                me.friedhof.hyperbuilder.computation.modules.Player player = game.getPlayer();
                if (player != null) {
                    me.friedhof.hyperbuilder.computation.modules.items.BaseItem selectedItem = 
                        game.getRenderer().getHUD().getHotbar().getSelectedItem(player.getInventory());
                    
                    if (selectedItem != null) {
                        // Check if it's grass or sapling and if it can be placed
                        if (selectedItem.getItemId().equals(Material.GRASS) ||
                            selectedItem.getItemId().equals(Material.SAPLING)) {
                            // Check if the block below is grass
                            Block blockBelow = 
                                game.getWorld().getBlock(new Vector4DInt(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ(), blockPos.getW()));
                            if (blockBelow != null && blockBelow.getBlockId().equals(Material.GRASS_BLOCK)) {
                                outlineColor = new Color(0, 255, 0, 200); // Green outline - can place
                            } else {
                                outlineColor = new Color(255, 0, 0, 200); // Red outline - cannot place
                            }
                        } else {
                            outlineColor = new Color(0, 255, 0, 200); // Green outline - other items can be placed
                        }
                        Block blockToPlace = ItemRegistry.createBlock(selectedItem.getItemId());
                        if(game.checkEntityInWayAtPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getW(), blockToPlace)){
                                    // Red for blocks that cannot be destroyed
                                    outlineColor =  new Color(255, 0, 0, 200);
                        }

                    } else {
                        outlineColor = new Color(0, 255, 0, 200); // Green outline - no item selected
                    }
                } else {
                    outlineColor = new Color(0, 255, 0, 200); // Green outline - no player
                }
            } else {
                outlineColor = new Color(0, 255, 0, 200); // Green outline - can destroy
            }
        } else {
            // Red for blocks that cannot be destroyed
            outlineColor =  new Color(255, 0, 0, 200);
        }
       

        outlineThickness = 3;
        
        if(isHovered){
            // Draw the outline
            g.setColor(outlineColor);
            g.setStroke(new BasicStroke(outlineThickness));
            g.drawRect(pixelX + 1, pixelY + 1, BLOCK_SIZE - 3, BLOCK_SIZE - 3);
        }
        g.setStroke(new BasicStroke(1)); // Reset stroke
        
    }
    
    /**
     * Draws a textured block using a 4D texture.
     * 
     * @param g The graphics context
     * @param pixelX The X pixel coordinate to draw at
     * @param pixelY The Y pixel coordinate to draw at
     * @param texture The 4D texture to use
     * @param blockPos The 4D position of the block (used for texture coordinates)
     */
    private void drawTexturedBlock(Graphics2D g, int pixelX, int pixelY, Texture4D texture, Vector4DInt blockPos, double fracZ, double fracW) {
        // Calculate fractional texture coordinates for smooth transitions
        // Scale fractional coordinates by 8 to get texture transitions every 1/8th of a block
        double textureZ = blockPos.getZ() + fracZ * 8.0;
        double textureW = blockPos.getW() + fracW * 8.0;
        
        // Get the interpolated 2D texture slice using fractional coordinates
        BufferedImage textureSlice = texture.getSlice2DFractional(textureZ, textureW);
        
        // Scale and draw the texture to fit the block size
        g.drawImage(textureSlice, pixelX, pixelY, BLOCK_SIZE, BLOCK_SIZE, null);
    }
    
    /**
     * Checks if a block is being hovered over by the mouse.
     * 
     * @param blockX The block's X coordinate in the slice
     * @param blockY The block's Y coordinate in the slice
     * @param sliceHorizontal The horizontal slice coordinate
     * @param sliceVertical The vertical slice coordinate
     * @param mouseX The mouse X coordinate
     * @param mouseY The mouse Y coordinate
     * @param game The game instance for coordinate conversion
     * @return true if the block is being hovered over
     */
    private boolean isBlockHovered(int blockX, int blockY, int sliceHorizontal, int sliceVertical, int mouseX, int mouseY, me.friedhof.hyperbuilder.Game game) {
        // Convert mouse coordinates to world coordinates
        Vector4DInt hoveredBlock = game.screenToWorldCoordinates(mouseX, mouseY);
        
        if (hoveredBlock == null) {
            return false;
        }


        // Get the world coordinates for this block
        // This logic should match the coordinate calculation in renderSlice
        Camera camera = game.getCamera();
        Vector4D sliceCenterWorld = camera.getSliceCenterWorldCoord(sliceHorizontal, sliceVertical);
        
        int worldY = (int) Math.floor(sliceCenterWorld.getY()) + getSliceCenter() - blockY;
        
        Vector4DInt blockWorldPos;
        switch (camera.getHorizontalDimension()) {
            case X:
                int worldX = (int) Math.floor(sliceCenterWorld.getX()) - getSliceCenter() + blockX;
                blockWorldPos = new Vector4DInt(worldX, worldY, (int) Math.floor(sliceCenterWorld.getZ()), (int) Math.floor(sliceCenterWorld.getW()));
                break;
            case Z:
                int worldZ = (int) Math.floor(sliceCenterWorld.getZ()) - getSliceCenter() + blockX;
                blockWorldPos = new Vector4DInt((int) Math.floor(sliceCenterWorld.getX()), worldY, worldZ, (int) Math.floor(sliceCenterWorld.getW()));
                break;
            case W:
                int worldW = (int) Math.floor(sliceCenterWorld.getW()) - getSliceCenter() + blockX;
                blockWorldPos = new Vector4DInt((int) Math.floor(sliceCenterWorld.getX()), worldY, (int) Math.floor(sliceCenterWorld.getZ()), worldW);
                break;
            default:
                worldX = (int) Math.floor(sliceCenterWorld.getX()) - getSliceCenter() + blockX;
                blockWorldPos = new Vector4DInt(worldX, worldY, (int) Math.floor(sliceCenterWorld.getZ()), (int) Math.floor(sliceCenterWorld.getW()));
                break;
        }
        
        // Check if the hovered block matches this block's world position
        return hoveredBlock.equals(blockWorldPos);
    }
    
    /**
     * Draws dropped items that are in the specified slice.
     * 
     * @param g The graphics context to draw on
     * @param world The world containing the entities
     * @param sliceHorizontal The horizontal slice coordinate
     * @param sliceVertical The vertical slice coordinate
     * @param camera The camera for coordinate conversion
     */
    private void drawDroppedItemsInSlice(Graphics2D g, World world, int sliceHorizontal, int sliceVertical, Camera camera) {
        // Get all entities from the world
        java.util.List<Entity> entities = world.getEntitiesList();
        
        for (Entity entity : entities) {
            if (entity instanceof DroppedItem) {
                DroppedItem droppedItem = (DroppedItem) entity;
                
                // Convert dropped item world position to view coordinates
                Vector4D itemWorldPos = droppedItem.getPosition();
                Vector4D itemViewPos = camera.worldToView(itemWorldPos);
                
                // Calculate which slice the dropped item should appear in
                int itemSliceHorizontal, itemSliceVertical;
                switch (camera.getHorizontalDimension()) {
                    case X:
                        // X mode: viewing X-Y plane, grid represents Z (horizontal) and W (vertical)
                        itemSliceHorizontal = (int) Math.round(itemViewPos.getZ()) + getSliceCenter();
                        itemSliceVertical = (int) Math.round(itemViewPos.getW()) + getSliceCenter();
                        break;
                    case Z:
                        // Z mode: viewing Z-Y plane, grid represents X (horizontal) and W (vertical)
                        itemSliceHorizontal = (int) Math.round(itemViewPos.getX()) + getSliceCenter();
                        itemSliceVertical = (int) Math.round(itemViewPos.getW()) + getSliceCenter();
                        break;
                    case W:
                        // W mode: viewing W-Y plane, grid represents X (horizontal) and Z (vertical)
                        itemSliceHorizontal = (int) Math.round(itemViewPos.getX()) + getSliceCenter();
                        itemSliceVertical = (int) Math.round(itemViewPos.getZ()) + getSliceCenter();
                        break;
                    default:
                        // Default to X mode: viewing X-Y plane, grid represents Z and W
                        itemSliceHorizontal = (int) Math.round(itemViewPos.getZ()) + getSliceCenter();
                        itemSliceVertical = (int) Math.round(itemViewPos.getW()) + getSliceCenter();
                        break;
                }
                
                // If this dropped item is in the current slice, draw it
                if (sliceHorizontal == itemSliceHorizontal && sliceVertical == itemSliceVertical &&
                    itemSliceHorizontal >= 0 && itemSliceHorizontal < SLICE_SIZE && itemSliceVertical >= 0 && itemSliceVertical < SLICE_SIZE) {
                    
                    drawDroppedItemOnGraphics(g, droppedItem, camera);
                }
            }
        }
    }
    
    /**
     * Draws a single dropped item on the graphics context.
     * 
     * @param g The graphics context to draw on
     * @param droppedItem The dropped item to draw
     * @param camera The camera for coordinate conversion
     */
    private void drawDroppedItemOnGraphics(Graphics2D g, DroppedItem droppedItem, Camera camera) {
        Vector4D itemWorldPos = droppedItem.getPosition();
        Vector4D itemViewPos = camera.worldToView(itemWorldPos);
        
        // Calculate the item's position within the slice
        double relativeX = itemViewPos.getX();
        double relativeY = itemViewPos.getY();
        
        // Convert to pixel coordinates within the slice
        double pixelX = (getSliceCenter() + 0.5 + relativeX) * BLOCK_SIZE;
        double pixelY = (getSliceCenter() + 0.5 - relativeY) * BLOCK_SIZE; // Subtract because Y is flipped
        
        // Dropped items are smaller than blocks 
        int itemSizePixels = (int)(droppedItem.getSizeX() * BLOCK_SIZE);
        
        // Try to get the texture for the item's material
        BaseItem item = droppedItem.getItem();
        Material itemMaterial = item.getItemId();
        
        // Try to get 2D texture for the item first (preferred for dropped items)
        Texture2D texture2D = ItemRegistry.getItemTexture(itemMaterial);
        BufferedImage textureImage = null;
        
        if (texture2D != null) {
            // Use the 2D texture directly as it is
            textureImage = texture2D.getImage();
        } 
        
        if (textureImage != null) {
            // Draw the item using its texture (smaller than a block)
            
            // Store original rendering hints
            Object originalInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            Object originalRendering = g.getRenderingHint(RenderingHints.KEY_RENDERING);
            
            // Set pixel-perfect rendering hints to preserve all pixels
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            
            g.drawImage(textureImage, 
                (int)(pixelX - itemSizePixels / 2), 
                (int)(pixelY - itemSizePixels / 2), 
                itemSizePixels, 
                itemSizePixels, 
                null);
            
            // Restore original rendering hints
            if (originalInterpolation != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, originalInterpolation);
            }
            if (originalRendering != null) {
                g.setRenderingHint(RenderingHints.KEY_RENDERING, originalRendering);
            }
            
            // Draw a very thin edge around the dropped item
            g.setColor(new Color(255, 255, 255, 80)); 
            g.setStroke(new BasicStroke(10.0f)); // 1 pixel thin stroke
            g.drawRect(
                (int)(pixelX - itemSizePixels / 2), 
                (int)(pixelY - itemSizePixels / 2), 
                itemSizePixels - 1, 
                itemSizePixels - 1
            );
        } else {
            // Fallback: draw as a small colored square
            g.setColor(Color.YELLOW);
            g.fillRect(
                (int)(pixelX - itemSizePixels / 2), 
                (int)(pixelY - itemSizePixels / 2), 
                itemSizePixels, 
                itemSizePixels
            );
        }
        
    }
    
    /**
     * Draws the player on the graphics context.
     * 
     * @param g The graphics context to draw on
     * @param playerViewPos The player's position in view coordinates
     */
    private void drawPlayerOnGraphics(Graphics2D g, Player player, Camera camera) {


        Vector4D playerWorldPos = player.getPosition();
        Vector4D playerViewPos = camera.worldToView(playerWorldPos);
        // Calculate the player's position within the slice
        // The slice center is at (3, 3) in slice coordinates
        // Player position is relative to the slice center
        double relativeX = playerViewPos.getX();
        double relativeY = playerViewPos.getY();
        
        // Convert to pixel coordinates within the slice
        // Add 3.5 to center in the slice, then add the fractional offset
        double pixelX = (getSliceCenter() + 0.5 + relativeX) * BLOCK_SIZE;
        double pixelY = (getSliceCenter() + 0.5 - relativeY) * BLOCK_SIZE; // Subtract because Y is flipped in screen coordinates
        
        // Player hitbox is 0.5x0.5 blocks, so draw circle to match this size
        // Convert player size (0.5 blocks) to pixels
        int playerSizePixels = (int)(player.getSize() * BLOCK_SIZE);
        

        // Draw the player as a red circle centered at the calculated position
        // The position represents the center of the player (matching collision detection)

        g.setColor(Color.RED);
        g.fillOval(
            (int)(pixelX - playerSizePixels / 2), 
            (int)(pixelY - playerSizePixels / 2), 
            playerSizePixels, 
            playerSizePixels
        );
       
    }
    
    /**
     * Disposes of the graphics resources.
     */
    public void dispose() {
        graphics.dispose();
    }
}