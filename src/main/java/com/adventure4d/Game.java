package com.adventure4d;

import com.adventure4d.computation.modules.Vector4D;
import com.adventure4d.computation.modules.World;
import com.adventure4d.computation.modules.Player;
import com.adventure4d.rendering.modules.Renderer;
import com.adventure4d.rendering.modules.Camera;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

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
    
    /**
     * Initializes the game.
     */
    public void init() {
        System.out.println("Initializing " + GAME_TITLE + " v" + VERSION);
        
        // Create a new world
        world = new World("Test World", System.currentTimeMillis());
        
        // Create a player at a fixed center position (camera will handle world positioning)
        player = world.createPlayer("Player1", new Vector4D(0, 1, 0, 0));
        
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
     * Sets up input handling.
     */
    private void setupInput() {
        JFrame frame = renderer.getFrame();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e.getKeyCode());
            }
        });
    }
    
    // Movement state tracking
    private boolean[] keys = new boolean[256];
    
    /**
     * Handles key press events.
     * 
     * @param keyCode The key code
     */
    private void handleKeyPress(int keyCode) {
        keys[keyCode] = true;
        
        // Handle immediate actions
        switch (keyCode) {
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_SPACE:
                // Set jumping flag
                updateMovementInput();
                break;
        }
        
        // Update movement input
        updateMovementInput();
    }
    
    /**
     * Updates the player's movement input based on current key states.
     */
    private void updateMovementInput() {
        boolean movingLeft = keys[KeyEvent.VK_A];
        boolean movingRight = keys[KeyEvent.VK_D];
        boolean movingForward = keys[KeyEvent.VK_W];
        boolean movingBackward = keys[KeyEvent.VK_S];
        boolean movingUp = keys[KeyEvent.VK_E];     // Z+ movement
        boolean movingDown = keys[KeyEvent.VK_Q];   // Z- movement
        boolean jumping = keys[KeyEvent.VK_SPACE];
        
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
        keys[keyCode] = false;
        
        // Update movement input
        updateMovementInput();
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
        renderer.render(world, camera, player);
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