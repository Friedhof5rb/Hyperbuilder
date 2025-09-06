package com.adventure4d.rendering.modules;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Represents a simple 2D texture loaded from a PNG file.
 * This is used for item rendering in the inventory and UI.
 */
public class Texture2D {
    private final BufferedImage image;
    private final int width;
    private final int height;
    private final String filename;
    
    /**
     * Creates a new 2D texture from a BufferedImage.
     * 
     * @param image The BufferedImage containing the texture data
     * @param filename The original filename for reference
     */
    public Texture2D(BufferedImage image, String filename) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.filename = filename;
    }
    
    /**
     * Loads a 2D texture from a file.
     * 
     * @param filename The filename of the texture to load
     * @return A new Texture2D instance
     * @throws IOException If the file cannot be loaded
     */
    public static Texture2D loadFromFile(String filename) throws IOException {
        // Load from the actual texture data directory
        File file = new File(filename);
        
        if (!file.exists()) {
            throw new IOException("Texture file not found: " + filename);
        }
        
        BufferedImage image = ImageIO.read(file);
        
        if (image == null) {
            throw new IOException("Failed to load texture: " + filename);
        }
        
        return new Texture2D(image, filename);
    }
    
    /**
     * Loads a 2D texture from a classpath resource.
     * 
     * @param resourcePath The resource path of the texture to load
     * @return A new Texture2D instance
     * @throws IOException If the resource cannot be loaded
     */
    public static Texture2D loadFromResource(String resourcePath) throws IOException {
        InputStream inputStream = Texture2D.class.getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            throw new IOException("Texture resource not found: " + resourcePath);
        }
        
        BufferedImage image = ImageIO.read(inputStream);
        inputStream.close();
        
        if (image == null) {
            throw new IOException("Failed to load texture resource: " + resourcePath);
        }
        
        return new Texture2D(image, resourcePath);
    }
    
    /**
     * Gets the BufferedImage containing the texture data.
     * 
     * @return The texture image
     */
    public BufferedImage getImage() {
        return image;
    }
    
    /**
     * Gets the width of the texture.
     * 
     * @return The width in pixels
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the texture.
     * 
     * @return The height in pixels
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gets the filename of this texture.
     * 
     * @return The filename
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Gets a pixel color from the texture.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The color as an ARGB integer
     */
    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0; // Transparent for out-of-bounds
        }
        return image.getRGB(x, y);
    }
    
    /**
     * Creates a scaled version of this texture.
     * 
     * @param newWidth The desired width
     * @param newHeight The desired height
     * @return A new Texture2D with the scaled image
     */
    public Texture2D createScaled(int newWidth, int newHeight) {
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return new Texture2D(scaledImage, filename + "_scaled_" + newWidth + "x" + newHeight);
    }
}