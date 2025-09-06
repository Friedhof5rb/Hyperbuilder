package com.adventure4d.rendering.modules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages 2D textures for item rendering.
 * Provides caching and loading functionality for PNG textures.
 */
public class TextureManager2D {
    // Base path for texture files
    private static final String TEXTURE_BASE_PATH = "src/main/java/com/adventure4d/rendering/data/";
    
    // Cache for loaded textures
    private static final Map<String, Texture2D> textureCache = new HashMap<>();
    
    /**
     * Loads a 2D texture from a file and caches it.
     * 
     * @param filename The filename of the texture to load
     * @return The loaded Texture2D
     * @throws IOException If the texture cannot be loaded
     */
    public static Texture2D loadTexture2D(String filename) throws IOException {
        // Check if already cached
        if (textureCache.containsKey(filename)) {
            return textureCache.get(filename);
        }
        
        // Load the texture
        Texture2D texture = Texture2D.loadFromFile(TEXTURE_BASE_PATH + filename);
        
        // Cache it
        textureCache.put(filename, texture);
        
        System.out.println("Loaded 2D texture: " + filename + " (" + texture.getWidth() + "x" + texture.getHeight() + ")");
        return texture;
    }
    
    /**
     * Gets a cached 2D texture by filename.
     * 
     * @param filename The filename of the texture
     * @return The texture, or null if not found
     */
    public static Texture2D getTexture2D(String filename) {
        return textureCache.get(filename);
    }
    
    /**
     * Preloads all standard item textures.
     */
    public static void preloadItemTextures() {
        try {
            // Load the same textures that are used for blocks
            loadTexture2D("Grass.png");
            loadTexture2D("Dirt.png");
            loadTexture2D("stone.png");
            // Add more textures as they become available
            // loadTexture2D("Wood.png");
            // loadTexture2D("Leaves.png");
            
            System.out.println("All 2D item textures preloaded successfully");
        } catch (IOException e) {
            System.err.println("Failed to preload 2D item textures: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Clears the 2D texture cache.
     */
    public static void clearCache() {
        textureCache.clear();
        System.out.println("2D texture cache cleared");
    }
    
    /**
     * Gets the number of cached 2D textures.
     * 
     * @return The number of cached textures
     */
    public static int getCacheSize() {
        return textureCache.size();
    }
    
    /**
     * Checks if a texture is available in the cache.
     * 
     * @param filename The filename to check
     * @return true if the texture is cached, false otherwise
     */
    public static boolean isTextureCached(String filename) {
        return textureCache.containsKey(filename);
    }
}