package me.friedhof.hyperbuilder.computation.modules;

import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;

/**
 * Specialized inventory for smelters with input and output slots.
 * Slot 0: Input slot for items to be smelted
 * Slot 1: Output slot for smelted items
 */
public class SmelterInventory {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int TOTAL_SLOTS = 2;
    
    private final BaseItem[] slots;
    
    /**
     * Creates a new smelter inventory with input and output slots.
     */
    public SmelterInventory() {
        this.slots = new BaseItem[TOTAL_SLOTS];
    }
    
    /**
     * Gets the item in the input slot.
     * 
     * @return The item in the input slot, or null if empty
     */
    public BaseItem getInputItem() {
        return slots[INPUT_SLOT];
    }
    
    /**
     * Gets the item in the output slot.
     * 
     * @return The item in the output slot, or null if empty
     */
    public BaseItem getOutputItem() {
        return slots[OUTPUT_SLOT];
    }
    
    /**
     * Sets the item in the input slot.
     * 
     * @param item The item to set
     */
    public void setInputItem(BaseItem item) {
        slots[INPUT_SLOT] = item;
    }
    
    /**
     * Sets the item in the output slot.
     * 
     * @param item The item to set
     */
    public void setOutputItem(BaseItem item) {
        slots[OUTPUT_SLOT] = item;
    }
    
    /**
     * Gets the item in the specified slot.
     * 
     * @param slot The slot index (0 = input, 1 = output)
     * @return The item in the slot, or null if empty or invalid slot
     */
    public BaseItem getItem(int slot) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            return slots[slot];
        }
        return null;
    }
    
    /**
     * Sets the item in the specified slot.
     * 
     * @param slot The slot index (0 = input, 1 = output)
     * @param item The item to set
     */
    public void setItem(int slot, BaseItem item) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            slots[slot] = item;
        }
    }
    
    /**
     * Checks if the input slot is empty.
     * 
     * @return true if the input slot is empty
     */
    public boolean isInputEmpty() {
        return slots[INPUT_SLOT] == null || slots[INPUT_SLOT].getCount() <= 0;
    }
    
    /**
     * Checks if the output slot is empty.
     * 
     * @return true if the output slot is empty
     */
    public boolean isOutputEmpty() {
        return slots[OUTPUT_SLOT] == null || slots[OUTPUT_SLOT].getCount() <= 0;
    }
    
    /**
     * Checks if the output slot has space for the specified item.
     * 
     * @param item The item to check space for
     * @return true if there is space in the output slot
     */
    public boolean hasOutputSpaceFor(BaseItem item) {
        if (item == null) {
            return true;
        }
        
        BaseItem outputItem = slots[OUTPUT_SLOT];
        
        if (outputItem == null) {
            // Empty slot has space
            return true;
        }
        
        if (!outputItem.getItemId().equals(item.getItemId())) {
            // Different item type, no space
            return false;
        }
        
        // Same item type, check if we can stack
        int currentCount = outputItem.getCount();
        int maxStackSize = outputItem.getMaxStackSize();
        
        return currentCount + item.getCount() <= maxStackSize;
    }
    
    /**
     * Adds an item to the output slot, stacking if possible.
     * 
     * @param item The item to add
     * @return true if the item was added successfully
     */
    public boolean addToOutput(BaseItem item) {
        if (item == null) {
            return true;
        }
        
        BaseItem outputItem = slots[OUTPUT_SLOT];
        
        if (outputItem == null) {
            // Empty slot, place the item
            slots[OUTPUT_SLOT] = item;
            return true;
        }
        
        if (!outputItem.getItemId().equals(item.getItemId())) {
            // Different item type, can't add
            return false;
        }
        
        // Same item type, try to stack
        int currentCount = outputItem.getCount();
        int maxStackSize = outputItem.getMaxStackSize();
        int newCount = currentCount + item.getCount();
        
        if (newCount <= maxStackSize) {
            slots[OUTPUT_SLOT] = outputItem.withCount(newCount);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes one item from the input slot.
     * 
     * @return true if an item was removed
     */
    public boolean consumeInputItem() {
        BaseItem inputItem = slots[INPUT_SLOT];
        
        if (inputItem == null || inputItem.getCount() <= 0) {
            return false;
        }
        
        if (inputItem.getCount() == 1) {
            slots[INPUT_SLOT] = null;
        } else {
            slots[INPUT_SLOT] = inputItem.withCount(inputItem.getCount() - 1);
        }
        
        return true;
    }
    
    /**
     * Gets the total number of slots in this inventory.
     * 
     * @return The number of slots (always 2)
     */
    public int getSize() {
        return TOTAL_SLOTS;
    }
    
    /**
     * Gets the input slot index.
     * 
     * @return The input slot index (0)
     */
    public static int getInputSlotIndex() {
        return INPUT_SLOT;
    }
    
    /**
     * Gets the output slot index.
     * 
     * @return The output slot index (1)
     */
    public static int getOutputSlotIndex() {
        return OUTPUT_SLOT;
    }
    
    /**
     * Checks if a slot index is the input slot.
     * 
     * @param slot The slot index to check
     * @return true if the slot is the input slot
     */
    public static boolean isInputSlot(int slot) {
        return slot == INPUT_SLOT;
    }
    
    /**
     * Checks if a slot index is the output slot.
     * 
     * @param slot The slot index to check
     * @return true if the slot is the output slot
     */
    public static boolean isOutputSlot(int slot) {
        return slot == OUTPUT_SLOT;
    }
}