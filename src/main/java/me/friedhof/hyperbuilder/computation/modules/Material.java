package me.friedhof.hyperbuilder.computation.modules;

/**
 * Enum containing all material/item IDs used in the game.
 * This provides a centralized location for all item identifiers,
 * making it easier to manage and reference them throughout the codebase.
 */
public enum Material {
    AIR("air"),
    DIRT("dirt"),
    GRASS("grass"),
    GRASS_BLOCK("grass_block"),
    STONE("stone"),
    WOOD_LOG("wood_log"),
    LEAVES("leaves"),
    SAPLING("sapling"),
    FLINT("flint"),
    PLANT_FIBER("plant_fiber"),
    STICKS("stick");
    



    private final String id;
    
    /**
     * Constructor for Materials enum.
     * 
     * @param id The string identifier for this material
     */
    Material(String id) {
        this.id = id;
    }
    
    /**
     * Gets the string identifier for this material.
     * 
     * @return The material ID string
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets a Materials enum from its string ID.
     * 
     * @param id The string ID to look up
     * @return The corresponding Materials enum, or null if not found
     */
    public static Material fromId(String id) {
        for (Material material : Material.values()) {
            if (material.getId().equals(id)) {
                return material;
            }
        }
        return null;
    }
    
    /**
     * Returns the string representation of this material.
     * 
     * @return The material ID string
     */
    @Override
    public String toString() {
        return id;
    }
}