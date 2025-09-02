package com.adventure4d.computation.modules;

/**
 * Represents a vector in 4D space with x, y, z, and w components.
 * Used for positions, directions, and calculations in the 4D world.
 */
public class Vector4D {
    private final double x;
    private final double y;
    private final double z;
    private final double w;

    /**
     * Creates a new 4D vector with the specified components.
     */
    public Vector4D(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Creates a zero vector (0,0,0,0).
     */
    public Vector4D() {
        this(0, 0, 0, 0);
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getW() { return w; }

    /**
     * Returns a new vector that is the sum of this vector and the other vector.
     */
    public Vector4D add(Vector4D other) {
        return new Vector4D(
            this.x + other.x,
            this.y + other.y,
            this.z + other.z,
            this.w + other.w
        );
    }

    /**
     * Returns a new vector that is the difference of this vector and the other vector.
     */
    public Vector4D subtract(Vector4D other) {
        return new Vector4D(
            this.x - other.x,
            this.y - other.y,
            this.z - other.z,
            this.w - other.w
        );
    }

    /**
     * Returns a new vector that is this vector scaled by the given factor.
     */
    public Vector4D scale(double factor) {
        return new Vector4D(
            this.x * factor,
            this.y * factor,
            this.z * factor,
            this.w * factor
        );
    }

    /**
     * Calculates the dot product of this vector and another vector.
     */
    public double dot(Vector4D other) {
        return this.x * other.x + 
               this.y * other.y + 
               this.z * other.z + 
               this.w * other.w;
    }

    /**
     * Calculates the squared magnitude (length) of this vector.
     * This is more efficient than magnitude() when only comparing distances.
     */
    public double magnitudeSquared() {
        return x*x + y*y + z*z + w*w;
    }

    /**
     * Calculates the magnitude (length) of this vector.
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    /**
     * Returns a normalized version of this vector (same direction, but magnitude of 1).
     */
    public Vector4D normalize() {
        double mag = magnitude();
        if (mag < 1e-10) {
            return new Vector4D(0, 0, 0, 0); // Avoid division by zero
        }
        return scale(1.0 / mag);
    }

    /**
     * Calculates the Euclidean distance between this vector and another vector.
     */
    public double distance(Vector4D other) {
        return subtract(other).magnitude();
    }

    /**
     * Calculates the squared Euclidean distance between this vector and another vector.
     * This is more efficient than distance() when only comparing distances.
     */
    public double distanceSquared(Vector4D other) {
        return subtract(other).magnitudeSquared();
    }

    /**
     * Returns a vector with the floor of each component.
     * Useful for converting from continuous coordinates to discrete block coordinates.
     */
    public Vector4D floor() {
        return new Vector4D(
            Math.floor(x),
            Math.floor(y),
            Math.floor(z),
            Math.floor(w)
        );
    }

    /**
     * Creates an integer-based version of this vector.
     * Useful for block positions in the voxel world.
     */
    public Vector4DInt toVector4DInt() {
        return new Vector4DInt(
            (int) Math.floor(x),
            (int) Math.floor(y),
            (int) Math.floor(z),
            (int) Math.floor(w)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Vector4D other = (Vector4D) obj;
        double epsilon = 1e-10; // Small value for floating-point comparison
        
        return Math.abs(x - other.x) < epsilon &&
               Math.abs(y - other.y) < epsilon &&
               Math.abs(z - other.z) < epsilon &&
               Math.abs(w - other.w) < epsilon;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        result = 31 * result + Double.hashCode(w);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Vector4D(%.2f, %.2f, %.2f, %.2f)", x, y, z, w);
    }
}