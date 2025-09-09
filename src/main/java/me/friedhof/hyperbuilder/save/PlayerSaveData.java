package me.friedhof.hyperbuilder.save;

import me.friedhof.hyperbuilder.computation.modules.*;
import java.io.Serializable;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;


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
            me.friedhof.hyperbuilder.computation.modules.items.BaseItem item = inventory.getItem(i);
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
                me.friedhof.hyperbuilder.computation.modules.items.BaseItem item = inventoryItems[i].toItem();
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
    private static final long serialVersionUID = 3L; // Updated version for durability support
    
    private final Material itemId;
    private final int count;
    private final int durability; // -1 for non-tools, actual durability for tools
    
    public ItemSaveData(BaseItem item) {
        this.itemId = item.getItemId();
        this.count = item.getCount();
        
        // Save durability if item is a tool
        if (item instanceof me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool) {
            me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool tool = 
                (me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool) item;
            this.durability = tool.getCurrentDurability();
        } else {
            this.durability = -1; // Not a tool
        }
    }
    
    public BaseItem toItem() {
        BaseItem item = ItemRegistry.createItem(itemId, count);
        
        // Restore durability if item is a tool and we have durability data
        if (item instanceof me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool && durability >= 0) {
            me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool tool = 
                (me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool) item;
            tool.setDurability(durability);
        }
        
        return item;
    }
    
    public Material getItemId() { return itemId; }
    public int getCount() { return count; }
    public int getDurability() { return durability; }
}