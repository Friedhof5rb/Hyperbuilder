package me.friedhof.hyperbuilder.computation.modules.interfaces;

/**
 * Interface for items that have texture information for rendering.
 * All items should implement this interface to provide visual representation.
 */
public interface HasTexture {



    public boolean HasTexture();

    /**
     * Gets the texture filename for this item's 4D block texture.
     * Used when the item is placed as a block in the world.
     * 
     * @return The 4D texture filename (without path or extension)
     */
    String getBlockTextureName();
    
    /**
     * Gets the texture filename for this item's 2D inventory icon.
     * Used when displaying the item in inventories and UI.
     * 
     * @return The 2D texture filename (without path or extension)
     */
    default String getItemTextureName() {
        return getBlockTextureName(); // Default uses same name as block texture
    }
    
    /**
     * Gets the texture variant for this specific item instance.
     * Allows for different textures based on item state or metadata.
     * 
     * @return The texture variant identifier, or null for default
     */
    default String getTextureVariant() {
        return null; // Default no variant
    }
}