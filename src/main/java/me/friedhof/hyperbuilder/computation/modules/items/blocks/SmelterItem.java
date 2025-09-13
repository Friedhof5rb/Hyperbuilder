package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.interfaces.IsTool;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.SmelterInventory;
import me.friedhof.hyperbuilder.computation.modules.SmelterRecipe;

import java.util.ArrayList;

public class SmelterItem extends Block{
    private SmelterInventory inventory;
    private long processingStartTime;
    private boolean isProcessing;
    private static final long PROCESSING_TIME = 5000; // 5 seconds in milliseconds
    
    public SmelterItem() {
        super(Material.SMELTER, "Smelter", 999, 0);
        this.inventory = new SmelterInventory();
        this.processingStartTime = 0;
        this.isProcessing = false;
    }
   
    public SmelterItem(int count) {
        super(Material.SMELTER, "Smelter", 999, count);
        this.inventory = new SmelterInventory();
        this.processingStartTime = 0;
        this.isProcessing = false;
    }
    
    // Constructor that accepts an existing inventory (for state transfer)
    public SmelterItem(int count, SmelterInventory existingInventory) {
        super(Material.SMELTER, "Smelter", 999, count);
        this.inventory = existingInventory;
        this.processingStartTime = 0;
        this.isProcessing = false;
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new SmelterItem(newCount);
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
                drops.add(this);
            }
        }
        return drops;
    }
    
    /**
     * Updates the smelter processing logic.
     * Should be called regularly to check for completed processing.
     */
    public void update() {
        if (isProcessing && System.currentTimeMillis() - processingStartTime >= PROCESSING_TIME) {
            completeProcessing();
        }
    }
    
    /**
     * Starts processing if conditions are met.
     * 
     * @return true if processing started, false otherwise
     */
    public boolean startProcessing() {
        if (isProcessing || inventory.isInputEmpty()) {
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
     * Gets the processing time duration.
     * 
     * @return The processing time in milliseconds
     */
    public static long getProcessingTime() {
        return PROCESSING_TIME;
    }
    @Override
    public int getBreakTier() {
        return 1;
    }
    @Override
    public String getBreakType() {
        return "pickaxe";
    }
}
