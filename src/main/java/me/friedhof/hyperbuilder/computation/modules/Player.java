package me.friedhof.hyperbuilder.computation.modules;

/**
 * Represents a player in the 4D world.
 * Players can move, interact with blocks, and have an inventory.
 */
public class Player extends Entity {
    // Player constants - adjusted for better visual/collision alignment
    private static final double PLAYER_SIZE = 0.5;
  
    // Physics constants
    private static final double GRAVITY = -15.0; // blocks per second squared (increased for more realistic falling)
    private static final double JUMP_VELOCITY = 10.0; // blocks per second
    private static final double MOVE_SPEED = 4.0; // blocks per second
    private static final double FRICTION = 0.8; // friction coefficient
    private static final double EPSILON = 0.001; // small value to prevent floating point precision issues

    // Player state
    private String username;
    private Inventory inventory;
    private int selectedSlot;
    private boolean onGround;
    private int health;
    private int maxHealth;
    
    // Movement input state
    private boolean movingLeft;
    private boolean movingRight;
    private boolean movingForward;
    private boolean movingBackward;
    private boolean movingUp;
    private boolean movingDown;
    private boolean jumping;
    
    /**
     * Creates a new player with the specified ID, position, and username.
     * 
     * @param id The unique identifier for this player
     * @param position The initial position of this player
     * @param username The player's username
     */
    public Player(int id, Vector4D position, String username) {
        super(id, position, PLAYER_SIZE, PLAYER_SIZE, PLAYER_SIZE, PLAYER_SIZE);
        this.username = username;
        this.inventory = new Inventory(36); // 36 slots (9x4)
        this.selectedSlot = 0;
        this.onGround = false; // Start in air so gravity applies immediately
        this.health = 20;
        this.maxHealth = 20;
        
        // Ensure gravity is enabled
        setGravity(true);
    }
    
    /**
     * Gets the player's size.
     * 
     * @return The player's size
     */
    public double getSize() {
        return PLAYER_SIZE;
    }



    /**
     * Gets the player's username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the player's inventory.
     * 
     * @return The inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Gets the currently selected inventory slot.
     * 
     * @return The selected slot index
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    /**
     * Sets the currently selected inventory slot.
     * 
     * @param slot The slot index to select
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            this.selectedSlot = slot;
        }
    }
    
    /**
     * Gets the player's current health.
     * 
     * @return The health value
     */
    public int getHealth() {
        return health;
    }
    
    /**
     * Sets the player's health.
     * 
     * @param health The new health value
     */
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }
    
    /**
     * Gets the player's maximum health.
     * 
     * @return The maximum health value
     */
    public int getMaxHealth() {
        return maxHealth;
    }
    
    
    
    
    
    /**
     * Sets the player's movement input state.
     * 
     * @param movingLeft Whether the player is moving left
     * @param movingRight Whether the player is moving right
     * @param movingForward Whether the player is moving forward
     * @param movingBackward Whether the player is moving backward
     * @param movingUp Whether the player is moving up
     * @param movingDown Whether the player is moving down
     * @param jumping Whether the player is jumping
     */
    public void setMovementInput(boolean movingLeft, boolean movingRight, 
                                boolean movingForward, boolean movingBackward,
                                boolean movingUp, boolean movingDown,
                                boolean jumping) {
        this.movingLeft = movingLeft;
        this.movingRight = movingRight;
        this.movingForward = movingForward;
        this.movingBackward = movingBackward;
        this.movingUp = movingUp;
        this.movingDown = movingDown;
        this.jumping = jumping;
    }
    
    /**
     * Updates the player's state for the current tick.
     * Handles movement, physics, and collision detection.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     * @param world The world to check for collisions
     */
    public void update(double deltaTime, World world) {
        // Process movement input and apply forces
        processMovementInput(deltaTime);



        // Apply gravity if enabled
        if (hasGravity()) {
            Vector4D velocity = getVelocity();
            velocity = velocity.add(new Vector4D(0, GRAVITY * deltaTime, 0, 0));
            setVelocity(velocity);
        }
        
        // Apply friction to all velocity components
        Vector4D velocity = getVelocity();
        velocity = velocity.scale(Math.pow(FRICTION, deltaTime));
        setVelocity(velocity);
        
        // Handle movement with collision detection for all directions
        handleMovementWithCollision(deltaTime, world);
    }
    
    /**
     * Processes movement input and applies movement forces.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    private void processMovementInput(double deltaTime) {
        Vector4D currentVelocity = getVelocity();
        
        // Set horizontal movement velocity directly (X axis)
        double xVelocity = 0;
        if (movingLeft) {
            xVelocity -= MOVE_SPEED;
        }
        if (movingRight) {
            xVelocity += MOVE_SPEED;
        }
        
        // Set Z axis movement velocity directly
        double wVelocity = 0;
        if (movingForward) {
            wVelocity += MOVE_SPEED;
        }
        if (movingBackward) {
            wVelocity -= MOVE_SPEED;
        }
        
        // Set W axis movement velocity directly
        double zVelocity = 0;
        if (movingUp) {
            zVelocity += MOVE_SPEED;
        }
        if (movingDown) {
            zVelocity -= MOVE_SPEED;
        }
        
        // Process jumping (Y axis) - only if on ground
        double yVelocity = currentVelocity.getY();
        if (jumping && onGround) {
            yVelocity = JUMP_VELOCITY;
        }
        
        // Set the new velocity with direct input values for horizontal movement
        setVelocity(new Vector4D(xVelocity, yVelocity, zVelocity, wVelocity));
    }
    
    /**
     * Handles movement with collision detection for all directions.
     * Treats all directions equally without relying on onGround functionality.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     * @param world The world to check for collisions
     */
    private void handleMovementWithCollision(double deltaTime, World world) {
        Vector4D velocity = getVelocity();
        Vector4D position = getPosition();
        
        // Calculate the intended movement for this frame
        Vector4D movement = velocity.scale(deltaTime);
        
        // Handle collision detection and movement for each axis separately
        // This ensures that collision in one direction doesn't prevent movement in others
        
        // X-axis movement
        Vector4D newPosition = position.add(new Vector4D(movement.getX(), 0, 0, 0));
        if (!checkCollision(newPosition, world)) {
            position = newPosition;
        } else {
            // Stop X velocity on collision
            velocity = new Vector4D(0, velocity.getY(), velocity.getZ(), velocity.getW());
        }
        
        // Y-axis movement
        newPosition = position.add(new Vector4D(0, movement.getY(), 0, 0));
        if (!checkCollision(newPosition, world)) {
            position = newPosition;
            onGround = false; // Player is moving freely in Y direction
        } else {
            // Stop Y velocity on collision
            velocity = new Vector4D(velocity.getX(), 0, velocity.getZ(), velocity.getW());
            // Check if collision is from below (landing on ground)
            if (movement.getY() <= 0) {
                onGround = true;
            }
        }
        
        // Z-axis movement
        newPosition = position.add(new Vector4D(0, 0, movement.getZ(), 0));
        if (!checkCollision(newPosition, world)) {
            position = newPosition;
        } else {
            // Stop Z velocity on collision
            velocity = new Vector4D(velocity.getX(), velocity.getY(), 0, velocity.getW());
        }
        
        // W-axis movement
        newPosition = position.add(new Vector4D(0, 0, 0, movement.getW()));
        if (!checkCollision(newPosition, world)) {
            position = newPosition;
        } else {
            // Stop W velocity on collision
            velocity = new Vector4D(velocity.getX(), velocity.getY(), velocity.getZ(), 0);
        }
        
        // Additional ground check: test if player would collide when moving slightly down
        if (!onGround) {
            Vector4D groundTestPosition = position.add(new Vector4D(0, -EPSILON, 0, 0));
            if (checkCollision(groundTestPosition, world)) {
                onGround = true;
            }
        }
        
        // Update position and velocity
        setPosition(position);
        setVelocity(velocity);
    }
    
    /**
     * Checks if the player would collide with any blocks at the given position.
     * 
     * @param position The position to check
     * @param world The world to check for collisions
     * @return true if there would be a collision, false otherwise
     */
    private boolean checkCollision(Vector4D position, World world) {
        // Calculate player's bounding box at the given position
        double minX = position.getX() - (getSizeX() / 2.0);
        double maxX = position.getX() + (getSizeX() / 2.0);
        double minY = position.getY() - (getSizeY() / 2.0);
        double maxY = position.getY() + (getSizeY() / 2.0);
        double minZ = position.getZ() - (getSizeZ() / 2.0);
        double maxZ = position.getZ() + (getSizeZ() / 2.0);
        double minW = position.getW() - (getSizeW() / 2.0);
        double maxW = position.getW() + (getSizeW() / 2.0);
        
        

        // Calculate the range of blocks that could potentially intersect with the player
        int startX = (int) Math.floor(minX);
        int endX = (int) Math.floor(maxX);
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.floor(maxY);
        int startZ = (int) Math.floor(minZ);
        int endZ = (int) Math.floor(maxZ);
        int startW = (int) Math.floor(minW);
        int endW = (int) Math.floor(maxW);
        
        // Check each potentially intersecting block
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    for (int w = startW; w <= endW; w++) {
                        // Check if there's a solid block at this position
                         Vector4DInt blockPos = new Vector4DInt(x, y, z, w);
                         Block block = world.getBlock(blockPos);
                         if (block != null && !block.isAir()) { // Check if block exists and is not air
                             // Check if player's bounding box intersects with this block
                             if (intersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, minW, maxW, x, y, z, w)) {
                                 return true; // Collision detected
                             }
                         }
                    }
                }
            }
        }
        
        return false; // No collision
    }
    
    /**
     * Checks if the player's bounding box intersects with a block at the given coordinates.
     * 
     * @param minX Player's minimum X coordinate
     * @param maxX Player's maximum X coordinate
     * @param minY Player's minimum Y coordinate
     * @param maxY Player's maximum Y coordinate
     * @param minZ Player's minimum Z coordinate
     * @param maxZ Player's maximum Z coordinate
     * @param minW Player's minimum W coordinate
     * @param maxW Player's maximum W coordinate
     * @param blockX Block's X coordinate
     * @param blockY Block's Y coordinate
     * @param blockZ Block's Z coordinate
     * @param blockW Block's W coordinate
     * @return true if there's an intersection, false otherwise
     */
    public boolean intersectsBlock(double minX, double maxX, double minY, double maxY,
                                   double minZ, double maxZ, double minW, double maxW,
                                   int blockX, int blockY, int blockZ, int blockW) {
        // Block occupies space from (blockX, blockY, blockZ, blockW) to (blockX+1, blockY+1, blockZ+1, blockW+1)
        return (maxX > blockX && minX < blockX + 1) &&
               (maxY > blockY && minY < blockY + 1) &&
               (maxZ > blockZ && minZ < blockZ + 1) &&
               (maxW > blockW && minW < blockW + 1);
    }
    
    /**
     * Checks if a position is within the player's reach.
     * 
     * @param position The position to check
     * @return true if the position is within reach, false otherwise
     */
    private boolean isWithinReach(Vector4DInt position) {
        // Calculate the distance between the player and the position
        double distance = getPosition().distance(position.toVector4D());
        
        // Check if the distance is within reach (3 blocks)
        return distance <= 3.0;
    }
}