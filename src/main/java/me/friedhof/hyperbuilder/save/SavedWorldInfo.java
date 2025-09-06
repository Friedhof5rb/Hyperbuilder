package me.friedhof.hyperbuilder.save;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contains metadata information about a saved world.
 * This class is used to display world information in the load game dialog
 * and to manage world save files.
 */
public class SavedWorldInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private final long seed;
    private final LocalDateTime creationDate;
    private final LocalDateTime lastPlayed;
    private final String fileName;
    
    /**
     * Creates a new SavedWorldInfo instance.
     * 
     * @param name The display name of the world
     * @param seed The world generation seed
     * @param creationDate When the world was first created
     * @param lastPlayed When the world was last played
     * @param fileName The file name used to save this world
     */
    public SavedWorldInfo(String name, long seed, LocalDateTime creationDate, LocalDateTime lastPlayed, String fileName) {
        this.name = name;
        this.seed = seed;
        this.creationDate = creationDate;
        this.lastPlayed = lastPlayed;
        this.fileName = fileName;
    }
    
    /**
     * Creates a new SavedWorldInfo for a newly created world.
     * 
     * @param name The display name of the world
     * @param seed The world generation seed
     * @param fileName The file name used to save this world
     */
    public SavedWorldInfo(String name, long seed, String fileName) {
        this(name, seed, LocalDateTime.now(), LocalDateTime.now(), fileName);
    }
    
    /**
     * Gets the display name of the world.
     * 
     * @return The world name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the world generation seed.
     * 
     * @return The seed used for world generation
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * Gets the creation date of the world.
     * 
     * @return When the world was first created
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    /**
     * Gets the last played date of the world.
     * 
     * @return When the world was last played
     */
    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }
    
    /**
     * Gets the file name used to save this world.
     * 
     * @return The save file name
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Creates a new SavedWorldInfo with updated last played time.
     * 
     * @return A new SavedWorldInfo instance with current time as last played
     */
    public SavedWorldInfo withUpdatedLastPlayed() {
        return new SavedWorldInfo(name, seed, creationDate, LocalDateTime.now(), fileName);
    }
    
    /**
     * Gets a formatted string representation of the creation date.
     * 
     * @return Formatted creation date string
     */
    public String getFormattedCreationDate() {
        return creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    /**
     * Gets a formatted string representation of the last played date.
     * 
     * @return Formatted last played date string
     */
    public String getFormattedLastPlayed() {
        return lastPlayed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    @Override
    public String toString() {
        return name + " (Seed: " + seed + ", Created: " + getFormattedCreationDate() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SavedWorldInfo that = (SavedWorldInfo) obj;
        return seed == that.seed && 
               name.equals(that.name) && 
               fileName.equals(that.fileName);
    }
    
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Long.hashCode(seed);
        result = 31 * result + fileName.hashCode();
        return result;
    }
}