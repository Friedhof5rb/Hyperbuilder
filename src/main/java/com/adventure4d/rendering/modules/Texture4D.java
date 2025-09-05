package com.adventure4d.rendering.modules;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Represents a 4D texture.
 * A 4D texture is essentially a 3D array of 2D textures.
 */
public class Texture4D {
    // The texture data (w, z, y, x)
    private int[][][][] data;
    
    // Dimensions of the texture
    private final int width;
    private final int height;
    private final int depth;
    private final int wSize;
    
    /**
     * Creates a new 4D texture with the specified dimensions.
     * 
     * @param width The width of the texture (x)
     * @param height The height of the texture (y)
     * @param depth The depth of the texture (z)
     * @param wSize The size in the fourth dimension (w)
     */
    public Texture4D(int width, int height, int depth, int wSize) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.wSize = wSize;
        
        // Initialize the texture data
        data = new int[wSize][depth][height][width];
    }
    
    
    
    /**
     * Gets the color at the specified coordinates.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param w The w coordinate
     * @return The color as an ARGB integer
     */
    public int getColor(int x, int y, int z, int w) {
        // Wrap coordinates to handle out-of-bounds access
        x = Math.floorMod(x, width);
        y = Math.floorMod(y, height);
        z = Math.floorMod(z, depth);
        w = Math.floorMod(w, wSize);
        
        return data[w][z][y][x];
    }
    
    /**
     * Sets the color at the specified coordinates.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param w The w coordinate
     * @param color The color as an ARGB integer
     */
    public void setColor(int x, int y, int z, int w, int color) {
        // Wrap coordinates to handle out-of-bounds access
        x = Math.floorMod(x, width);
        y = Math.floorMod(y, height);
        z = Math.floorMod(z, depth);
        w = Math.floorMod(w, wSize);
        
        data[w][z][y][x] = color;
    }
    
    /**
     * Gets the width of the texture.
     * 
     * @return The width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the texture.
     * 
     * @return The height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gets the depth of the texture.
     * 
     * @return The depth
     */
    public int getDepth() {
        return depth;
    }
    
    /**
     * Gets the size in the fourth dimension.
     * 
     * @return The w-size
     */
    public int getWSize() {
        return wSize;
    }
    
    /**
     * Extracts a 2D texture slice from the 4D texture at the specified z and w coordinates.
     * This is used for rendering blocks in the game.
     * 
     * @param z The z coordinate to extract
     * @param w The w coordinate to extract
     * @return A BufferedImage containing the 2D slice
     */
    public BufferedImage getSlice2D(int z, int w) {
        // Wrap coordinates to handle out-of-bounds access
        z = Math.floorMod(z, depth);
        w = Math.floorMod(w, wSize);
        
        // Create a new BufferedImage for the slice
        BufferedImage slice = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Copy the data from the 4D texture to the 2D slice
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                slice.setRGB(x, y, data[w][z][y][x]);
            }
        }
        
        return slice;
    }
    
    /**
     * Gets a pixel color from a specific 2D slice at the given coordinates.
     * This is optimized for rendering and avoids creating BufferedImage objects.
     * 
     * @param x The x coordinate within the slice
     * @param y The y coordinate within the slice
     * @param z The z coordinate of the slice
     * @param w The w coordinate of the slice
     * @return The color as an ARGB integer
     */
    public int getSlicePixel(int x, int y, int z, int w) {
        return getColor(x, y, z, w);
    }
    
    /**
     * Gets a 2D texture slice using fractional coordinates for smooth transitions.
     * Interpolates between adjacent slices based on fractional parts.
     * 
     * @param fracZ The fractional z coordinate (e.g., 2.3 means 30% between slice 2 and 3)
     * @param fracW The fractional w coordinate (e.g., 1.7 means 70% between slice 1 and 2)
     * @return A BufferedImage representing the interpolated texture slice
     */
    public BufferedImage getSlice2DFractional(double fracZ, double fracW) {
        // Extract integer and fractional parts
        int z1 = (int) Math.floor(fracZ);
        int w1 = (int) Math.floor(fracW);
        double fracZPart = fracZ - z1;
        double fracWPart = fracW - w1;
        
        // Calculate adjacent slice coordinates
        int z2 = z1 + 1;
        int w2 = w1 + 1;
        
        // Wrap coordinates
        z1 = Math.floorMod(z1, depth);
        z2 = Math.floorMod(z2, depth);
        w1 = Math.floorMod(w1, wSize);
        w2 = Math.floorMod(w2, wSize);
        
        // Create the interpolated image
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Interpolate between the four corner slices
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get colors from four corner slices
                int c00 = data[w1][z1][y][x]; // (z1, w1)
                int c01 = data[w2][z1][y][x]; // (z1, w2)
                int c10 = data[w1][z2][y][x]; // (z2, w1)
                int c11 = data[w2][z2][y][x]; // (z2, w2)
                
                // Bilinear interpolation
                int interpolated = interpolateColors(c00, c01, c10, c11, fracZPart, fracWPart);
                result.setRGB(x, y, interpolated);
            }
        }
        
        return result;
    }
    
    /**
     * Performs bilinear interpolation between four colors.
     * 
     * @param c00 Color at (0,0)
     * @param c01 Color at (0,1)
     * @param c10 Color at (1,0)
     * @param c11 Color at (1,1)
     * @param fracX Fractional X coordinate (0.0 to 1.0)
     * @param fracY Fractional Y coordinate (0.0 to 1.0)
     * @return The interpolated color
     */
    private int interpolateColors(int c00, int c01, int c10, int c11, double fracX, double fracY) {
        // Interpolate along X axis
        int c0 = interpolateColor(c00, c10, fracX);
        int c1 = interpolateColor(c01, c11, fracX);
        
        // Interpolate along Y axis
        return interpolateColor(c0, c1, fracY);
    }
    
    /**
     * Linearly interpolates between two colors.
     * 
     * @param c1 First color
     * @param c2 Second color
     * @param t Interpolation factor (0.0 = c1, 1.0 = c2)
     * @return The interpolated color
     */
    private int interpolateColor(int c1, int c2, double t) {
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        
        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        
        int a = (int) (a1 * (1 - t) + a2 * t);
        int r = (int) (r1 * (1 - t) + r2 * t);
        int g = (int) (g1 * (1 - t) + g2 * t);
        int b = (int) (b1 * (1 - t) + b2 * t);
        
        // Clamp values to valid range
        a = Math.max(0, Math.min(255, a));
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}