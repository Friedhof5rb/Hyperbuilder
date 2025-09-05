package com.adventure4d.rendering.modules;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Manages loading and caching of 4D textures.
 * Handles splitting 2D images into 4D texture grids.
 */
public class TextureManager {
    // Cache for loaded textures
    private static final Map<String, Texture4D> textureCache = new HashMap<>();
    
    // Base path for texture files
    private static final String TEXTURE_BASE_PATH = "src/main/java/com/adventure4d/rendering/data/";
    
    /**
     * Loads a 4D texture from a 2D image file by splitting it into a grid.
     * The image is split into an 8x8x8x8 grid where each 8x8 pixel section
     * represents one element in the 4D texture space.
     * 
     * @param filename The filename of the texture (without path)
     * @return The loaded 4D texture
     * @throws IOException If the texture file cannot be loaded
     */
    public static Texture4D loadTexture4D(String filename) throws IOException {
        // Check cache first
        if (textureCache.containsKey(filename)) {
            return textureCache.get(filename);
        }
        
        // Load the image file
        String fullPath = TEXTURE_BASE_PATH + filename;
        BufferedImage image = ImageIO.read(new File(fullPath));
        
        // Verify the image is 64x64 pixels
        if (image.getWidth() != 64 || image.getHeight() != 64) {
            throw new IllegalArgumentException("Texture must be 64x64 pixels, got " + 
                image.getWidth() + "x" + image.getHeight());
        }
        
        // Create the 4D texture (8x8x8x8 = 8 pixels per dimension)
        Texture4D texture4D = new Texture4D(8, 8, 8, 8);
        
        // Split the 64x64 image into 8x8 sections
        // The image is organized as an 8x8 grid of 8x8 pixel sections
        // Each section corresponds to one (z,w) coordinate pair
        for (int w = 0; w < 8; w++) {
            for (int z = 0; z < 8; z++) {
                // Calculate the top-left corner of this section in the source image
                int sectionStartX = z * 8;
                int sectionStartY = w * 8;
                
                // Copy the 8x8 section to the 4D texture
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        int sourceX = sectionStartX + x;
                        int sourceY = sectionStartY + y;
                        int color = image.getRGB(sourceX, sourceY);
                        texture4D.setColor(x, y, z, w, color);
                    }
                }
            }
        }
        
        // Cache the texture
        textureCache.put(filename, texture4D);
        
        System.out.println("Loaded 4D texture: " + filename + " (8x8x8x8 from 64x64 image)");
        return texture4D;
    }
    
    /**
     * Gets a specific texture by filename.
     * 
     * @param filename The filename of the texture
     * @return The texture, or null if not found
     */
    public static Texture4D getTexture(String filename) {
        return textureCache.get(filename);
    }
    
    /**
     * Preloads all standard block textures.
     */
    public static void preloadTextures() {
        try {
            // Load the grass texture
            loadTexture4D("Grass.png");
            loadTexture4D("Dirt.png");
            loadTexture4D("stone.png");
            System.out.println("All textures preloaded successfully");
        } catch (IOException e) {
            System.err.println("Failed to preload textures: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Clears the texture cache.
     */
    public static void clearCache() {
        textureCache.clear();
        System.out.println("Texture cache cleared");
    }
    
    /**
     * Gets the number of cached textures.
     * 
     * @return The number of cached textures
     */
    public static int getCacheSize() {
        return textureCache.size();
    }
}