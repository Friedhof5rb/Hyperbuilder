package me.friedhof.hyperbuilder.save;

import me.friedhof.hyperbuilder.computation.modules.*;
import java.io.Serializable;

/**
 * Serializable data class for saving and loading Player data.
 * Contains all necessary information to reconstruct a Player object.
 */
public class PlayerSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Player identification
    private final int id;
    private final String username;
    
    // Position and movement
    private final double posX, posY, posZ, posW;
    private final double velX, velY, velZ, velW;
    
    // Player state
    private final int health;
    private final int maxHealth;
    private final int selectedSlot;
    private final boolean onGround;
    private final boolean gravity;
    
    // Inventory data
    private final ItemSaveData[] inventoryItems;
    
    /**
     * Creates PlayerSaveData from a Player object.
     * 
     * @param player The player to serialize
     */
    public PlayerSaveData(Player player) {
        this.id = player.getId();
        this.username = player.getUsername();
        
        // Position and velocity
        Vector4D pos = player.getPosition();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.posW = pos.getW();
        
        Vector4D vel = player.getVelocity();
        this.velX = vel.getX();
        this.velY = vel.getY();
        this.velZ = vel.getZ();
        this.velW = vel.getW();
        
        // Player state
        this.health = player.getHealth();
        this.maxHealth = player.getMaxHealth();
        this.selectedSlot = player.getSelectedSlot();
        this.onGround = false; // Will be recalculated on load
        this.gravity = player.hasGravity();
        
        // Serialize inventory
        Inventory inventory = player.getInventory();
        this.inventoryItems = new ItemSaveData[inventory.getSize()];
        for (int i = 0; i < inventory.getSize(); i++) {
            Item item = inventory.getItem(i);
            if (item != null) {
                this.inventoryItems[i] = new ItemSaveData(item);
            }
        }
    }
    
    /**
     * Reconstructs a Player object from this save data.
     * 
     * @param world The world to create the player in
     * @return The reconstructed Player object
     */
    public Player toPlayer(World world) {
        // Create player with basic data
        Vector4D position = new Vector4D(posX, posY, posZ, posW);
        Player player = new Player(id, position, username);
        
        // Restore velocity
        player.setVelocity(new Vector4D(velX, velY, velZ, velW));
        
        // Restore player state
        player.setHealth(health);
        player.setSelectedSlot(selectedSlot);
        player.setGravity(gravity);
        
        // Restore inventory
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventoryItems.length && i < inventory.getSize(); i++) {
            if (inventoryItems[i] != null) {
                Item item = inventoryItems[i].toItem();
                inventory.setItem(i, item);
            }
        }
        
        return player;
    }
    
    // Getters for debugging/inspection
    public int getId() { return id; }
    public String getUsername() { return username; }
    public Vector4D getPosition() { return new Vector4D(posX, posY, posZ, posW); }
    public Vector4D getVelocity() { return new Vector4D(velX, velY, velZ, velW); }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getSelectedSlot() { return selectedSlot; }
    public boolean hasGravity() { return gravity; }
}

/**
 * Serializable data class for Item objects.
 */
class ItemSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final byte type;
    private final int count;
    
    public ItemSaveData(Item item) {
        this.type = item.getType();
        this.count = item.getCount();
    }
    
    public Item toItem() {
        return new Item(type, count);
    }
    
    public byte getType() { return type; }
    public int getCount() { return count; }
}