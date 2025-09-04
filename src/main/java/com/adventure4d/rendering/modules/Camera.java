package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Vector4D;

/**
 * Represents a camera in the 4D world that handles view positioning.
 * The camera maintains a world offset that determines what part of the world is visible.
 * The player always appears centered, and movement affects the camera's world offset.
 */
public class Camera {
    // The camera's position in the world (what the camera is looking at)
    private Vector4D worldOffset;
    
    // The fixed player position (always at the center of the view)
    private static final Vector4D PLAYER_CENTER_POSITION = new Vector4D(0, 0, 0, 0);
    
    /**
     * Creates a new camera with the specified initial world offset.
     * 
     * @param initialWorldOffset The initial world offset
     */
    public Camera(Vector4D initialWorldOffset) {
        this.worldOffset = initialWorldOffset;
    }
    
    /**
     * Creates a new camera at the origin.
     */
    public Camera() {
        this(new Vector4D(0, 0, 0, 0));
    }
    
    /**
     * Gets the current world offset.
     * 
     * @return The world offset
     */
    public Vector4D getWorldOffset() {
        return worldOffset;
    }
    
    /**
     * Sets the world offset.
     * 
     * @param worldOffset The new world offset
     */
    public void setWorldOffset(Vector4D worldOffset) {
        this.worldOffset = worldOffset;
    }
    
    /**
     * Moves the camera by the specified offset.
     * 
     * @param offset The offset to move by
     */
    public void move(Vector4D offset) {
        this.worldOffset = this.worldOffset.add(offset);
    }
    
    /**
     * Gets the player's fixed center position (always the same).
     * 
     * @return The player's center position
     */
    public Vector4D getPlayerCenterPosition() {
        return PLAYER_CENTER_POSITION;
    }
    
    /**
     * Converts a world coordinate to a view coordinate relative to the camera.
     * 
     * @param worldCoord The world coordinate
     * @return The view coordinate
     */
    public Vector4D worldToView(Vector4D worldCoord) {
        return worldCoord.subtract(worldOffset);
    }
    
    /**
     * Converts a view coordinate to a world coordinate.
     * 
     * @param viewCoord The view coordinate
     * @return The world coordinate
     */
    public Vector4D viewToWorld(Vector4D viewCoord) {
        return viewCoord.add(worldOffset);
    }
    
    /**
     * Gets the world coordinate that should be rendered at the center of a slice.
     * 
     * @param sliceX The x-coordinate of the slice in the grid (0-6)
     * @param sliceY The y-coordinate of the slice in the grid (0-6)
     * @return The world coordinate for the center of that slice
     */
    public Vector4D getSliceCenterWorldCoord(int sliceX, int sliceY) {
        // Calculate the offset from the center slice (3,3)
        double wOffset = sliceX - SliceRenderer.getSliceCenter();
        double zOffset = sliceY - SliceRenderer.getSliceCenter();
        
        // Return the world coordinate for this slice center
        return new Vector4D(
            worldOffset.getX(),
            worldOffset.getY(),
            worldOffset.getZ() + zOffset,
            worldOffset.getW() + wOffset
        );
    }
}