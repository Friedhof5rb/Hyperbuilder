package com.adventure4d.rendering.modules;

import com.adventure4d.computation.modules.Vector4D;

/**
 * Represents a camera in 4D space.
 * Handles perspective projection and view transformations.
 */
public class Camera4D {
    // Camera position in 4D space
    private Vector4D position;
    
    // Camera orientation (forward, up, right, ana vectors)
    private Vector4D forward;
    private Vector4D up;
    private Vector4D right;
    private Vector4D ana; // Fourth dimension direction
    
    // Camera parameters
    private double fov; // Field of view in degrees
    private double aspectRatio;
    private double nearClip;
    private double farClip;
    
    // The w-coordinate for the current 3D slice
    private double wSlice;
    
    /**
     * Creates a new camera with the specified parameters.
     * 
     * @param position The camera position
     * @param forward The forward direction
     * @param up The up direction
     * @param fov The field of view in degrees
     * @param aspectRatio The aspect ratio (width / height)
     * @param nearClip The near clipping plane distance
     * @param farClip The far clipping plane distance
     */
    public Camera4D(Vector4D position, Vector4D forward, Vector4D up, 
                   double fov, double aspectRatio, double nearClip, double farClip) {
        this.position = position;
        this.forward = forward.normalize();
        this.up = up.normalize();
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearClip = nearClip;
        this.farClip = farClip;
        
        // Calculate right vector using cross product (assuming 3D for now)
        this.right = new Vector4D(
            this.forward.getY() * this.up.getZ() - this.forward.getZ() * this.up.getY(),
            this.forward.getZ() * this.up.getX() - this.forward.getX() * this.up.getZ(),
            this.forward.getX() * this.up.getY() - this.forward.getY() * this.up.getX(),
            0
        ).normalize();
        
        // Initialize ana vector (fourth dimension)
        this.ana = new Vector4D(0, 0, 0, 1);
        
        // Initialize w-slice to camera position's w coordinate
        this.wSlice = position.getW();
    }
    
    /**
     * Gets the camera position.
     * 
     * @return The camera position
     */
    public Vector4D getPosition() {
        return position;
    }
    
    /**
     * Sets the camera position.
     * 
     * @param position The new camera position
     */
    public void setPosition(Vector4D position) {
        this.position = position;
    }
    
    /**
     * Gets the forward direction.
     * 
     * @return The forward direction
     */
    public Vector4D getForward() {
        return forward;
    }
    
    /**
     * Gets the up direction.
     * 
     * @return The up direction
     */
    public Vector4D getUp() {
        return up;
    }
    
    /**
     * Gets the right direction.
     * 
     * @return The right direction
     */
    public Vector4D getRight() {
        return right;
    }
    
    /**
     * Gets the ana direction (fourth dimension).
     * 
     * @return The ana direction
     */
    public Vector4D getAna() {
        return ana;
    }
    
    /**
     * Gets the current w-slice.
     * 
     * @return The w-slice
     */
    public double getWSlice() {
        return wSlice;
    }
    
    /**
     * Sets the current w-slice.
     * 
     * @param wSlice The new w-slice
     */
    public void setWSlice(double wSlice) {
        this.wSlice = wSlice;
    }
    
    /**
     * Gets the field of view.
     * 
     * @return The field of view in degrees
     */
    public double getFov() {
        return fov;
    }
    
    /**
     * Gets the aspect ratio.
     * 
     * @return The aspect ratio
     */
    public double getAspectRatio() {
        return aspectRatio;
    }
    
    /**
     * Sets the aspect ratio.
     * 
     * @param aspectRatio The new aspect ratio
     */
    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    
    /**
     * Gets the near clipping plane distance.
     * 
     * @return The near clipping plane distance
     */
    public double getNearClip() {
        return nearClip;
    }
    
    /**
     * Gets the far clipping plane distance.
     * 
     * @return The far clipping plane distance
     */
    public double getFarClip() {
        return farClip;
    }
    
    /**
     * Moves the camera in the specified direction.
     * 
     * @param direction The direction to move in
     * @param distance The distance to move
     */
    public void move(Vector4D direction, double distance) {
        position = position.add(direction.scale(distance));
    }
    
    /**
     * Moves the camera forward.
     * 
     * @param distance The distance to move
     */
    public void moveForward(double distance) {
        move(forward, distance);
    }
    
    /**
     * Moves the camera backward.
     * 
     * @param distance The distance to move
     */
    public void moveBackward(double distance) {
        move(forward, -distance);
    }
    
    /**
     * Moves the camera right.
     * 
     * @param distance The distance to move
     */
    public void moveRight(double distance) {
        move(right, distance);
    }
    
    /**
     * Moves the camera left.
     * 
     * @param distance The distance to move
     */
    public void moveLeft(double distance) {
        move(right, -distance);
    }
    
    /**
     * Moves the camera up.
     * 
     * @param distance The distance to move
     */
    public void moveUp(double distance) {
        move(up, distance);
    }
    
    /**
     * Moves the camera down.
     * 
     * @param distance The distance to move
     */
    public void moveDown(double distance) {
        move(up, -distance);
    }
    
    /**
     * Moves the camera in the ana direction (fourth dimension).
     * 
     * @param distance The distance to move
     */
    public void moveAna(double distance) {
        move(ana, distance);
        wSlice = position.getW();
    }
    
    /**
     * Moves the camera in the kata direction (negative fourth dimension).
     * 
     * @param distance The distance to move
     */
    public void moveKata(double distance) {
        move(ana, -distance);
        wSlice = position.getW();
    }
    
    /**
     * Rotates the camera around the specified axis.
     * 
     * @param axis The axis to rotate around
     * @param angle The angle to rotate in degrees
     */
    public void rotate(Vector4D axis, double angle) {
        // TODO: Implement 4D rotation
        // This is a placeholder for now
        // In 4D, we need to handle rotations in 6 planes (xy, xz, xw, yz, yw, zw)
    }
}