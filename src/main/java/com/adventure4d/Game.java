package com.adventure4d;

import com.adventure4d.computation.modules.Vector4D;
import com.adventure4d.computation.modules.Vector4DInt;
import com.adventure4d.computation.modules.World;
import com.adventure4d.computation.modules.Player;
import com.adventure4d.computation.modules.Block;
import com.adventure4d.rendering.modules.Renderer;
import com.adventure4d.rendering.modules.SliceRenderer;
import com.adventure4d.rendering.modules.Camera;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for the 4D Adventure Game.
 */
public class Game {
    private static final String GAME_TITLE = "Adventure 4D";
    private static final String VERSION = "0.1.0";
    
    // Window dimensions
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 1000;
    
    private World world;
    private Player player;
    private Renderer renderer;
    private Camera camera;
    private boolean running;
    
    // Mouse position tracking
    private int mouseX = 0;
    private int mouseY = 0;
    
    /**
     * Initializes the game.
     */
    public void init() {
        System.out.println("Initializing " + GAME_TITLE + " v" + VERSION);
        
        // Create a new world
        world = new World("Test World", System.currentTimeMillis());
        
        // Create a player at spawn position (center at Y=1.25, so feet at Y=1, just above grass at Y=0)
        // Player size is 0.5, so center needs to be at Y=1.25 for feet to be at Y=1
        // Position at (0.5, 1.25, 0.5, 0.5) to avoid overlapping with blocks at negative coordinates
        player = world.createPlayer("Player1", new Vector4D(0, 2, 0, 0));
        
        // Debug: Check what blocks exist around spawn
        System.out.println("Debug: Checking blocks around spawn position:");
        for (int y = -2; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int w = -1; w <= 1; w++) {
                        Vector4DInt pos = new Vector4DInt(x, y, z, w);
                        Block block = world.getBlock(pos);
                        if (block != null && !block.isAir()) {
                            System.out.println("  Block at (" + x + ", " + y + ", " + z + ", " + w + "): " + block.getType());
                        }
                    }
                }
            }
        }
        
        // Give the player some starting blocks
        com.adventure4d.computation.modules.Inventory inventory = player.getInventory();
        inventory.addItem(com.adventure4d.computation.modules.Block.TYPE_DIRT, 64);
        inventory.addItem(com.adventure4d.computation.modules.Block.TYPE_STONE, 32);
        inventory.addItem(com.adventure4d.computation.modules.Block.TYPE_WOOD, 16);
        inventory.addItem(com.adventure4d.computation.modules.Block.TYPE_GRASS, 32);
        inventory.addItem(com.adventure4d.computation.modules.Block.TYPE_LEAVES, 16);
        
        // Create a camera starting at the player's initial world position
        camera = new Camera(new Vector4D(0, 1, 0, 0));
        
        // Initialize the renderer
        renderer = new Renderer(WIDTH, HEIGHT, GAME_TITLE + " v" + VERSION);
        
        // Set up input handling
        setupInput();
        
        // Show the window
        renderer.show();
        
        System.out.println("Game initialized successfully");
    }
    
    /**
     * Sets up input handling using KeyListener for better simultaneous key support.
     * This approach uses polling-based state tracking to overcome Java Swing's
     * limitations with multiple simultaneous key presses.
     */
    private void setupInput() {
        JFrame frame = renderer.getFrame();
        frame.setFocusable(true);
        frame.requestFocus();
        
        // Get the content pane for accurate mouse coordinates
        Component contentPane = frame.getContentPane();
        
        // Add KeyListener for direct key event handling
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e.getKeyCode());
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Not used for game input
            }
        });
        
        // Add MouseListener for block interaction
        contentPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
               
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY(), e.getButton());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                // Not used
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                // Not used
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Not used
            }
        });
        
        // Add MouseMotionListener for mouse position tracking
        contentPane.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
    }
    
    // Movement state tracking using ConcurrentHashMap for thread-safe polling-based input
    // This approach works better than Swing's key bindings for simultaneous key detection
    private Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
    
    /**
     * Handles key press events.
     * 
     * @param keyCode The key code
     */
    private void handleKeyPress(int keyCode) {
        pressedKeys.add(keyCode);
        
        // Handle immediate actions
        switch (keyCode) {
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_SPACE:
                // Set jumping flag
                updateMovementInput();
                break;
            // Hotbar selection (1-9 keys)
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
            case KeyEvent.VK_6:
            case KeyEvent.VK_7:
            case KeyEvent.VK_8:
            case KeyEvent.VK_9:
                int slot = keyCode - KeyEvent.VK_1; // Convert to 0-8 range
                renderer.getHUD().getHotbar().setSelectedSlot(slot);
                break;
        }
        
        // Update movement input
        updateMovementInput();
    }
    
    /**
     * Updates the player's movement input based on current key states.
     */
    private void updateMovementInput() {
        boolean movingLeft = pressedKeys.contains(KeyEvent.VK_A);
        boolean movingRight = pressedKeys.contains(KeyEvent.VK_D);
        boolean movingForward = pressedKeys.contains(KeyEvent.VK_S);
        boolean movingBackward = pressedKeys.contains(KeyEvent.VK_W);
        boolean movingUp = pressedKeys.contains(KeyEvent.VK_E);     // Z+ movement
        boolean movingDown = pressedKeys.contains(KeyEvent.VK_Q);   // Z- movement
        boolean jumping = pressedKeys.contains(KeyEvent.VK_SPACE);


        // Set the movement input on the player
        player.setMovementInput(movingLeft, movingRight, movingForward, movingBackward, 
                               movingUp, movingDown, jumping);
    }
    

    
    /**
     * Handles key release events.
     * 
     * @param keyCode The key code
     */
    private void handleKeyRelease(int keyCode) {
        pressedKeys.remove(keyCode);
        
        // Update movement input
        updateMovementInput();
    }
    
    /**
     * Gets the current mouse X coordinate.
     * 
     * @return The mouse X coordinate
     */
    public int getMouseX() {
        return mouseX;
    }
    
    /**
     * Gets the current mouse Y coordinate.
     * 
     * @return The mouse Y coordinate
     */
    public int getMouseY() {
        return mouseY;
    }
    
    /**
     * Handles mouse click events for block interaction.
     * 
     * @param x The x coordinate of the click
     * @param y The y coordinate of the click
     * @param button The mouse button (1=left, 3=right)
     */
    private void handleMouseClick(int x, int y, int button) {
        // Convert screen coordinates to world coordinates
        Vector4DInt worldPos = screenToWorldCoordinates(x, y);
        
        if (worldPos != null) {
            if (button == 1) { // Left click - destroy block
                handleBlockDestruction(worldPos.getX(), worldPos.getY(), worldPos.getZ(), worldPos.getW());
            } else if (button == 3) { // Right click - place block
                handleBlockPlacement(worldPos.getX(), worldPos.getY(), worldPos.getZ(), worldPos.getW());
            }
        }
    }
    
    /**
     * Converts screen coordinates to world coordinates.
     * 
     * @param screenX The screen X coordinate
     * @param screenY The screen Y coordinate
     * @return The world coordinates, or null if the click is outside the grid
     */
    private Vector4DInt screenToWorldCoordinates(int screenX, int screenY) {
        // Get grid dimensions and positioning
        int gridSizePixels = renderer.getGridRenderer().getGridSizePixels();
        int gridX = (WIDTH - gridSizePixels) / 2;
        int gridY = (HEIGHT - gridSizePixels) / 2;
        
        // Check if click is within the grid bounds
        if (screenX < gridX || screenX >= gridX + gridSizePixels ||
            screenY < gridY || screenY >= gridY + gridSizePixels) {
            return null; // Click is outside the grid
        }
        
        // Convert to grid-relative coordinates
        int relativeX = screenX - gridX;
        int relativeY = screenY - gridY;
        
        // Calculate slice size in pixels (7x7 blocks per slice)
        int sliceSizePixels = gridSizePixels / SliceRenderer.getSliceSize(); // 7x7 grid of slices
        
        // Determine which slice was clicked
        int sliceX = relativeX / sliceSizePixels;
        int sliceY = relativeY / sliceSizePixels;
        
        // Ensure slice coordinates are within bounds
        if (sliceX < 0 || sliceX >= SliceRenderer.getSliceSize() || sliceY < 0 || sliceY >= SliceRenderer.getSliceSize()) {
            return null;
        }
        
        // Calculate position within the slice
        int sliceRelativeX = relativeX % sliceSizePixels;
        int sliceRelativeY = relativeY % sliceSizePixels;
        
        // Calculate block size within slice (7x7 blocks per slice)
        int blockSize = sliceSizePixels / SliceRenderer.getSliceSize();
        
        // Determine which block within the slice was clicked
        int blockX = sliceRelativeX / blockSize;
        int blockY = sliceRelativeY / blockSize;
        
        // Ensure block coordinates are within bounds
        if (blockX < 0 || blockX >= SliceRenderer.getSliceSize() || blockY < 0 || blockY >= SliceRenderer.getSliceSize()) {
            return null;
        }
        
        // Get the world coordinates for the center of the clicked slice
        Vector4D sliceCenterWorld = camera.getSliceCenterWorldCoord(sliceX, sliceY);
        
        // Convert block position within slice to world coordinates
        // Block coordinates within slice: (0,0) is top-left, (6,6) is bottom-right
        // World coordinates: slice center is at (sliceCenterWorld.x, sliceCenterWorld.y)
        // Blocks are offset from -3 to +3 relative to slice center
        int worldX = (int) Math.floor(sliceCenterWorld.getX()) - SliceRenderer.getSliceCenter() + blockX;
        int worldY = (int) Math.floor(sliceCenterWorld.getY()) + SliceRenderer.getSliceCenter()  - blockY; // Y is flipped in screen coordinates
        int worldZ = (int) Math.floor(sliceCenterWorld.getZ());
        int worldW = (int) Math.floor(sliceCenterWorld.getW());
        
        return new Vector4DInt(worldX, worldY, worldZ, worldW);
    }
     
     /**
     * Handles block destruction at the specified world coordinates.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     */
    private void handleBlockDestruction(int x, int y, int z, int w) {
        // Create Vector4DInt for the position
        Vector4DInt position = new Vector4DInt(x, y, z, w);
        
        // Get the block at this position
        Block block = world.getBlock(position);
        
        // Check if there's a block at this position (not null and not air)
        if (block != null && !block.isAir()) {
            // Get the block type before destroying it
            byte blockType = block.getType();
            
            // Remove the block from the world by setting it to air
            world.setBlock(position, new Block(Block.TYPE_AIR));
            
            // Add the block to the player's inventory
            player.getInventory().addItem(blockType, 1);
            
            System.out.println("Destroyed block at (" + x + ", " + y + ", " + z + ", " + w + ")");
        }
    }
     
     /**
     * Handles block placement at the specified world coordinates.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     */
    private void handleBlockPlacement(int x, int y, int z, int w) {
        // Create Vector4DInt for the position
        Vector4DInt position = new Vector4DInt(x, y, z, w);
        
        // Get the block at this position
        Block block = world.getBlock(position);
        
        // Check if the position is empty (null or air)
        if (block == null || block.isAir()) {
            // Get the selected item from the hotbar
            com.adventure4d.computation.modules.Item selectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
            
            if (selectedItem != null && selectedItem.getCount() > 0) {
                // Check if there's an adjacent block (adjacency validation)
                if (hasAdjacentBlock(x, y, z, w)) {
                    // Place the block
                    world.setBlock(position, new Block(selectedItem.getType()));
                    
                    // Remove one item from inventory
                    player.getInventory().removeItem(selectedItem.getType(), 1);
                    
                    System.out.println("Placed block at (" + x + ", " + y + ", " + z + ", " + w + ")");
                } else {
                    System.out.println("Cannot place block - no adjacent blocks found");
                }
            } else {
                System.out.println("No item selected or item count is 0");
            }
        } else {
            System.out.println("Cannot place block - position is occupied");
        }
    }
     
     /**
     * Checks if there's at least one adjacent block to the specified position.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     * @return true if there's an adjacent block, false otherwise
     */
    private boolean hasAdjacentBlock(int x, int y, int z, int w) {
        // Check all 8 adjacent positions in 4D space (±1 in each dimension)
        int[][] offsets = {
            {-1, 0, 0, 0}, {1, 0, 0, 0},  // X axis
            {0, -1, 0, 0}, {0, 1, 0, 0},  // Y axis
            {0, 0, -1, 0}, {0, 0, 1, 0},  // Z axis
            {0, 0, 0, -1}, {0, 0, 0, 1}   // W axis
        };
        
        for (int[] offset : offsets) {
            int checkX = x + offset[0];
            int checkY = y + offset[1];
            int checkZ = z + offset[2];
            int checkW = w + offset[3];
            
            // Create Vector4DInt for the adjacent position
            Vector4DInt adjacentPos = new Vector4DInt(checkX, checkY, checkZ, checkW);
            
            // Get the block at the adjacent position
            Block adjacentBlock = world.getBlock(adjacentPos);
            
            // Check if there's a solid block (not null and not air)
            if (adjacentBlock != null && !adjacentBlock.isAir()) {
                return true;
            }
        }
        
        return false;
    }
     
     /**
      * Starts the game loop.
      */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        run();
    }
    
    /**
     * Main game loop.
     */
    private void run() {
        System.out.println("Game is running...");
        
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while (delta >= 1) {
                update(1.0 / amountOfTicks);
                delta--;
            }
            
            render();
            frames++;
            
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
            
            // Add a small delay to prevent CPU overuse
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        cleanup();
    }
    

    
    /**
     * Updates the game state.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    private void update(double deltaTime) {
        // Update movement input continuously for smooth movement
        updateMovementInput();
        

    
        // Update player physics (gravity, collision detection, etc.)
        player.update(deltaTime, world);
        
        // Sync camera to follow player - keep player centered
        Vector4D playerPos = player.getPosition();
        camera.setWorldOffset(playerPos);
        
        // Update the world
        world.update(deltaTime);
    }
    
    /**
     * Renders the game.
     */
    private void render() {
        // Render the world using our renderer with camera and player
        renderer.render(world, camera, player, mouseX, mouseY);
    }
    
    /**
     * Stops the game.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        System.out.println("Game stopped");
    }
    
    /**
     * Cleans up resources.
     */
    private void cleanup() {
        renderer.dispose();
        System.out.println("Game resources cleaned up");
    }
    
    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.init();
        game.start();
    }
}