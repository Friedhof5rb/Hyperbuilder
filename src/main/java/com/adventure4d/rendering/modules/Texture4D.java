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
     * Creates a 4D texture from a 2D image file, repeating it in the z and w dimensions.
     * 
     * @param filePath The path to the image file
     * @param depth The depth of the texture (z)
     * @param wSize The size in the fourth dimension (w)
     * @return The created texture
     * @throws IOException If the image file cannot be read
     */
    public static Texture4D fromFile(String filePath, int depth, int wSize) throws IOException {
        // Load the image
        BufferedImage image = ImageIO.read(new File(filePath));
        
        // Get the dimensions
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Create a new texture
        Texture4D texture = new Texture4D(width, height, depth, wSize);
        
        // Copy the image data to the texture
        for (int w = 0; w < wSize; w++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        texture.data[w][z][y][x] = image.getRGB(x, y);
                    }
                }
            }
        }
        
        return texture;
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
}