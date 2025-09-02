package com.adventure4d;

import com.adventure4d.computation.modules.Vector4D;
import com.adventure4d.computation.modules.World;
import com.adventure4d.computation.modules.Player;
import com.adventure4d.rendering.modules.Renderer;

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
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    
    private World world;
    private Player player;
    private Renderer renderer;
    private boolean running;
    
    /**
     * Initializes the game.
     */
    public void init() {
        System.out.println("Initializing " + GAME_TITLE + " v" + VERSION);
        
        // Create a new world
        world = new World("Test World", System.currentTimeMillis());
        
        // Create a player at position (0, 10, 0, 0)
        player = world.createPlayer("Player1", new Vector4D(0, 10, 0, 0));
        
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
    
    /**
     * Handles key press events.
     * 
     * @param keyCode The key code
     */
    private void handleKeyPress(int keyCode) {
        Vector4D movement = new Vector4D(0, 0, 0, 0);
        double speed = 0.5;
        
        switch (keyCode) {
            // X-Y movement (2D plane)
            case KeyEvent.VK_W:
                movement = movement.add(new Vector4D(0, -speed, 0, 0));
                break;
            case KeyEvent.VK_S:
                movement = movement.add(new Vector4D(0, speed, 0, 0));
                break;
            case KeyEvent.VK_A:
                movement = movement.add(new Vector4D(-speed, 0, 0, 0));
                break;
            case KeyEvent.VK_D:
                movement = movement.add(new Vector4D(speed, 0, 0, 0));
                break;
                
            // Z movement (3rd dimension)
            case KeyEvent.VK_Q:
                movement = movement.add(new Vector4D(0, 0, -speed, 0));
                break;
            case KeyEvent.VK_E:
                movement = movement.add(new Vector4D(0, 0, speed, 0));
                break;
                
            // W movement (4th dimension)
            case KeyEvent.VK_Z:
                movement = movement.add(new Vector4D(0, 0, 0, -speed));
                break;
            case KeyEvent.VK_C:
                movement = movement.add(new Vector4D(0, 0, 0, speed));
                break;
                
            // Exit the game
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
        }
        
        // Apply movement
        if (!movement.equals(new Vector4D(0, 0, 0, 0))) {
            player.move(movement, world);
        }
    }
    
    /**
     * Handles key release events.
     * 
     * @param keyCode The key code
     */
    private void handleKeyRelease(int keyCode) {
        // Currently not used
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
        // Update the world
        world.update(deltaTime);
    }
    
    /**
     * Renders the game.
     */
    private void render() {
        // Render the world using our renderer
        renderer.render(world, player);
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