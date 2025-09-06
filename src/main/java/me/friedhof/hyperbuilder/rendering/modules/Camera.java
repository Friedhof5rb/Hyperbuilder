package me.friedhof.hyperbuilder.rendering.modules;

import me.friedhof.hyperbuilder.computation.modules.Vector4D;

/**
 * Represents a camera in the 4D world that handles view positioning.
 * The camera maintains a world offset that determines what part of the world is visible.
 * The player always appears centered, and movement affects the camera's world offset.
 */
public class Camera {
    /**
     * Enum representing which dimension is mapped to the horizontal (left-right) axis within slices.
     */
    public enum HorizontalDimension {
        X("X"), Z("Z"), W("W");
        
        private final String displayName;
        
        HorizontalDimension(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Gets the next dimension in the cycle.
         */
        public HorizontalDimension getNext() {
            switch (this) {
                case X: return Z;
                case Z: return W;
                case W: return X;
                default: return X;
            }
        }
        
        /**
         * Gets the previous dimension in the cycle.
         */
        public HorizontalDimension getPrevious() {
            switch (this) {
                case X: return W;
                case Z: return X;
                case W: return Z;
                default: return X;
            }
        }
    }
    
    // The camera's position in the world (what the camera is looking at)
    private Vector4D worldOffset;
    
    // The current horizontal dimension mapping (which dimension is left-right within slices)
    private HorizontalDimension horizontalDimension;
    
    // The fixed player position (always at the center of the view)
    private static final Vector4D PLAYER_CENTER_POSITION = new Vector4D(0, 0, 0, 0);
    
    /**
     * Creates a new camera with the specified initial world offset.
     * 
     * @param initialWorldOffset The initial world offset
     */
    public Camera(Vector4D initialWorldOffset) {
        this.worldOffset = initialWorldOffset;
        this.horizontalDimension = HorizontalDimension.X; // Default to X dimension as horizontal
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
     * Gets the current horizontal dimension.
     * 
     * @return The current horizontal dimension
     */
    public HorizontalDimension getHorizontalDimension() {
        return horizontalDimension;
    }
    
    /**
     * Cycles to the next horizontal dimension (X -> Z -> W -> X).
     */
    public void cycleHorizontalDimension() {
        this.horizontalDimension = this.horizontalDimension.getNext();
    }
    
    /**
     * Sets the horizontal dimension.
     * 
     * @param dimension The dimension to set as horizontal
     */
    public void setHorizontalDimension(HorizontalDimension dimension) {
        this.horizontalDimension = dimension;
    }
    
    /**
     * Get the world coordinates for the center of a specific slice in the grid.
     * 
     * The 7x7 slice grid represents the two dimensions orthogonal to the current viewing plane.
     * Each viewing mode shows a 2D slice (horizontal + Y), and the grid shows different positions
     * in the two remaining dimensions.
     * 
     * @param sliceHorizontal The horizontal coordinate of the slice in the grid (0-6)
     * @param sliceVertical The vertical coordinate of the slice in the grid (0-6)
     * @return The world coordinates of the slice center
     */
    public Vector4D getSliceCenterWorldCoord(int sliceHorizontal, int sliceVertical) {
        // Calculate the offset from the center slice (3,3)
        double horizontalOffset = sliceHorizontal - SliceRenderer.getSliceCenter();
        double verticalOffset = sliceVertical - SliceRenderer.getSliceCenter();
    
        // Map slice grid coordinates to the two dimensions orthogonal to the viewing plane
        switch (horizontalDimension) {
            case X:
                // X mode: viewing X-Y plane, grid represents Z (horizontal) and W (vertical) dimensions
                return new Vector4D(
                    worldOffset.getX(),
                    worldOffset.getY(),
                    worldOffset.getZ() + horizontalOffset,
                    worldOffset.getW() + verticalOffset
                );
            case Z:
                // Z mode: viewing Z-Y plane, grid represents X (horizontal) and W (vertical) dimensions
                return new Vector4D(
                    worldOffset.getX() + horizontalOffset,
                    worldOffset.getY(),
                    worldOffset.getZ(),
                    worldOffset.getW() + verticalOffset
                );
            case W:
                // W mode: viewing W-Y plane, grid represents X (horizontal) and Z (vertical) dimensions
                return new Vector4D(
                    worldOffset.getX() + horizontalOffset,
                    worldOffset.getY(),
                    worldOffset.getZ() + verticalOffset,
                    worldOffset.getW()
                );
            default:
                // Default to X mode: viewing X-Y plane, grid represents Z and W dimensions
                return new Vector4D(
                    worldOffset.getX(),
                    worldOffset.getY(),
                    worldOffset.getZ() + horizontalOffset,
                    worldOffset.getW() + verticalOffset
                );
        }
    }
}