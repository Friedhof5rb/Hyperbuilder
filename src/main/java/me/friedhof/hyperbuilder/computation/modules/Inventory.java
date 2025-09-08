package me.friedhof.hyperbuilder.computation.modules;

import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;

/**
 * Represents an inventory that can store items.
 * Used by players and other entities that can hold items.
 */
public class Inventory {
    private final BaseItem[] slots;
    
    /**
     * Creates a new inventory with the specified number of slots.
     * 
     * @param size The number of slots in this inventory
     */
    public Inventory(int size) {
        this.slots = new BaseItem[size];
    }
    
    /**
     * Gets the number of slots in this inventory.
     * 
     * @return The inventory size
     */
    public int getSize() {
        return slots.length;
    }
    
    /**
     * Gets the item in the specified slot.
     * 
     * @param slot The slot index
     * @return The item in the slot, or null if the slot is empty
     */
    public BaseItem getItem(int slot) {
        if (isValidSlot(slot)) {
            return slots[slot];
        }
        return null;
    }
    
    /**
     * Sets the item in the specified slot.
     * 
     * @param slot The slot index
     * @param item The item to set, or null to clear the slot
     * @return true if the item was set, false otherwise
     */
    public boolean setItem(int slot, BaseItem item) {
        if (isValidSlot(slot)) {
            slots[slot] = item;
            return true;
        }
        return false;
    }
    
    /**
     * Adds an item to the inventory.
     * Tries to stack the item with existing items of the same type.
     * 
     * @param item The item to add
     * @return true if the item was added, false if there was no room
     */
    public boolean addItem(BaseItem item) {
        if (item == null) {
            return true;
        }
        
        // Try to stack with existing items
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getItemId().equals(item.getItemId())) {
                int currentCount = slots[i].getCount();
                int maxStackSize = slots[i].getMaxStackSize();
                
                if (currentCount < maxStackSize) {
                    int spaceAvailable = maxStackSize - currentCount;
                    int amountToAdd = Math.min(spaceAvailable, item.getCount());
                    
                    slots[i] = slots[i].withCount(currentCount + amountToAdd);
                    
                    // If we added all items, return true
                    if (amountToAdd >= item.getCount()) {
                        return true;
                    }
                    
                    // Otherwise, update the item and continue
                    item = item.withCount(item.getCount() - amountToAdd);
                }
            }
        }
        
        // If we get here, we need to find an empty slot
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }
        
        // If we get here, there was no room
        return false;
    }
    
    /**
     * Adds an item to the inventory by item ID and count.
     * Convenience method that creates a BaseItem object internally.
     * 
     * @param itemId The item ID
     * @param count The number of items to add
     * @return true if the item was added, false if there was no room
     */
    public boolean addItem(Material itemId, int count) {
        if (count <= 0) {
            return true;
        }
        
        BaseItem item = ItemRegistry.createItem(itemId, count);
        return addItem(item);
    }
    
    /**
     * Removes an item from the inventory.
     * 
     * @param item The item to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(BaseItem item) {
        if (item == null) {
            return true;
        }
        
        int remainingToRemove = item.getCount();
        
        // Find matching items
        for (int i = 0; i < slots.length && remainingToRemove > 0; i++) {
            if (slots[i] != null && slots[i].getItemId().equals(item.getItemId())) {
                int currentCount = slots[i].getCount();
                
                if (currentCount <= remainingToRemove) {
                    // Remove the entire stack
                    remainingToRemove -= currentCount;
                    slots[i] = null;
                } else {
                    // Remove part of the stack
                    slots[i] = slots[i].withCount(currentCount - remainingToRemove);
                    remainingToRemove = 0;
                }
            }
        }
        
        // Return true if we removed all requested items
        return remainingToRemove == 0;
    }
    
    /**
     * Removes an item from the inventory by item ID and count.
     * Convenience method that creates a BaseItem object internally.
     * 
     * @param itemId The item ID
     * @param count The number of items to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(Material itemId, int count) {
        if (count <= 0) {
            return true;
        }
        
        BaseItem item = ItemRegistry.createItem(itemId, count);
        return removeItem(item);
    }
    
    /**
     * Checks if the inventory contains the specified item.
     * 
     * @param item The item to check for
     * @return true if the inventory contains the item, false otherwise
     */
    public boolean contains(BaseItem item) {
        if (item == null) {
            return true;
        }
        
        int count = 0;
        
        // Count matching items
        for (BaseItem slotItem : slots) {
            if (slotItem != null && slotItem.getItemId().equals(item.getItemId())) {
                count += slotItem.getCount();
                
                if (count >= item.getCount()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a slot index is valid for this inventory.
     * 
     * @param slot The slot index to check
     * @return true if the slot is valid, false otherwise
     */
    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < slots.length;
    }
}