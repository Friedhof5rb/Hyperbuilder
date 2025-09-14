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

import me.friedhof.hyperbuilder.computation.modules.items.blocks.AirItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.Block;
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
import me.friedhof.hyperbuilder.computation.modules.DroppedItem;
import me.friedhof.hyperbuilder.computation.modules.Entity;
import me.friedhof.hyperbuilder.computation.modules.interfaces.EntityInWay;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.SmelterInventory;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.SmelterItem;
import me.friedhof.hyperbuilder.computation.modules.items.blocks.SmelterPoweredItem;

import java.util.ArrayList;

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
    private static final String VERSION = MainMenu.VERSION;
    
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
    private static final long BASE_BLOCK_BREAK_TIME = 11000; // Base time to break a block (1 second)
    private long currentBlockBreakTime = BASE_BLOCK_BREAK_TIME; // Current break time for the block being broken
    private float breakingProgress = 0.0f;
    private boolean leftMousePressed = false; // Track if left mouse button is held down
    private BaseItem lastSelectedItem = null; // Track the last selected item to detect switches
    
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
    
    // Drop rate limiting
    private long lastDropTime = 0;
    private static final long MIN_DROP_INTERVAL = 250; // 250ms minimum between drops
    
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
        /* 
        inventory.addItem(Material.STONE_PICKAXE, 1);
        inventory.addItem(Material.STONE_SHOVEL, 1);
        inventory.addItem(Material.SMELTER, 5);
        inventory.addItem(Material.IRON_ORE, 10);
        inventory.addItem(Material.COPPER_ORE, 10);
        inventory.addItem(Material.COAL, 10);
        */
        inventory.addItem(Material.WATER, 64);


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
                world.addEntity(player);
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
                // Update hotbar hover detection
                renderer.getHUD().getHotbar().updateMousePosition(mouseX, mouseY);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                // Update hotbar hover detection
                renderer.getHUD().getHotbar().updateMousePosition(mouseX, mouseY);
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
                    // Use SwingUtilities.invokeLater to ensure the resize is fully processed
                    // before updating HUD dimensions, preventing coordinate system mismatches
                    SwingUtilities.invokeLater(() -> {
                        renderer.updateDimensions(newWidth, newHeight);
                    });
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
                  if (renderer.getHUD().getSmelterGUI().isVisible()) {
                    renderer.getHUD().getSmelterGUI().setVisible(false);
                    renderer.getHUD().getInventoryUI().setVisible(false);
                } else {
                    renderer.getHUD().getInventoryUI().setVisible(false);
                }
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
                resetBreakingProgressOnItemSwitch();
                break;
            case KeyEvent.VK_I:
                // Toggle inventory visibility
                // If smelter GUI is open, close both smelter and inventory
                if (renderer.getHUD().getSmelterGUI().isVisible()) {
                    renderer.getHUD().getSmelterGUI().setVisible(false);
                    renderer.getHUD().getInventoryUI().setVisible(false);
                } else {
                    renderer.getHUD().getInventoryUI().toggleVisibility();
                }
                break;
            case KeyEvent.VK_S:
                // Manual save with Ctrl+S
                if (pressedKeys.contains(KeyEvent.VK_CONTROL)) {
                    manualSave();
                }
                break;
            case KeyEvent.VK_C:
                dropSelectedItem();
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
     * Handles mouse wheel events for hotbar slot cycling and zoom functionality.
     * 
     * @param wheelRotation The wheel rotation amount (negative = up, positive = down)
     */
    private void handleMouseWheel(int wheelRotation) {
        // Check if Ctrl is pressed for zoom functionality
        if (pressedKeys.contains(KeyEvent.VK_CONTROL)) {
            // Handle zoom functionality
            boolean zoomChanged = false;
            if (wheelRotation < 0) {
                // Wheel up with Ctrl - zoom in (decrease slice size)
                zoomChanged = SliceRenderer.zoomIn();
            } else {
                // Wheel down with Ctrl - zoom out (increase slice size)
                zoomChanged = SliceRenderer.zoomOut();
            }
            
            // If zoom changed, recalculate block size and update renderer
             if (zoomChanged && renderer != null) {
                 // Get current window dimensions
                 int windowWidth = renderer.getFrame().getContentPane().getWidth();
                 int windowHeight = renderer.getFrame().getContentPane().getHeight();
                 
                 // Recalculate block size with new slice size
                 SliceRenderer.setDynamicBlockSize(windowWidth, windowHeight);
                 
                 // Update GridRenderer to handle slice size changes
                 renderer.getGridRenderer().updateSliceSize();
                 
                 // Update renderer dimensions to apply the new block size
                 renderer.updateDimensions(windowWidth, windowHeight);
             }
        } else {
            // Handle hotbar slot cycling (original functionality)
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
            
            // Reset block breaking progress when switching items
            resetBreakingProgressOnItemSwitch();
        }
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
        // Check if there's a dragged item and click is outside inventory bounds
        BaseItem draggedItem = renderer.getHUD().getInventoryUI().getDraggedItem();
        if (draggedItem != null && !renderer.getHUD().getInventoryUI().isWithinInventoryBounds(x, y) && 
            !renderer.getHUD().getSmelterGUI().isWithinBounds(x, y)) {
            // Drop the item into the world
            if (button == 1) { // Left click - drop whole stack
                dropItemIntoWorld(draggedItem);
                renderer.getHUD().getInventoryUI().clearDraggedItem();
            } else if (button == 3) { // Right click - drop single item
                BaseItem singleItem = draggedItem.withCount(1);
                dropItemIntoWorld(singleItem);
                
                // Update dragged item count (this will clear if count becomes 0)
                renderer.getHUD().getInventoryUI().updateDraggedItemCount(draggedItem.getCount() - 1);
            }
            return;
        }
        
        // Check if the smelter GUI handles the click
        if (renderer.getHUD().getSmelterGUI().handleMouseClick(x, y, button, player.getInventory(), renderer.getHUD().getInventoryUI())) {
            return; // Smelter GUI handled the click, don't process other interactions
        }
        
        // Check if the inventory UI handles the click
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
        // Check if the inventory UI handles the mouse release (for crafting UI slider)
        if (renderer.getHUD().getInventoryUI().isVisible()) {
            if (renderer.getHUD().getInventoryUI().getCraftingUI() != null) {
                renderer.getHUD().getInventoryUI().getCraftingUI().handleMouseRelease(new MouseEvent(
                    new java.awt.Component() {}, 
                    MouseEvent.MOUSE_RELEASED, 
                    System.currentTimeMillis(), 
                    0, x, y, 1, false, button
                ));
            }
        }
        
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
            
            // Calculate break time based on selected tool and block type
            currentBlockBreakTime = calculateBlockBreakTime(block);
            
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
            breakingProgress = Math.min(1.0f, (float) elapsedTime / currentBlockBreakTime);
            
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
                world.setBlock(breakingBlockPos, new AirItem());
                
                // Apply durability damage to the selected tool if it's a tool
                BaseItem selectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
                if (selectedItem instanceof IsTool) {
                    IsTool tool = (IsTool) selectedItem;
                    tool.damage(1); // Take 1 durability damage per block broken
                    
                    // Check if tool is broken (durability <= 0)
                    if (tool.getCurrentDurability() <= 0) {
                        // Remove the broken tool from inventory
                        int selectedSlot = renderer.getHUD().getHotbar().getSelectedSlot();
                        player.getInventory().setItem(selectedSlot,null );
                    }
                }
                // If a grass block was broken, also break any grass on top of it
                if (Material.GRASS_BLOCK.equals(blockId)) {
                    Vector4DInt abovePos = new Vector4DInt(
                        breakingBlockPos.getX(),
                        breakingBlockPos.getY() + 1,
                        breakingBlockPos.getZ(),
                        breakingBlockPos.getW()
                    );
                    Block blockAbove = world.getBlock(abovePos);
                    if (blockAbove != null && Material.GRASS.equals(blockAbove.getBlockId())) {
                        // Remove the grass block above
                        world.setBlock(abovePos, new AirItem());
                        
                        // Drop plant fiber instead of grass with probability
                        Vector4D grassDropPos = new Vector4D(
                            abovePos.getX() + 0.5,
                            abovePos.getY() + 0.5,
                            abovePos.getZ() + 0.5,
                            abovePos.getW() + 0.5
                        );
                        block.drops(selectedItem);
                        ArrayList<BaseItem> blockItems = blockAbove.drops(selectedItem);
                        for(BaseItem item : blockItems){
                            DroppedItem droppedBlock = new DroppedItem(world.getNextEntityId(), grassDropPos, item);
                            world.addEntity(droppedBlock);
                        }
                       

                    }
                }
                
                // Calculate drop position (center of the broken block)
                Vector4D dropPos = new Vector4D(
                    breakingBlockPos.getX() + 0.5,
                    breakingBlockPos.getY() + 0.5,
                    breakingBlockPos.getZ() + 0.5,
                    breakingBlockPos.getW() + 0.5
                );
                
                ArrayList<BaseItem> blockItems = block.drops(selectedItem);
                if (blockItems != null) {
                    for(BaseItem item : blockItems){
                        DroppedItem droppedBlock = new DroppedItem(world.getNextEntityId(), dropPos, item);
                        world.addEntity(droppedBlock);
                    }
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
        
        // Check for smelter interactions first
        if (block != null && (block.getBlockId() == Material.SMELTER || block.getBlockId() == Material.SMELTER_POWERED)) {
            handleSmelterInteraction(x, y, z, w, block);
            return;
        }
        
        // Get the selected item from the hotbar first to determine what block will be placed
        BaseItem selectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
        
        if (selectedItem != null && selectedItem.getCount() > 0) {
            // Create block from the placeable item to check its properties
            Block blockToPlace = ItemRegistry.createBlock(selectedItem.getItemId());
            if (blockToPlace == null) {
                return;
            }
            
            // Check if the position is empty, no entities are in the way (for solid blocks), and is in line of sight
            if ((block == null || block.canPlaceAt(x,y,z,w, world)) && !checkEntityInWayAtPosition(x, y, z, w, blockToPlace) && isInSightOfPlayer(x, y, z, w)) {
                // Check placement restrictions for grass and saplings
                if (selectedItem.getItemId() == Material.GRASS || selectedItem.getItemId() == Material.SAPLING) {
                    // Check if the block below is a grass block
                    Block blockBelow = world.getBlock(new Vector4DInt(x, y - 1, z, w));
                    if (blockBelow == null || !Material.GRASS_BLOCK.equals(blockBelow.getBlockId())) {
                        return; // Cannot place grass or saplings on non-grass blocks
                    }
                }
                
                // Check if there's an adjacent block (adjacency validation)
                if (hasAdjacentBlock(x, y, z, w)) {
                    world.setBlock(new Vector4DInt(x, y, z, w), blockToPlace);
                    
                    // Remove one item from inventory
                    player.getInventory().removeItem(selectedItem.getItemId(), 1);
                }
            }
        }
    }
    
    /**
     * Checks if any entities implementing EntityInWay interface are blocking the specified block position.
     * Only checks for entity collision if the block being placed is solid.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     * @param blockToPlace The block that is being placed
     * @return true if any EntityInWay entity is blocking this position and the block is solid
     */
    public boolean checkEntityInWayAtPosition(int x, int y, int z, int w, Block blockToPlace) {
        // Only check entity collision if the block being placed is solid
        if (blockToPlace == null || !blockToPlace.isSolid()) {
            return false; // Non-solid blocks can be placed inside entities
        }
        
        // Get all entities in the world
        java.util.List<Entity> entities = world.getEntitiesList();
        
        for (Entity entity : entities) {
            // Check if entity implements EntityInWay interface
            if (entity instanceof EntityInWay) {
                EntityInWay entityInWay = (EntityInWay) entity;
                
                // Check if this entity is in the way of the block position
                if (entityInWay.isInWayOfBlock(x, y, z, w)) {
                    return true; // Entity is blocking placement
                }
            }
        }
        
        return false; // No entities are blocking placement
    }
    
    /**
     * Handles smelter-specific interactions when right-clicking on a smelter block.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     * @param block The smelter block being interacted with
     */
    private void handleSmelterInteraction(int x, int y, int z, int w, Block block) {
        Vector4DInt position = new Vector4DInt(x, y, z, w);
        BaseItem selectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
        
        // Check if player is holding coal and smelter is not powered
        if (selectedItem != null && selectedItem.getItemId() == Material.COAL && block.getBlockId() == Material.SMELTER) {
            // Consume one coal and power the smelter
            player.getInventory().removeItem(Material.COAL, 1);
            
            // Get the current smelter's inventory to preserve it
            SmelterInventory currentInventory = null;
            if (block instanceof SmelterItem) {
                currentInventory = ((SmelterItem) block).getInventory();
            }
            
            // Replace smelter with powered smelter, preserving inventory
            Block poweredSmelter;
            if (currentInventory != null) {
                poweredSmelter = new SmelterPoweredItem(1, currentInventory);
            } else {
                poweredSmelter = ItemRegistry.createBlock(Material.SMELTER_POWERED);
            }
            world.setBlock(position, poweredSmelter);
            
            // Start processing if there are items in the input slot
            if (poweredSmelter instanceof SmelterPoweredItem && currentInventory != null && currentInventory.getInputItem() != null) {
                ((SmelterPoweredItem) poweredSmelter).startProcessing();
            }
            
        } else {
            // Set the smelter block reference in the GUI
            renderer.getHUD().getSmelterGUI().setSmelterBlock(world, position);
            
            // Open smelter GUI and inventory
            renderer.getHUD().getSmelterGUI().setVisible(true);
            renderer.getHUD().getInventoryUI().setVisible(true);
        }
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
     * Gets the player.
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the world.
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Gets the renderer.
     */
    public Renderer getRenderer() {
        return renderer;
    }


    /**
     * Gets the save manager.
     */
    public WorldSaveManager getSaveManager() {
        return saveManager;
    }
    
    /**
     * Drops an item into the world from inventory UI drag and drop.
     * Items are dropped near the player with a small random velocity.
     */
    private void dropItemIntoWorld(BaseItem itemToDrop) {
        if (player == null || world == null || itemToDrop == null) return;
        
        // Calculate drop position near the player
        Vector4D playerPos = player.getPosition();
        Vector4D dropPos = new Vector4D(
            playerPos.getX() + (Math.random() - 0.5) * 2.0, // Random offset within 1 block
            playerPos.getY() + 1.0, // Drop above player
            playerPos.getZ() + (Math.random() - 0.5) * 2.0, // Random offset within 1 block
            playerPos.getW()
        );
        
        // Give item a small random velocity
        Vector4D throwVelocity = new Vector4D(
            (Math.random() - 0.5) * 0.2, // Small horizontal velocity
            0.1, // Small upward velocity
            (Math.random() - 0.5) * 0.2, // Small horizontal velocity
            0
        );
        
        // Create and add the dropped item
        DroppedItem droppedItem = new DroppedItem(world.getNextEntityId(), dropPos, itemToDrop);
        droppedItem.setVelocity(throwVelocity);
        world.addEntity(droppedItem);
        
        System.out.println("Dropped item from inventory: " + itemToDrop.getItemId() + " x" + itemToDrop.getCount() + " at " + dropPos);
    }
    
    /**
     * Drops the currently selected item from the player's hotbar.
     * Items are thrown toward the block the player is hovering over.
     */
    private void dropSelectedItem() {
        if (player == null || world == null) return;
        
        // Check drop rate limiting
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDropTime < MIN_DROP_INTERVAL) {
            return; // Too soon since last drop
        }
        
        // Get the selected slot number from the hotbar
        int selectedSlot = renderer.getHUD().getHotbar().getSelectedSlot();
        BaseItem selectedItem = player.getInventory().getItem(selectedSlot);
        
        if (selectedItem != null && selectedItem.getCount() > 0) {
            // Create a copy of the item with count 1
            BaseItem itemToDrop = selectedItem.withCount(1);
            
            // Remove one item from the selected slot specifically
            if (selectedItem.getCount() > 1) {
                // Reduce count by 1
                player.getInventory().setItem(selectedSlot, selectedItem.withCount(selectedItem.getCount() - 1));
            } else {
                // Remove the item entirely (set to null)
                player.getInventory().setItem(selectedSlot, null);
            }
            
            // Calculate drop position and throw direction
            Vector4D playerPos = player.getPosition();
            Vector4D dropPos = new Vector4D(
                playerPos.getX(),
                playerPos.getY() + 0.5, // Drop at player's chest level
                playerPos.getZ(),
                playerPos.getW()
            );
            
            // Calculate throw direction toward hovered block
            Vector4D throwVelocity;
            Vector4DInt hoveredBlock = screenToWorldCoordinates(mouseX, mouseY);
            
            if (hoveredBlock != null) {
                // Throw toward the hovered block
                Vector4D targetPos = new Vector4D(
                    hoveredBlock.getX() + 0.5,
                    hoveredBlock.getY() + 0.5,
                    hoveredBlock.getZ() + 0.5,
                    hoveredBlock.getW() + 0.5
                );
                Vector4D direction = targetPos.subtract(playerPos).normalize();
                double throwSpeed = 0.3; // Very gentle drop with direction
                throwVelocity = direction.scale(throwSpeed);
            } else {
                // Fallback: gentle drop with small random offset
                throwVelocity = new Vector4D(
                    (Math.random() - 0.5) * 0.2,
                    0.1, // Very slight upward velocity
                    (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.2
                );
            }
            
            // Create dropped item entity with custom velocity
            DroppedItem droppedItem = new DroppedItem(world.getNextEntityId(), dropPos, itemToDrop);
            droppedItem.setVelocity(throwVelocity);
            
            // Add to world
            world.addEntity(droppedItem);
            
            // Update last drop time for rate limiting
            lastDropTime = currentTime;
            
            System.out.println("Dropped item: " + itemToDrop.getItemId() + " at " + dropPos + " toward " + hoveredBlock);
        }
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
    
    /**
     * Gets the currently selected tool from the player's inventory.
     * 
     * @return The selected tool, or null if no tool is selected
     */
    private IsTool getSelectedTool() {
        if (player == null || renderer == null) {
            return null;
        }
        
        BaseItem selectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
        if (selectedItem instanceof IsTool) {
            return (IsTool) selectedItem;
        }
        
        return null;
    }
    
    /**
     * Gets the effective mining speed for the current tool against a block.
     * 
     * @param block The block being mined
     * @return The mining speed multiplier
     */
    private float getEffectiveMiningSpeed(Block block) {
        IsTool tool = getSelectedTool();
        if (tool != null) {
            return tool.getMiningSpeed(block)/block.getCollisionResistance();
        }
        
        return 1.0f/block.getCollisionResistance(); // Default speed when no tool is selected
    }
    
  
    
    /**
     * Calculates the break time for a block based on the selected tool.
     * 
     * @param block The block being broken
     * @return The time in milliseconds to break the block
     */
    private long calculateBlockBreakTime(Block block) {
        float miningSpeed = getEffectiveMiningSpeed(block);
        return (long) (BASE_BLOCK_BREAK_TIME / miningSpeed);
    }
    
    /**
     * Resets block breaking progress if the player has switched to a different item.
     */
    private void resetBreakingProgressOnItemSwitch() {
        BaseItem currentSelectedItem = renderer.getHUD().getHotbar().getSelectedItem(player.getInventory());
        
        // Check if the selected item has changed
        if (lastSelectedItem != currentSelectedItem) {
            // Items are different, reset breaking progress
            if (isBreakingBlock) {
                stopBlockBreaking();
            }
            lastSelectedItem = currentSelectedItem;
        }
    }

}