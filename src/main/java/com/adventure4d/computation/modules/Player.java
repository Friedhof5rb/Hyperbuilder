package com.adventure4d.computation.modules;

/**
 * Represents a player in the 4D world.
 * Players can move, interact with blocks, and have an inventory.
 */
public class Player extends Entity {
    // Player constants
    private static final double PLAYER_SIZE_X = 0.6;
    private static final double PLAYER_SIZE_Y = 1.8;
    private static final double PLAYER_SIZE_Z = 0.6;
    private static final double PLAYER_SIZE_W = 0.6;
    
    private static final double WALK_SPEED = 4.0; // blocks per second
    private static final double JUMP_FORCE = 8.0;
    private static final double GRAVITY_FORCE = -20.0;
    
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
        super(id, position, PLAYER_SIZE_X, PLAYER_SIZE_Y, PLAYER_SIZE_Z, PLAYER_SIZE_W);
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
     * Checks if the player is on the ground.
     * 
     * @return true if the player is on the ground, false otherwise
     */
    public boolean isOnGround() {
        return onGround;
    }
    
    /**
     * Sets whether the player is on the ground.
     * 
     * @param onGround true if the player is on the ground, false otherwise
     */
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
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
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    @Override
    public void update(double deltaTime) {
        // This method is called without world access, so we can't do collision detection here
        // Movement with collision detection should be handled through the move() method
        
        // Calculate movement direction based on input
        Vector4D movement = new Vector4D();
        
        if (movingRight) movement = movement.add(new Vector4D(1, 0, 0, 0));
        if (movingLeft) movement = movement.add(new Vector4D(-1, 0, 0, 0));
        if (movingForward) movement = movement.add(new Vector4D(0, 0, 1, 0));
        if (movingBackward) movement = movement.add(new Vector4D(0, 0, -1, 0));
        if (movingUp) movement = movement.add(new Vector4D(0, 0, 0, 1));
        if (movingDown) movement = movement.add(new Vector4D(0, 0, 0, -1));
        
        // Normalize movement if needed
        if (movement.magnitude() > 0) {
            movement = movement.normalize().scale(WALK_SPEED * deltaTime);
        }
        
        // Apply gravity
        Vector4D velocity = getVelocity();
        velocity = velocity.add(new Vector4D(0, GRAVITY_FORCE * deltaTime, 0, 0));
        
        // Handle jumping
        if (jumping && onGround) {
            velocity = new Vector4D(velocity.getX(), JUMP_FORCE, velocity.getZ(), velocity.getW());
            onGround = false;
        }
        
        // Update velocity
        setVelocity(velocity);
        
        // Update position (without collision detection - this will be handled in Game class)
        Vector4D currentPos = getPosition();
        Vector4D newPos = currentPos.add(velocity.scale(deltaTime));
        setPosition(newPos);
        
        // Reset jumping input
        jumping = false;
    }
    
    /**
     * Updates the player's state with world collision detection.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     * @param world The world to check collisions against
     */
    public void update(double deltaTime, World world) {
        // Calculate movement direction based on input
        Vector4D movement = new Vector4D();
        
        if (movingRight) movement = movement.add(new Vector4D(1, 0, 0, 0));
        if (movingLeft) movement = movement.add(new Vector4D(-1, 0, 0, 0));
        if (movingForward) movement = movement.add(new Vector4D(0, 0, 1, 0));
        if (movingBackward) movement = movement.add(new Vector4D(0, 0, -1, 0));
        if (movingUp) movement = movement.add(new Vector4D(0, 0, 0, 1));
        if (movingDown) movement = movement.add(new Vector4D(0, 0, 0, -1));
        
        // Normalize movement vector if it's not zero
        if (movement.magnitudeSquared() > 0.001) {
            movement = movement.normalize().scale(WALK_SPEED);
        }
        
        // Apply horizontal movement
        Vector4D velocity = getVelocity();
        velocity = new Vector4D(movement.getX(), velocity.getY(), movement.getZ(), movement.getW());
        
        // Apply jumping
        if (jumping && onGround) {
            velocity = new Vector4D(velocity.getX(), JUMP_FORCE, velocity.getZ(), velocity.getW());
            onGround = false;
        }
        
        // Apply gravity (always apply if not on ground, regardless of hasGravity check)
        if (!onGround) {
            velocity = new Vector4D(
                velocity.getX(),
                velocity.getY() + GRAVITY_FORCE * deltaTime,
                velocity.getZ(),
                velocity.getW()
            );
        }
        
        // Update velocity
        setVelocity(velocity);
        
        // Update position with collision detection
        Vector4D currentPos = getPosition();
        Vector4D newPos = currentPos.add(velocity.scale(deltaTime));
        
        // Check for ground collision
        if (velocity.getY() < 0) { // Falling
            // Check if we would hit the ground
            Vector4D testPos = new Vector4D(newPos.getX(), newPos.getY(), newPos.getZ(), newPos.getW());
            if (checkGroundCollision(testPos, world)) {
                // Stop at ground level
                newPos = new Vector4D(newPos.getX(), Math.floor(newPos.getY()) + 1, newPos.getZ(), newPos.getW());
                velocity = new Vector4D(velocity.getX(), 0, velocity.getZ(), velocity.getW());
                setVelocity(velocity);
                onGround = true;
            } else {
                onGround = false;
            }
        } else {
            onGround = false;
        }
        
        // Apply the new position
        setPosition(newPos);
        
        // Reset jumping input
        jumping = false;
    }
    
    /**
     * Moves the player in the specified direction.
     * 
     * @param direction The direction to move in (x, y, z, w)
     * @param world The world to check for collisions
     * @return true if the player moved successfully, false if blocked by collision
     */
    public boolean move(Vector4D direction, World world) {
        // Calculate the new position
        Vector4D newPosition = getPosition().add(direction);
        
        // Check for collisions
        if (!checkCollision(newPosition, world)) {
            // No collision, update position
            setPosition(newPosition);
            return true;
        }
        
        // Try to slide along axes if we hit something
        Vector4D slideX = new Vector4D(0, direction.getY(), direction.getZ(), direction.getW());
        Vector4D slideY = new Vector4D(direction.getX(), 0, direction.getZ(), direction.getW());
        Vector4D slideZ = new Vector4D(direction.getX(), direction.getY(), 0, direction.getW());
        Vector4D slideW = new Vector4D(direction.getX(), direction.getY(), direction.getZ(), 0);
        
        // Try each sliding direction
        if (!checkCollision(getPosition().add(slideX), world)) {
            setPosition(getPosition().add(slideX));
            return true;
        } else if (!checkCollision(getPosition().add(slideY), world)) {
            setPosition(getPosition().add(slideY));
            return true;
        } else if (!checkCollision(getPosition().add(slideZ), world)) {
            setPosition(getPosition().add(slideZ));
            return true;
        } else if (!checkCollision(getPosition().add(slideW), world)) {
            setPosition(getPosition().add(slideW));
            return true;
        }
        
        // Couldn't move in any direction
        return false;
    }
    
    /**
     * Checks if the player would collide with any blocks at the specified position.
     * 
     * @param position The position to check
     * @param world The world to check in
     * @return true if there would be a collision, false otherwise
     */
    private boolean checkCollision(Vector4D position, World world) {
        // Get the player's bounding box
        double minX = position.getX() - (PLAYER_SIZE_X / 2);
        double maxX = position.getX() + (PLAYER_SIZE_X / 2);
        double minY = position.getY();
        double maxY = position.getY() + PLAYER_SIZE_Y;
        double minZ = position.getZ() - (PLAYER_SIZE_Z / 2);
        double maxZ = position.getZ() + (PLAYER_SIZE_Z / 2);
        double minW = position.getW() - (PLAYER_SIZE_W / 2);
        double maxW = position.getW() + (PLAYER_SIZE_W / 2);
        
        // Check all blocks that the player might be intersecting with
        for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
            for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
                for (int z = (int) Math.floor(minZ); z <= (int) Math.floor(maxZ); z++) {
                    for (int w = (int) Math.floor(minW); w <= (int) Math.floor(maxW); w++) {
                        // Get the block at this position
                        Vector4DInt blockPos = new Vector4DInt(x, y, z, w);
                        Block block = world.getBlock(blockPos);
                        
                        // If the block is not air and not null, there's a collision
                        if (block != null && !block.isAir()) {
                            return true;
                        }
                    }
                }
            }
        }
        
        // No collision
        return false;
    }
    
    /**
     * Checks if the player would collide with the ground at the given position.
     * 
     * @param position The position to check
     * @param world The world to check against
     * @return true if there would be a ground collision, false otherwise
     */
    private boolean checkGroundCollision(Vector4D position, World world) {
        // Calculate player bounding box bottom
        double minX = position.getX() - (PLAYER_SIZE_X / 2);
        double maxX = position.getX() + (PLAYER_SIZE_X / 2);
        double minZ = position.getZ() - (PLAYER_SIZE_Z / 2);
        double maxZ = position.getZ() + (PLAYER_SIZE_Z / 2);
        double minW = position.getW() - (PLAYER_SIZE_W / 2);
        double maxW = position.getW() + (PLAYER_SIZE_W / 2);
        
        // Check blocks just below the player's feet
        int groundY = (int) Math.floor(position.getY());
        
        for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
            for (int z = (int) Math.floor(minZ); z <= (int) Math.floor(maxZ); z++) {
                for (int w = (int) Math.floor(minW); w <= (int) Math.floor(maxW); w++) {
                    Vector4DInt blockPos = new Vector4DInt(x, groundY, z, w);
                    Block block = world.getBlock(blockPos);
                    if (block != null && !block.isAir()) {
                        return true; // Ground collision detected
                    }
                }
            }
        }
        
        return false; // No ground collision
    }
    
    /**
     * Places a block in the world.
     * 
     * @param world The world to place the block in
     * @param position The position to place the block at
     * @param blockType The type of block to place
     * @return true if the block was placed, false otherwise
     */
    public boolean placeBlock(World world, Vector4DInt position, byte blockType) {
        // Check if the position is within reach
        if (isWithinReach(position)) {
            // Check if the player has the block in their inventory
            if (inventory.removeItem(new Item(blockType, 1))) {
                // Place the block
                return world.setBlock(position, new Block(blockType));
            }
        }
        return false;
    }
    
    /**
     * Breaks a block in the world.
     * 
     * @param world The world to break the block in
     * @param position The position of the block to break
     * @return true if the block was broken, false otherwise
     */
    public boolean breakBlock(World world, Vector4DInt position) {
        // Check if the position is within reach
        if (isWithinReach(position)) {
            // Get the block
            Block block = world.getBlock(position);
            if (block != null && !block.isAir()) {
                // Add the block to the player's inventory
                inventory.addItem(new Item(block.getType(), 1));
                
                // Break the block
                return world.setBlock(position, new Block(Block.TYPE_AIR));
            }
        }
        return false;
    }
    
    /**
     * Checks if a position is within the player's reach.
     * 
     * @param position The position to check
     * @return true if the position is within reach, false otherwise
     */
    private boolean isWithinReach(Vector4DInt position) {
        // Calculate the distance between the player and the position
        double distance = getPosition().distance(position.toVector4D().add(new Vector4D(0.5, 0.5, 0.5, 0.5)));
        
        // Check if the distance is within reach (3 blocks)
        return distance <= 3.0;
    }
}