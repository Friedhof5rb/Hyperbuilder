package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.SmelterInventory;
import me.friedhof.hyperbuilder.computation.modules.SmelterRecipe;
import java.util.ArrayList;

public class SmelterPoweredItem extends Block{
    private SmelterInventory inventory;
    private long processingStartTime;
    private boolean isProcessing;
    private long powerStartTime;
    private static final long PROCESSING_TIME = 5000; // 5 seconds in milliseconds
    private static final long POWER_DURATION = 60000; // 1 minute in milliseconds

    public SmelterPoweredItem() {
        super(Material.SMELTER_POWERED, "Smelter", 999, 0);
        this.inventory = new SmelterInventory();
        this.processingStartTime = 0;
        this.isProcessing = false;
        this.powerStartTime = System.currentTimeMillis();
    }
   
    public SmelterPoweredItem(int count) {
        super(Material.SMELTER_POWERED, "Smelter", 999, count);
        this.inventory = new SmelterInventory();
        this.processingStartTime = 0;
        this.isProcessing = false;
        this.powerStartTime = System.currentTimeMillis();
    }
    
    // Constructor that accepts an existing inventory (for state transfer)
    public SmelterPoweredItem(int count, SmelterInventory existingInventory) {
        super(Material.SMELTER_POWERED, "Smelter", 999, count);
        this.inventory = existingInventory;
        this.processingStartTime = 0;
        this.isProcessing = false;
        this.powerStartTime = System.currentTimeMillis();
    }

    @Override
    public BaseItem withCount(int newCount) {
        return new SmelterPoweredItem(newCount);
    }

    @Override
    public boolean isSolid() {
        return true;
    }
    @Override
    public boolean isBreakable() {
        return true;
    }
    @Override
    public float getCollisionResistance() {
        return 2f;
    }
    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        if(selectedItem instanceof IsTool){
            IsTool tool = (IsTool) selectedItem;
            if(tool.canMine(this)) {
                drops.add(ItemRegistry.createItem(Material.SMELTER, 1));
            }
        }
        return drops;
    }
    
    /**
     * Updates the smelter processing logic.
     * Should be called regularly to check for completed processing and power status.
     * 
     * @return true if smelter is still powered, false if power expired
     */
    public boolean update() {
        // Check if power has expired
        if (!isPowered()) {
            setProcessing(false);
            return false;
        }
        
        // Check if processing is complete
        if (isProcessing && System.currentTimeMillis() - processingStartTime >= PROCESSING_TIME) {
            completeProcessing();
            // Try to start processing the next item automatically
            startProcessing();
        }
        
        return true;
    }
    
    /**
     * Starts processing if conditions are met.
     * 
     * @return true if processing started, false otherwise
     */
    public boolean startProcessing() {
        if (!isPowered() || isProcessing || inventory.isInputEmpty()) {
            return false;
        }
        
        BaseItem inputItem = inventory.getInputItem();
        if (!SmelterRecipe.canSmelt(inputItem)) {
            return false;
        }
        
        BaseItem result = SmelterRecipe.getSmeltingResult(inputItem);
        if (result == null || !inventory.hasOutputSpaceFor(result)) {
            return false;
        }
        
        setProcessing(true);
        return true;
    }
    
    /**
     * Completes the current processing operation.
     */
    private void completeProcessing() {
        if (!isProcessing) {
            return;
        }
        
        BaseItem inputItem = inventory.getInputItem();
        BaseItem result = SmelterRecipe.getSmeltingResult(inputItem);
        
        if (result != null && inventory.hasOutputSpaceFor(result)) {
            inventory.consumeInputItem();
            inventory.addToOutput(result);
        }
        
        setProcessing(false);
    }
    
    /**
     * Gets the processing progress as a percentage.
     * 
     * @return Processing progress from 0.0 to 1.0
     */
    public float getProcessingProgress() {
        if (!isProcessing) {
            return 0.0f;
        }
        
        long elapsed = System.currentTimeMillis() - processingStartTime;
        return Math.min(1.0f, (float) elapsed / PROCESSING_TIME);
    }
    
    /**
     * Gets the remaining power time in milliseconds.
     * 
     * @return Remaining power time, or 0 if not powered
     */
    public long getRemainingPowerTime() {
        if (!isPowered()) {
            return 0;
        }
        return POWER_DURATION - (System.currentTimeMillis() - powerStartTime);
    }
    
    /**
     * Gets the smelter inventory.
     * 
     * @return The smelter inventory
     */
    public SmelterInventory getInventory() {
        return inventory;
    }
    
    /**
     * Checks if the smelter is currently processing an item.
     * 
     * @return true if processing, false otherwise
     */
    public boolean isProcessing() {
        return isProcessing;
    }
    
    /**
     * Sets the processing state of the smelter.
     * 
     * @param processing true to start processing, false to stop
     */
    public void setProcessing(boolean processing) {
        this.isProcessing = processing;
        if (processing) {
            this.processingStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Gets the processing start time.
     * 
     * @return The processing start time in milliseconds
     */
    public long getProcessingStartTime() {
        return processingStartTime;
    }
    
    /**
     * Sets the processing start time.
     * 
     * @param startTime The processing start time in milliseconds
     */
    public void setProcessingStartTime(long startTime) {
        this.processingStartTime = startTime;
    }
    
    /**
     * Gets the power start time.
     * 
     * @return The power start time in milliseconds
     */
    public long getPowerStartTime() {
        return powerStartTime;
    }
    
    /**
     * Gets the power expire time.
     * 
     * @return The power expire time in milliseconds
     */
    public long getPowerExpireTime() {
        return powerStartTime + POWER_DURATION;
    }
    
    /**
     * Sets the power expire time by calculating the power start time.
     * 
     * @param expireTime The power expire time in milliseconds
     */
    public void setPowerExpireTime(long expireTime) {
        this.powerStartTime = expireTime - POWER_DURATION;
    }
    
    /**
     * Checks if the smelter is still powered.
     * 
     * @return true if powered, false otherwise
     */
    public boolean isPowered() {
        return (System.currentTimeMillis() - powerStartTime) < POWER_DURATION;
    }
    
    /**
     * Gets the processing time duration.
     * 
     * @return The processing time in milliseconds
     */
    public static long getProcessingTime() {
        return PROCESSING_TIME;
    }
    
    /**
     * Gets the power duration.
     * 
     * @return The power duration in milliseconds
     */
    public static long getPowerDuration() {
        return POWER_DURATION;
    }
}
