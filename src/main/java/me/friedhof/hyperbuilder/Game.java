package me.friedhof.hyperbuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


import me.friedhof.hyperbuilder.computation.modules.items.Block;
import me.friedhof.hyperbuilder.computation.modules.Player;
import me.friedhof.hyperbuilder.computation.modules.Vector4D;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import me.friedhof.hyperbuilder.computation.modules.World;
import me.friedhof.hyperbuilder.rendering.modules.Camera;
import me.friedhof.hyperbuilder.rendering.modules.Renderer;
import me.friedhof.hyperbuilder.rendering.modules.SliceRenderer;
import me.friedhof.hyperbuilder.rendering.modules.TextureManager;
import me.friedhof.hyperbuilder.rendering.modules.TextureManager2D;
import me.friedhof.hyperbuilder.ui.MainMenu;
import me.friedhof.hyperbuilder.save.SavedWorldInfo;
import me.friedhof.hyperbuilder.save.WorldSaveManager;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Inventory;
import me.friedhof.hyperbuilder.computation.modules.Material;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for the 4D Adventure Game.
 */
public class Game {
    
    public enum GameState {
        MENU,
        PLAYING,
        PAUSED
    }
    private static final String GAME_TITLE = "Hyperbuilder";
    private static final String VERSION = "0.1.0";
    
    // Window dimensions - automatically fit screen size with padding
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final int SCREEN_PADDING = 100; // Padding from screen edges
    private static final int WIDTH = (int) SCREEN_SIZE.getWidth() - (SCREEN_PADDING * 2);
    private static final int HEIGHT = (int) SCREEN_SIZE.getHeight() - (SCREEN_PADDING * 2);
    
    private World world;
    private Player player;
    private Renderer renderer;
    private Camera camera;
    private boolean running;
    private GameState currentState;
    private MainMenu mainMenu;
    private WorldSaveManager saveManager;
    
    // Mouse position tracking
    private int mouseX = 0;
    private int mouseY = 0;
    
    // Block breaking state
    private boolean isBreakingBlock = false;
    private Vector4DInt breakingBlockPos = null;
    private long breakingStartTime = 0;
    private static final long BLOCK_BREAK_TIME = 1000; // 1 second to break a block
    private float breakingProgress = 0.0f;
    private boolean leftMousePressed = false; // Track if left mouse button is held down
    
    /**
     * Gets whether a block is currently being broken.
     * 
     * @return true if breaking a block, false otherwise
     */
    public boolean isBreakingBlock() {
        return isBreakingBlock;
    }
    
    /**
     * Gets the current block breaking progress.
     * 
     * @return progress value from 0.0 to 1.0
     */
    public float getBreakingProgress() {
        return breakingProgress;
    }
    
    /**
     * Gets the position of the block currently being broken.
     * 
     * @return The world coordinates of the breaking block, or null if not breaking
     */
    public Vector4DInt getBreakingBlockPosition() {
        return breakingBlockPos;
    }
    
    // Auto-save functionality
    private long lastAutoSave = 0;
    private static final long AUTO_SAVE_INTERVAL = 300000; // 5 minutes in milliseconds
    
    // Rendering synchronization
    private volatile boolean renderPending = false;
    

    
    /**
     * Constructor for creating a game from menu.
     */
    public Game(String worldName, long seed, MainMenu menu) {
        this.mainMenu = menu;
        this.saveManager = new WorldSaveManager();
        this.currentState = GameState.PLAYING;


        // Create a new world
        world = new World(worldName, seed);
        initGameWorld();
        
        // Hide menu when transitioning to game
        if (mainMenu != null) {
            mainMenu.setVisible(false);
        }
    }
    
    /**
     * Constructor for loading a game from menu.
     */
    public Game(SavedWorldInfo worldInfo, MainMenu menu) {
        this.mainMenu = menu;
        this.saveManager = new WorldSaveManager();
        this.currentState = GameState.PLAYING;
        
        // Load the world
        loadGameWorld(worldInfo);
        
        // Hide menu when transitioning to game
        if (mainMenu != null) {
            mainMenu.setVisible(false);
        }
    }
    
    /**
     * Default constructor for direct game launch.
     */
    public Game() {
        this.saveManager = new WorldSaveManager();
        this.currentState = GameState.PLAYING;
    }
    
    /**
     * Initializes the game.
     */
    public void init() {
        System.out.println("Initializing " + GAME_TITLE + " v" + VERSION);
        
        if (currentState == GameState.PLAYING && world == null) {
            // Direct game launch - create default world only if no world exists
            world = new World("Test World", System.currentTimeMillis());
            initGameWorld();
        }
        
        // Initialize renderer
        renderer = new Renderer(WIDTH, HEIGHT, GAME_TITLE + " v" + VERSION);
        
        // Show the window
        renderer.getFrame().setVisible(true);
        
        if (currentState == GameState.PLAYING) {
            // Set up input handling for gameplay
            setupInput();
            
            // Force focus on the frame
            renderer.getFrame().requestFocus();
            renderer.getFrame().toFront();
            
            // Initialize auto-save timer
            lastAutoSave = System.currentTimeMillis();
        }
    }
    
    /**
     * Initializes game world components.
     */
    private void initGameWorld() {
        System.out.println("DEBUG: Starting initGameWorld()");
        System.out.println("DEBUG: World is null: " + (world == null));
        
        // Find a safe spawn position starting from Y=100 and moving down
        Vector4D safeSpawnPos = world.findSafeSpawnPosition(0.5, 0.5, 0.5);
        System.out.println("DEBUG: Found spawn position: " + safeSpawnPos);
        
        // Create a player at the safe spawn position
        player = world.createPlayer("Player1", safeSpawnPos);
        System.out.println("DEBUG: Created player: " + (player != null));
        
        // Debug: Check what blocks exist around spawn
        System.out.println("Debug: Checking blocks around spawn position:");
        for (int y = -2; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int w = -1; w <= 1; w++) {
                        Vector4DInt pos = new Vector4DInt(x, y, z, w);
                        Block block = world.getBlock(pos);
                        if (block != null && block.isSolid()) {
                System.out.println("  Block at (" + x + ", " + y + ", " + z + ", " + w + "): " + block.getBlockId());
                        }
                    }
                }
            }
        }
        
        // Give the player some starting blocks
       Inventory inventory = player.getInventory();
        inventory.addItem(Material.DIRT, 64);
        inventory.addItem(Material.STONE, 32);
        inventory.addItem(Material.WOOD_LOG, 16);
        
        // Create a camera starting at the player's initial world position
        camera = new Camera(new Vector4D(0, 1, 0, 0));
        System.out.println("DEBUG: Created camera: " + (camera != null));
        
        // Preload textures
        TextureManager.preloadTextures();
        
        // Preload 2D textures for items
        TextureManager2D.preloadItemTextures();
        
        System.out.println("Game world initialized successfully");
        System.out.println("DEBUG: Final state - world: " + (world != null) + ", player: " + (player != null) + ", camera: " + (camera != null));
    }
    
    /**
     * Loads an existing game world.
     */
    private void loadGameWorld(SavedWorldInfo worldInfo) {
        try {
            // Load the world using save manager
            WorldSaveManager.WorldSaveData saveData = saveManager.loadWorld(worldInfo);
            if (saveData != null) {
                world = saveData.getWorld();
                player = saveData.getPlayer();
                
                // Create camera at player position
                camera = new Camera(new Vector4D(0, 1, 0, 0));
                
                // Preload textures
                TextureManager.preloadTextures();
                TextureManager2D.preloadItemTextures();
                
                System.out.println("Loaded world: " + worldInfo.getName());
            } else {
                System.err.println("Failed to load world data for: " + worldInfo.getName());
            }
        } catch (Exception e) {
            System.err.println("Failed to load world: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up input handling using KeyListener for better simultaneous key support.
     * This approach uses polling-based state tracking to overcome Java Swing's
     * limitations with multiple simultaneous key presses.
     */
    private void setupInput() {
        JFrame frame = renderer.getFrame();
        frame.setFocusable(true);
        frame.setFocusTraversalKeysEnabled(false); // Disable focus traversal for Tab key
        frame.requestFocusInWindow();
        
        System.out.println("Setting up input - frame focusable: " + frame.isFocusable());
        System.out.println("Frame has focus: " + frame.hasFocus());
        
        // Get the content pane for accurate mouse coordinates
        Component contentPane = frame.getContentPane();
        
        // Add KeyListener for direct key event handling
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                //System.out.println("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()) + " (code: " + e.getKeyCode() + ")");
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
                handleMousePressed(e.getX(), e.getY(), e.getButton());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e.getX(), e.getY(), e.getButton());
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
        
        // Add MouseWheelListener for hotbar slot cycling
        contentPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                handleMouseWheel(e.getWheelRotation());
            }
        });
        
        // Add ComponentListener for manual window resizing
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Get content pane dimensions (excluding window decorations)
                int newWidth = frame.getContentPane().getWidth();
                int newHeight = frame.getContentPane().getHeight();
                
                // Only update if dimensions are valid
                if (newWidth > 0 && newHeight > 0 && renderer != null) {
                    renderer.updateDimensions(newWidth, newHeight);
                }
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
                if(renderer.getHUD().getInventoryUI().isVisible()){
                    renderer.getHUD().getInventoryUI().setVisible(false);
                }else{
                    returnToMenu();
                }
                break;
            case KeyEvent.VK_SPACE:
                // Set jumping flag
                updateMovementInput();
                break;
            case KeyEvent.VK_TAB:
                // Cycle through horizontal dimensions (X -> Z -> W -> X)
                camera.cycleHorizontalDimension();
                System.out.println("Switched to horizontal dimension: " + camera.getHorizontalDimension().getDisplayName());
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
                renderer.getHUD().getHotbar().setSelectedSlot(slot, player.getInventory());
                break;
            case KeyEvent.VK_F:
                // Toggle inventory visibility
                renderer.getHUD().getInventoryUI().toggleVisibility();
                break;
            case KeyEvent.VK_S:
                // Manual save with Ctrl+S
                if (pressedKeys.contains(KeyEvent.VK_CONTROL)) {
                    manualSave();
                }
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
     * Handles mouse wheel events for hotbar slot cycling.
     * 
     * @param wheelRotation The wheel rotation amount (negative = up, positive = down)
     */
    private void handleMouseWheel(int wheelRotation) {
        // Get current hotbar
        me.friedhof.hyperbuilder.rendering.modules.Hotbar hotbar = renderer.getHUD().getHotbar();
        
        // Get current selected slot
        int currentSlot = hotbar.getSelectedSlot();
        
        // Calculate new slot (wheel up = previous slot, wheel down = next slot)
        int newSlot;
        if (wheelRotation < 0) {
            // Wheel up - go to previous slot
            newSlot = (currentSlot - 1 + 9) % 9; // Wrap around from 0 to 8
        } else {
            // Wheel down - go to next slot
            newSlot = (currentSlot + 1) % 9; // Wrap around from 8 to 0
        }
        
        // Set the new selected slot
        hotbar.setSelectedSlot(newSlot, player.getInventory());
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
     * Gets the camera instance.
     * 
     * @return The camera
     */
    public Camera getCamera() {
        return camera;
    }
    
    
    /**
     * Handles mouse press events for UI and block interaction.
     * 
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     * @param button The mouse button (1=left, 3=right)
     */
    private void handleMousePressed(int x, int y, int button) {
        // First, check if the inventory UI handles the click
        if (renderer.getHUD().getInventoryUI().handleMouseClick(x, y, button, player.getInventory())) {
            return; // Inventory UI handled the click, don't process block interaction
        }
        
        if (button == 1) { // Left click - set flag for continuous breaking
            leftMousePressed = true;
            // Convert screen coordinates to world coordinates for block interaction
            Vector4DInt worldPos = screenToWorldCoordinates(x, y);
            if (worldPos != null) {
                startBlockBreaking(worldPos.getX(), worldPos.getY(), worldPos.getZ(), worldPos.getW());
            }
        } else if (button == 3) { // Right click - place block
            Vector4DInt worldPos = screenToWorldCoordinates(x, y);
            if (worldPos != null) {
                handleBlockPlacement(worldPos.getX(), worldPos.getY(), worldPos.getZ(), worldPos.getW());
            }
        }
    }
    
    /**
     * Handles mouse release events.
     * 
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     * @param button The mouse button (1=left, 3=right)
     */
    private void handleMouseReleased(int x, int y, int button) {
        if (button == 1) { // Left click released - stop breaking block
            leftMousePressed = false;
            stopBlockBreaking();
        }
    }
    
    /**
     * Starts the block breaking process at the specified coordinates.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     */
    private void startBlockBreaking(int x, int y, int z, int w) {
        Vector4DInt position = new Vector4DInt(x, y, z, w);
        Block block = world.getBlock(position);
        
        // Check if there's a block to break and if it's in sight
        if (block != null && block.isBreakable() && isInSightOfPlayer(x, y, z, w)) {
            // If already breaking a different block, stop the previous one
            if (isBreakingBlock && !position.equals(breakingBlockPos)) {
                stopBlockBreaking();
            }
            
            // Start breaking this block
            isBreakingBlock = true;
            breakingBlockPos = position;
            breakingStartTime = System.currentTimeMillis();
            breakingProgress = 0.0f;
            
    
        }
    }
    
    /**
     * Stops the block breaking process and resets progress.
     */
    private void stopBlockBreaking() {
        if (isBreakingBlock) {
            isBreakingBlock = false;
            breakingBlockPos = null;
            breakingProgress = 0.0f;
        }
    }
    
    /**
     * Updates the block breaking progress and completes breaking if time elapsed.
     * Also handles continuous breaking when left mouse is held down.
     */
    private void updateBlockBreaking() {
        // If left mouse is pressed, check for new blocks under cursor
        if (leftMousePressed) {
            Vector4DInt currentMouseBlock = screenToWorldCoordinates(mouseX, mouseY);
            
            // If we're hovering over a different block, switch to it
            if (currentMouseBlock != null && !currentMouseBlock.equals(breakingBlockPos)) {
                Block block = world.getBlock(currentMouseBlock);
                if (block != null && block.isBreakable() && isInSightOfPlayer(currentMouseBlock.getX(), currentMouseBlock.getY(), currentMouseBlock.getZ(), currentMouseBlock.getW())) {
                    // Start breaking the new block
                    startBlockBreaking(currentMouseBlock.getX(), currentMouseBlock.getY(), currentMouseBlock.getZ(), currentMouseBlock.getW());
                }
            }
        }
        
        if (isBreakingBlock && breakingBlockPos != null) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - breakingStartTime;
            
            // Calculate progress (0.0 to 1.0)
            breakingProgress = Math.min(1.0f, (float) elapsedTime / BLOCK_BREAK_TIME);
            
            // Check if breaking is complete
            if (breakingProgress >= 1.0f) {
                // Complete the block breaking
                completeBlockBreaking();
            }
        }
    }
    
    /**
     * Completes the block breaking process by destroying the block.
     */
    private void completeBlockBreaking() {
        if (breakingBlockPos != null) {
            // Get the block type before destroying it
            Block block = world.getBlock(breakingBlockPos);
            if (block != null && block.isBreakable()) {
                Material blockId = block.getBlockId();
                
                // Remove the block from the world by setting it to air
                world.setBlock(breakingBlockPos, new Block(Material.AIR));
                
                // Handle custom drops for leaves
                if (Material.LEAVES.equals(blockId)) {
                    // Random chance for sapling drop (10% chance)
                    if (Math.random() < 0.1) {
                        player.getInventory().addItem(Material.SAPLING, 1);
                    }
                    // Random chance for sticks drop (20% chance)
                    if (Math.random() < 0.2) {
                        player.getInventory().addItem(Material.STICKS, 1);
                    }
                    // Note: No leaves are added to inventory
                } else {
                    // Add the block to the player's inventory for all other blocks
                    player.getInventory().addItem(blockId, 1);
                }
                
            }
        }
        
        // Reset breaking state but continue if left mouse is still pressed
        isBreakingBlock = false;
        breakingBlockPos = null;
        breakingProgress = 0.0f;
        
        // If left mouse is still pressed, immediately check for next block
        if (leftMousePressed) {
            Vector4DInt nextBlock = screenToWorldCoordinates(mouseX, mouseY);
            if (nextBlock != null) {
                Block block = world.getBlock(nextBlock);
                if (block != null && block.isSolid() && isInSightOfPlayer(nextBlock.getX(), nextBlock.getY(), nextBlock.getZ(), nextBlock.getW())) {
                    startBlockBreaking(nextBlock.getX(), nextBlock.getY(), nextBlock.getZ(), nextBlock.getW());
                }
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
    public Vector4DInt screenToWorldCoordinates(int screenX, int screenY) {
        // Get grid dimensions and positioning
        int gridSizePixels = renderer.getGridRenderer().getGridSizePixels();
        // Use actual current window dimensions instead of static constants
        int currentWidth = renderer.getFrame().getContentPane().getWidth();
        int currentHeight = renderer.getFrame().getContentPane().getHeight();
        int gridX = (currentWidth - gridSizePixels) / 2;
        int gridY = (currentHeight - gridSizePixels) / 2;
        
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
        int sliceHorizontal = relativeX / sliceSizePixels;
        int sliceVertical = relativeY / sliceSizePixels;
        
        // Ensure slice coordinates are within bounds
        if (sliceHorizontal < 0 || sliceHorizontal >= SliceRenderer.getSliceSize() || sliceVertical < 0 || sliceVertical >= SliceRenderer.getSliceSize()) {
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
        Vector4D sliceCenterWorld = camera.getSliceCenterWorldCoord(sliceHorizontal, sliceVertical);
        
        // Convert block position within slice to world coordinates
        // Block coordinates within slice: (0,0) is top-left, (6,6) is bottom-right
        // World coordinates: slice center is at (sliceCenterWorld.x, sliceCenterWorld.y)
        // Blocks are offset from -3 to +3 relative to slice center




        int worldX, worldZ, worldW;
        int worldY = (int) Math.floor(sliceCenterWorld.getY()) + SliceRenderer.getSliceCenter() - blockY; // Y is flipped in screen coordinates

        // Map coordinates consistently with Camera and SliceRenderer
        // The viewing plane shows horizontal dimension + Y, blocks vary within that plane
        switch (camera.getHorizontalDimension()) {
            case X:
                // X mode: viewing X-Y plane, slice grid represents Z and W (fixed for this slice)
                worldX = (int) Math.floor(sliceCenterWorld.getX()) - SliceRenderer.getSliceCenter() + blockX;
                worldZ = (int) Math.floor(sliceCenterWorld.getZ());
                worldW = (int) Math.floor(sliceCenterWorld.getW());
                break;
            case Z:
                // Z mode: viewing Z-Y plane, slice grid represents X and W (fixed for this slice)
                worldX = (int) Math.floor(sliceCenterWorld.getX());
                worldZ = (int) Math.floor(sliceCenterWorld.getZ()) - SliceRenderer.getSliceCenter() + blockX;
                worldW = (int) Math.floor(sliceCenterWorld.getW());
                break;
            case W:
                // W mode: viewing W-Y plane, slice grid represents X and Z (fixed for this slice)
                worldX = (int) Math.floor(sliceCenterWorld.getX());
                worldZ = (int) Math.floor(sliceCenterWorld.getZ());
                worldW = (int) Math.floor(sliceCenterWorld.getW()) - SliceRenderer.getSliceCenter() + blockX;
                break;
            default:
                // Default to X mode: viewing X-Y plane
                worldX = (int) Math.floor(sliceCenterWorld.getX()) - SliceRenderer.getSliceCenter() + blockX;
                worldZ = (int) Math.floor(sliceCenterWorld.getZ());
                worldW = (int) Math.floor(sliceCenterWorld.getW());
                break;
        }



        return new Vector4DInt(worldX, worldY, worldZ, worldW);
    }
    
    /**
     * Converts world coordinates to screen coordinates.
     * This is the reverse of screenToWorldCoordinates.
     * 
     * @param worldPos The world coordinates
     * @return The screen coordinates as a Point, or null if not visible
     */
    public java.awt.Point worldToScreenCoordinates(Vector4DInt worldPos) {
        // Get grid dimensions and positioning
        int gridSizePixels = renderer.getGridRenderer().getGridSizePixels();
        // Use actual current window dimensions instead of static constants
        int currentWidth = renderer.getFrame().getContentPane().getWidth();
        int currentHeight = renderer.getFrame().getContentPane().getHeight();
        int gridX = (currentWidth - gridSizePixels) / 2;
        int gridY = (currentHeight - gridSizePixels) / 2;
        
        // Calculate slice size in pixels (7x7 blocks per slice)
        int sliceSizePixels = gridSizePixels / SliceRenderer.getSliceSize();
        
        // Calculate block size within slice (7x7 blocks per slice)
        int blockSize = sliceSizePixels / SliceRenderer.getSliceSize();
        
        // Find which slice this world position belongs to
        // We need to reverse the coordinate mapping logic
        int sliceHorizontal = -1, sliceVertical = -1;
        int blockX = -1, blockY = -1;
        
        // Check all slices to find the one containing this world position
        for (int sh = 0; sh < SliceRenderer.getSliceSize(); sh++) {
            for (int sv = 0; sv < SliceRenderer.getSliceSize(); sv++) {
                Vector4D sliceCenterWorld = camera.getSliceCenterWorldCoord(sh, sv);
                
                // Check if this world position could be in this slice
                boolean inThisSlice = false;
                int testBlockX = -1, testBlockY = -1;
                
                // Reverse the coordinate mapping based on camera mode
                switch (camera.getHorizontalDimension()) {
                    case X:
                        // X mode: viewing X-Y plane, slice grid represents Z and W
                        if ((int) Math.floor(sliceCenterWorld.getZ()) == worldPos.getZ() &&
                            (int) Math.floor(sliceCenterWorld.getW()) == worldPos.getW()) {
                            testBlockX = worldPos.getX() - (int) Math.floor(sliceCenterWorld.getX()) + SliceRenderer.getSliceCenter();
                            testBlockY = SliceRenderer.getSliceCenter() - (worldPos.getY() - (int) Math.floor(sliceCenterWorld.getY()));
                            inThisSlice = true;
                        }
                        break;
                    case Z:
                        // Z mode: viewing Z-Y plane, slice grid represents X and W
                        if ((int) Math.floor(sliceCenterWorld.getX()) == worldPos.getX() &&
                            (int) Math.floor(sliceCenterWorld.getW()) == worldPos.getW()) {
                            testBlockX = worldPos.getZ() - (int) Math.floor(sliceCenterWorld.getZ()) + SliceRenderer.getSliceCenter();
                            testBlockY = SliceRenderer.getSliceCenter() - (worldPos.getY() - (int) Math.floor(sliceCenterWorld.getY()));
                            inThisSlice = true;
                        }
                        break;
                    case W:
                        // W mode: viewing W-Y plane, slice grid represents X and Z
                        if ((int) Math.floor(sliceCenterWorld.getX()) == worldPos.getX() &&
                            (int) Math.floor(sliceCenterWorld.getZ()) == worldPos.getZ()) {
                            testBlockX = worldPos.getW() - (int) Math.floor(sliceCenterWorld.getW()) + SliceRenderer.getSliceCenter();
                            testBlockY = SliceRenderer.getSliceCenter() - (worldPos.getY() - (int) Math.floor(sliceCenterWorld.getY()));
                            inThisSlice = true;
                        }
                        break;
                }
                
                // Check if the block coordinates are valid within the slice
                if (inThisSlice && testBlockX >= 0 && testBlockX < SliceRenderer.getSliceSize() &&
                    testBlockY >= 0 && testBlockY < SliceRenderer.getSliceSize()) {
                    sliceHorizontal = sh;
                    sliceVertical = sv;
                    blockX = testBlockX;
                    blockY = testBlockY;
                    break;
                }
            }
            if (sliceHorizontal != -1) break;
        }
        
        // If we couldn't find the slice, the block is not visible
        if (sliceHorizontal == -1 || sliceVertical == -1) {
            return null;
        }
        
        // Convert slice and block coordinates to screen coordinates
        int sliceScreenX = gridX + sliceHorizontal * sliceSizePixels;
        int sliceScreenY = gridY + sliceVertical * sliceSizePixels;
        
        int blockScreenX = sliceScreenX + blockX * blockSize;
        int blockScreenY = sliceScreenY + blockY * blockSize;
        
        // Return the center of the block
        return new java.awt.Point(blockScreenX + blockSize / 2, blockScreenY + blockSize / 2);
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
        
        // Check if the position is empty (null or air) and is in line of sight
        if ((block == null || !block.isSolid()) && !checkCollisionWithBlockPosition(x, y, z, w) && isInSightOfPlayer(x, y, z, w)) {
            // Get the selected item from the hotbar
            BaseItem selectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
            
            if (selectedItem != null && selectedItem.getCount() > 0) {
                // Check if there's an adjacent block (adjacency validation)
                if (hasAdjacentBlock(x, y, z, w)) {
                    // Create block from the placeable item using its properties
                    Block blockToPlace = ItemRegistry.createBlockFromItem(selectedItem);
                    if (blockToPlace != null) {
                        world.setBlock(new Vector4DInt(x, y, z, w), blockToPlace);
                    } else {
                        return;
                    }
                    
                    // Remove one item from inventory
                    player.getInventory().removeItem(selectedItem.getItemId(), 1);
                    
                }
            } 
        }
    }


    public boolean checkCollisionWithBlockPosition(int x, int y, int z, int w){
         // Calculate player's bounding box at the given position
        double minX = player.getPosition().getX() - (player.getSizeX() / 2.0);
        double maxX = player.getPosition().getX() + (player.getSizeX() / 2.0);
        double minY = player.getPosition().getY() - (player.getSizeY() / 2.0);
        double maxY = player.getPosition().getY() + (player.getSizeY() / 2.0);
        double minZ = player.getPosition().getZ() - (player.getSizeZ() / 2.0);
        double maxZ = player.getPosition().getZ() + (player.getSizeZ() / 2.0);
        double minW = player.getPosition().getW() - (player.getSizeW() / 2.0);
        double maxW = player.getPosition().getW() + (player.getSizeW() / 2.0);
        // Check if player's bounding box intersects with this block
        if (player.intersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, minW, maxW, x, y, z, w)) {
            return true; // Collision detected
        }
        return false;
    
    }

    public boolean isInSightOfPlayer(int x, int y, int z, int w){
        // Get player's position
        Vector4D playerPos = player.getPosition();
        
        // Calculate the direction vector from player to target
        Vector4D targetPos = new Vector4D(x + 0.5, y + 0.5, z + 0.5, w + 0.5); // Center of target block
        Vector4D direction = targetPos.subtract(playerPos);
        
        // Calculate the distance to the target
        double distance = direction.magnitude();
        
        // If target is too close (essentially at player position), consider it visible
        if (distance < 0.1) {
            return true;
        }
        
        // Normalize the direction vector
        direction = direction.normalize();
        
        // Step size for ray marching (smaller = more accurate, larger = faster)
        double stepSize = 0.1;
        int steps = (int) Math.ceil(distance / stepSize);
        
        // March along the ray from player to target
        for (int i = 1; i < steps; i++) { // Start from 1 to skip player's position
            // Calculate current position along the ray
            Vector4D currentPos = playerPos.add(direction.scale(i * stepSize));
            
            // Convert to block coordinates
            int blockX = (int) Math.floor(currentPos.getX());
            int blockY = (int) Math.floor(currentPos.getY());
            int blockZ = (int) Math.floor(currentPos.getZ());
            int blockW = (int) Math.floor(currentPos.getW());
            
            // Skip checking the target block itself (it's allowed to be solid)
            if (blockX == x && blockY == y && blockZ == z && blockW == w) {
                continue;
            }
            
            // Check if there's a solid block at this position
            Vector4DInt blockPos = new Vector4DInt(blockX, blockY, blockZ, blockW);
            Block block = world.getBlock(blockPos);
            
            // If there's a solid block (not null and not air), the view is blocked
            if (block != null && block.isSolid()) {
                return false;
            }
        }
        
        // No obstructions found, target is visible
        return true;
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
    public boolean hasAdjacentBlock(int x, int y, int z, int w) {
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
            if (adjacentBlock != null && adjacentBlock.isSolid()) {
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
        
        // Run the game loop in a separate thread to avoid blocking the EDT
        Thread gameThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            long timer = System.currentTimeMillis();
            int frames = 0;
            final int targetFPS = 60;
            final long frameTime = 1000 / targetFPS; // 16.67ms per frame
            
            while (running) {
                long currentTime = System.currentTimeMillis();
                
                // Update game logic
                update(1.0 / targetFPS);
                
                // Render on EDT with queue management
                if (!renderPending) {
                    renderPending = true;
                    SwingUtilities.invokeLater(() -> {
                        render();
                        renderPending = false;
                    });
                }
                frames++;
                
                // Update FPS counter every second
                if (currentTime - timer >= 1000) {
                    renderer.getHUD().updateFPS(frames);
                    frames = 0;
                    timer = currentTime;
                }
                
                // Sleep to maintain target FPS
                long elapsed = System.currentTimeMillis() - currentTime;
                long sleepTime = frameTime - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            cleanup();
        });
        
        gameThread.setDaemon(true);
        gameThread.start();
    }
    

    
    /**
     * Updates the game state.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    private void update(double deltaTime) {
        switch (currentState) {
            case MENU:
                // Update menu (if needed)
                break;
            case PLAYING:
                // Update movement input continuously for smooth movement
                updateMovementInput();
                
                // Update player physics (gravity, collision detection, etc.)
                player.update(deltaTime, world);
                
                // Sync camera to follow player - keep player centered
                Vector4D playerPos = player.getPosition();
                camera.setWorldOffset(playerPos);
                
                // Update the world
                world.update(deltaTime);
                
                // Update block breaking progress
                updateBlockBreaking();
                
                // Check for periodic auto-save
                checkAutoSave();
                break;
            case PAUSED:
                // Game is paused, don't update game logic
                break;
        }
    }
    
    /**
     * Renders the game.
     */
    private void render() {
        switch (currentState) {
            case MENU:
                // Menu rendering is handled by MainMenu itself
                // Window visibility is managed by state transitions, not here
                break;
            case PLAYING:
            case PAUSED:
                // Render the world using our renderer with camera and player
                renderer.render(world, camera, player, this, mouseX, mouseY);
                break;
        }
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
     * Returns to the main menu.
     */
    public void returnToMenu() {
        currentState = GameState.MENU;
        // Clean up current game state if needed
        if (world != null) {
            // Auto-save before returning to menu
            try {
                saveManager.saveWorld(world, player);
                System.out.println("World auto-saved before returning to menu");
            } catch (Exception e) {
                System.err.println("Failed to auto-save world: " + e.getMessage());
            }
        }
        
        // Hide the game window
        if (renderer != null) {
            renderer.getFrame().setVisible(false);
        }
        
        // Show the main menu
        if (mainMenu != null) {
            mainMenu.setVisible(true);
            mainMenu.showMenu();
        }
    }
    
    /**
     * Gets the current game state.
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the save manager.
     */
    public WorldSaveManager getSaveManager() {
        return saveManager;
    }
    
    /**
     * Performs a manual save of the current world and player data.
     */
    private void manualSave() {
        if (world != null && player != null && saveManager != null) {
            try {
                boolean success = saveManager.saveWorld(world, player);
                if (success) {
                    System.out.println("Game saved manually");
                    // You could add a visual notification here if desired
                } else {
                    System.err.println("Manual save failed");
                }
            } catch (Exception e) {
                System.err.println("Error during manual save: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot save: world, player, or save manager is null");
        }
    }
    
    /**
     * Performs periodic auto-save if enough time has passed.
     */
    private void checkAutoSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAutoSave >= AUTO_SAVE_INTERVAL) {
            if (world != null && player != null && saveManager != null) {
                try {
                    boolean success = saveManager.saveWorld(world, player);
                    if (success) {
                        System.out.println("Auto-save completed");
                        lastAutoSave = currentTime;
                    } else {
                        System.err.println("Auto-save failed");
                    }
                } catch (Exception e) {
                    System.err.println("Error during auto-save: " + e.getMessage());
                }
            }
        }
    }
    

}