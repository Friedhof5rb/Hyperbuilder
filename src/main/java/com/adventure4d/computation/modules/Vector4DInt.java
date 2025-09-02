package com.adventure4d.computation.modules;

/**
 * Represents a vector in 4D space with integer x, y, z, and w components.
 * Used for block positions and chunk coordinates in the 4D voxel world.
 */
public class Vector4DInt {
    private final int x;
    private final int y;
    private final int z;
    private final int w;

    /**
     * Creates a new 4D integer vector with the specified components.
     */
    public Vector4DInt(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Creates a zero vector (0,0,0,0).
     */
    public Vector4DInt() {
        this(0, 0, 0, 0);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getW() { return w; }

    /**
     * Returns a new vector that is the sum of this vector and the other vector.
     */
    public Vector4DInt add(Vector4DInt other) {
        return new Vector4DInt(
            this.x + other.x,
            this.y + other.y,
            this.z + other.z,
            this.w + other.w
        );
    }

    /**
     * Returns a new vector that is the difference of this vector and the other vector.
     */
    public Vector4DInt subtract(Vector4DInt other) {
        return new Vector4DInt(
            this.x - other.x,
            this.y - other.y,
            this.z - other.z,
            this.w - other.w
        );
    }

    /**
     * Returns a new vector that is this vector scaled by the given factor.
     */
    public Vector4DInt scale(int factor) {
        return new Vector4DInt(
            this.x * factor,
            this.y * factor,
            this.z * factor,
            this.w * factor
        );
    }

    /**
     * Calculates the Manhattan distance between this vector and another vector.
     * This is the sum of the absolute differences of their coordinates.
     */
    public int manhattanDistance(Vector4DInt other) {
        return Math.abs(this.x - other.x) + 
               Math.abs(this.y - other.y) + 
               Math.abs(this.z - other.z) + 
               Math.abs(this.w - other.w);
    }

    /**
     * Calculates the squared Euclidean distance between this vector and another vector.
     */
    public int distanceSquared(Vector4DInt other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int dz = this.z - other.z;
        int dw = this.w - other.w;
        return dx*dx + dy*dy + dz*dz + dw*dw;
    }

    /**
     * Converts this integer vector to a double-precision vector.
     */
    public Vector4D toVector4D() {
        return new Vector4D(x, y, z, w);
    }

    /**
     * Returns a new vector with each component modulo the given value.
     * Useful for wrapping coordinates within chunk boundaries.
     */
    public Vector4DInt mod(int modValue) {
        return new Vector4DInt(
            ((x % modValue) + modValue) % modValue,
            ((y % modValue) + modValue) % modValue,
            ((z % modValue) + modValue) % modValue,
            ((w % modValue) + modValue) % modValue
        );
    }

    /**
     * Returns a new vector with each component divided by the given value.
     * Useful for converting block coordinates to chunk coordinates.
     */
    public Vector4DInt divide(int divisor) {
        return new Vector4DInt(
            Math.floorDiv(x, divisor),
            Math.floorDiv(y, divisor),
            Math.floorDiv(z, divisor),
            Math.floorDiv(w, divisor)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Vector4DInt other = (Vector4DInt) obj;
        return x == other.x && 
               y == other.y && 
               z == other.z && 
               w == other.w;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + w;
        return result;
    }

    @Override
    public String toString() {
        return String.format("Vector4DInt(%d, %d, %d, %d)", x, y, z, w);
    }
}