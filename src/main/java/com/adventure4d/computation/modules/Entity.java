package com.adventure4d.computation.modules;

/**
 * Base class for all entities in the 4D world.
 * Entities are objects that can move and interact with the world, such as players, items, etc.
 */
public abstract class Entity {
    // Unique identifier for this entity
    private final int id;
    
    // Position and velocity in the 4D world
    private Vector4D position;
    private Vector4D velocity;
    
    // Entity size (collision box dimensions)
    private final double sizeX;
    private final double sizeY;
    private final double sizeZ;
    private final double sizeW;
    
    // Whether this entity is affected by gravity
    private boolean gravity;
    
    /**
     * Creates a new entity with the specified ID and position.
     * 
     * @param id The unique identifier for this entity
     * @param position The initial position of this entity
     * @param sizeX The X dimension of this entity's collision box
     * @param sizeY The Y dimension of this entity's collision box
     * @param sizeZ The Z dimension of this entity's collision box
     * @param sizeW The W dimension of this entity's collision box
     */
    public Entity(int id, Vector4D position, double sizeX, double sizeY, double sizeZ, double sizeW) {
        this.id = id;
        this.position = position;
        this.velocity = new Vector4D();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeW = sizeW;
        this.gravity = true;
    }
    
    /**
     * Gets the unique identifier for this entity.
     * 
     * @return The entity ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the current position of this entity.
     * 
     * @return The entity position
     */
    public Vector4D getPosition() {
        return position;
    }
    
    /**
     * Sets the position of this entity.
     * 
     * @param position The new position
     */
    public void setPosition(Vector4D position) {
        this.position = position;
    }
    
    /**
     * Gets the current velocity of this entity.
     * 
     * @return The entity velocity
     */
    public Vector4D getVelocity() {
        return velocity;
    }
    
    /**
     * Sets the velocity of this entity.
     * 
     * @param velocity The new velocity
     */
    public void setVelocity(Vector4D velocity) {
        this.velocity = velocity;
    }
    
    /**
     * Gets the X dimension of this entity's collision box.
     * 
     * @return The X size
     */
    public double getSizeX() {
        return sizeX;
    }
    
    /**
     * Gets the Y dimension of this entity's collision box.
     * 
     * @return The Y size
     */
    public double getSizeY() {
        return sizeY;
    }
    
    /**
     * Gets the Z dimension of this entity's collision box.
     * 
     * @return The Z size
     */
    public double getSizeZ() {
        return sizeZ;
    }
    
    /**
     * Gets the W dimension of this entity's collision box.
     * 
     * @return The W size
     */
    public double getSizeW() {
        return sizeW;
    }
    
    /**
     * Checks if this entity is affected by gravity.
     * 
     * @return true if gravity affects this entity, false otherwise
     */
    public boolean hasGravity() {
        return gravity;
    }
    
    /**
     * Sets whether this entity is affected by gravity.
     * 
     * @param gravity true to enable gravity, false to disable it
     */
    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }
    
    /**
     * Updates this entity's state for the current tick.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     * @param world The world to interact with during the update
     */
    public abstract void update(double deltaTime, World world);
    
    /**
     * Gets the chunk position of this entity.
     * 
     * @return The chunk coordinates containing this entity
     */
    public Vector4DInt getChunkPosition() {
        return new Vector4DInt(
            (int) Math.floor(position.getX() / Chunk4D.CHUNK_SIZE),
            (int) Math.floor(position.getY() / Chunk4D.CHUNK_SIZE),
            (int) Math.floor(position.getZ() / Chunk4D.CHUNK_SIZE),
            (int) Math.floor(position.getW() / Chunk4D.CHUNK_SIZE)
        );
    }
    
    /**
     * Gets the block position of this entity.
     * 
     * @return The block coordinates containing this entity
     */
    public Vector4DInt getBlockPosition() {
        return position.toVector4DInt();
    }
}