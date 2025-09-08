package me.friedhof.hyperbuilder.computation.modules;

import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.interfaces.HasCollision;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Represents a dropped item entity in the world.
 * Dropped items are smaller versions of blocks/items that can be picked up by players.
 * They have physics (gravity, collision) and a despawn timer.
 */
public class DroppedItem extends Entity {
    private static final double DROPPED_ITEM_SIZE = 0.375; // Smaller than blocks (0.25 vs 1.0)
    private static final double DROPPED_ITEM_GRAVITY = 0.02; // Same gravity as player
    private static final long DESPAWN_TIME_MS = 300000; // 5 minutes in milliseconds
    private static final long PICKUP_DELAY_MS = 500; // 2 seconds pickup delay
    
    private BaseItem item; // The item this entity represents
    private int count; // Number of items in this stack
    private long spawnTime; // When this item was dropped
    private boolean shouldDespawn; // Flag to mark for removal
    private Map<Integer, Long> playerPickupTimers; // Per-player pickup timers (playerId -> timer start time)
    
    /**
     * Creates a new dropped item entity.
     * 
     * @param id The entity ID
     * @param position The spawn position
     * @param item The item this entity represents
     */
    public DroppedItem(int id, Vector4D position, BaseItem item) {
        super(id, position,DROPPED_ITEM_SIZE,DROPPED_ITEM_SIZE,DROPPED_ITEM_SIZE,DROPPED_ITEM_SIZE);
        this.item = item;
        this.count = item.getCount();
        this.spawnTime = System.currentTimeMillis();
        this.shouldDespawn = false;
        this.playerPickupTimers = new HashMap<>();
        
        // Enable gravity
        setGravity(true);
        
        // Add some random initial velocity for visual effect
        double randomX = (Math.random() - 0.5) * 0.1;
        double randomZ = (Math.random() - 0.5) * 0.1;
        double randomW = (Math.random() - 0.5) * 0.1;
        setVelocity(new Vector4D(randomX, 0.1, randomZ, randomW));
    }
    
    /**
     * Creates a new dropped item entity with specified count.
     * 
     * @param id The entity ID
     * @param position The spawn position
     * @param item The item this entity represents
     * @param count The number of items in this stack
     */
    public DroppedItem(int id, Vector4D position, BaseItem item, int count) {
         super(id, position,DROPPED_ITEM_SIZE,DROPPED_ITEM_SIZE,DROPPED_ITEM_SIZE,DROPPED_ITEM_SIZE);
        this.item = item;
        this.count = count;
        this.spawnTime = System.currentTimeMillis();
        this.shouldDespawn = false;
        this.playerPickupTimers = new HashMap<>();
        
        // Enable gravity
        setGravity(true);
        
        // Add some random initial velocity for visual effect
        double randomX = (Math.random() - 0.5) * 0.1;
        double randomZ = (Math.random() - 0.5) * 0.1;
        double randomW = (Math.random() - 0.5) * 0.1;
        setVelocity(new Vector4D(randomX, 0.1, randomZ, randomW));
    }
    
    /**
     * Updates the dropped item entity.
     * Handles physics, collision, and despawn timer.
     * 
     * @param deltaTime The time elapsed since last update
     * @param world The world this entity exists in
     */
    @Override
    public void update(double deltaTime, World world) {
        

        // Check despawn timer
        long currentTime = System.currentTimeMillis();
        if (currentTime - spawnTime >= DESPAWN_TIME_MS) {
            shouldDespawn = true;
            return;
        }
        
        // Apply gravity
        if (hasGravity()) {
            Vector4D velocity = getVelocity();
            velocity = velocity.add(new Vector4D(0, -DROPPED_ITEM_GRAVITY, 0, 0));
            setVelocity(velocity);
        }
        
        // Handle movement with collision detection
        handleMovementWithCollision(world);
        
        // Update player proximity timers
        updatePlayerProximity(world);
    }
    
    /**
     * Handles movement with collision detection for the dropped item.
     * Similar to player movement but simpler.
     * 
     * @param world The world to check collisions against
     */
    private void handleMovementWithCollision(World world) {
        Vector4D velocity = getVelocity();
        Vector4D position = getPosition();
        
        // Handle X axis movement
        Vector4D newPos = position.add(new Vector4D(velocity.getX(), 0, 0, 0));
        if (!checkCollision(world, newPos)) {
            position = newPos;
        } else {
            // Stop all horizontal movement when hitting a wall
            velocity = new Vector4D(0, velocity.getY(), 0, 0);
        }
        
        // Handle Y axis movement
        newPos = position.add(new Vector4D(0, velocity.getY(), 0, 0));
        if (!checkCollision(world, newPos)) {
            position = newPos;
        } else {
            // Stop all movement when hitting ground or ceiling
            velocity = new Vector4D(0, 0, 0, 0);
        }
        
        // Handle Z axis movement
        newPos = position.add(new Vector4D(0, 0, velocity.getZ(), 0));
        if (!checkCollision(world, newPos)) {
            position = newPos;
        } else {
            // Stop all horizontal movement when hitting a wall
            velocity = new Vector4D(0, velocity.getY(), 0, 0);
        }
        
        // Handle W axis movement
        newPos = position.add(new Vector4D(0, 0, 0, velocity.getW()));
        if (!checkCollision(world, newPos)) {
            position = newPos;
        } else {
            // Stop all horizontal movement when hitting a wall
            velocity = new Vector4D(0, velocity.getY(), 0, 0);
        }
        
        setPosition(position);
        setVelocity(velocity);
    }
    
    /**
     * Checks if the dropped item would collide with blocks at the given position.
     * 
     * @param world The world to check against
     * @param position The position to check
     * @return true if there would be a collision
     */
    private boolean checkCollision(World world, Vector4D position) {
        // Get the bounding box of the dropped item
        double minX = position.getX() - getSizeX() / 2;
        double maxX = position.getX() + getSizeX() / 2;
        double minY = position.getY() - getSizeY() / 2;
        double maxY = position.getY() + getSizeY() / 2;
        double minZ = position.getZ() - getSizeZ() / 2;
        double maxZ = position.getZ() + getSizeZ() / 2;
        double minW = position.getW() - getSizeW() / 2;
        double maxW = position.getW() + getSizeW() / 2;
        
        // Check all blocks that the dropped item might intersect with
        for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
            for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
                for (int z = (int) Math.floor(minZ); z <= (int) Math.floor(maxZ); z++) {
                    for (int w = (int) Math.floor(minW); w <= (int) Math.floor(maxW); w++) {
                        Vector4DInt blockPos = new Vector4DInt(x, y, z, w);
                        me.friedhof.hyperbuilder.computation.modules.items.Block block = world.getBlock(blockPos);
                        
                        if (block != null && block instanceof HasCollision) {
                            HasCollision collisionBlock = (HasCollision) block;
                            if (collisionBlock.isSolid()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Updates player proximity timers. Should be called during world update.
     * 
     * @param world The world to check for nearby players
     */
    public void updatePlayerProximity(World world) {
        long currentTime = System.currentTimeMillis();
       
        // Get all players in the world (assuming Player extends Entity)
        java.util.List<Entity> entities = world.getEntitiesList();
        java.util.Set<Integer> nearbyPlayerIds = new java.util.HashSet<>();
        for (Entity entity : entities) {
           
            if (entity instanceof Player) {
        
                Player player = (Player) entity;
                double distance = getPosition().subtract(player.getPosition()).magnitude();
                if (distance <= 1.0) { // Same detection range as pickup range
                    int playerId = player.getId();
                    nearbyPlayerIds.add(playerId);
                    
                    // Start timer if not already started
                    if (!playerPickupTimers.containsKey(playerId)) {
                        playerPickupTimers.put(playerId, currentTime);
                    }
                }
            }
        }
        
        // Remove timers for players who are no longer nearby
        Iterator<Integer> iterator = playerPickupTimers.keySet().iterator();
        while (iterator.hasNext()) {
            Integer playerId = iterator.next();
            if (!nearbyPlayerIds.contains(playerId)) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Checks if this dropped item can be picked up by a player at the given position.
     * Uses per-player proximity-based pickup delay.
     * 
     * @param playerPosition The player's position
     * @param playerId The player's ID
     * @return true if the player is close enough and pickup delay has passed
     */
    public boolean canBePickedUpBy(Vector4D playerPosition, int playerId) {
        // Check distance first
        double distance = getPosition().subtract(playerPosition).magnitude();
        if (distance > 1) {
            return false; // Too far away
        }
        
        // Check if player has been nearby long enough
        Long timerStart = playerPickupTimers.get(playerId);
        if (timerStart == null) {
            return false; // Player hasn't been nearby
        }
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - timerStart) >= PICKUP_DELAY_MS;
    }
    
    /**
     * Legacy method for backward compatibility.
     * 
     * @param playerPosition The player's position
     * @return false (requires playerId for new system)
     */
    @Deprecated
    public boolean canBePickedUpBy(Vector4D playerPosition) {
        return false; // Legacy method - requires playerId now
    }
    
    /**
     * Gets the item this entity represents.
     * 
     * @return The BaseItem
     */
    public BaseItem getItem() {
        return item;
    }
    
    /**
     * Sets the item this entity represents.
     * 
     * @param item The new item
     */
    public void setItem(BaseItem item) {
        this.item = item;
    }
    
    /**
     * Gets the time when this item was spawned.
     * 
     * @return The spawn time in milliseconds
     */
    public long getSpawnTime() {
        return spawnTime;
    }
    
    /**
     * Checks if this item should despawn.
     * 
     * @return true if the item should be removed from the world
     */
    public boolean shouldDespawn() {
        return shouldDespawn;
    }
    
    /**
     * Marks this item for despawn.
     */
    public void markForDespawn() {
        shouldDespawn = true;
    }
    
    /**
     * Gets the remaining time before this item despawns.
     * 
     * @return The remaining time in milliseconds, or 0 if already expired
     */
    public long getRemainingDespawnTime() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        long remaining = DESPAWN_TIME_MS - elapsed;
        return Math.max(0, remaining);
    }
    
    /**
     * Gets the number of items in this stack.
     * 
     * @return The item count
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Gets the despawn timer value.
     * 
     * @return The despawn timer in seconds
     */
    public double getDespawnTimer() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        return elapsed / 1000.0; // Convert to seconds
    }
    
    /**
     * Sets the despawn timer value.
     * 
     * @param timerSeconds The despawn timer in seconds
     */
    public void setDespawnTimer(double timerSeconds) {
        long currentTime = System.currentTimeMillis();
        this.spawnTime = currentTime - (long)(timerSeconds * 1000.0);
    }
}