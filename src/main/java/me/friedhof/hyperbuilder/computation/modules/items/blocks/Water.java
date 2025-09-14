package me.friedhof.hyperbuilder.computation.modules.items.blocks;

import me.friedhof.hyperbuilder.computation.modules.Material;
import me.friedhof.hyperbuilder.computation.modules.items.BaseItem;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;
import me.friedhof.hyperbuilder.computation.modules.World;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;
import me.friedhof.hyperbuilder.computation.modules.DroppedItem;
import me.friedhof.hyperbuilder.computation.modules.Vector4D;


import java.util.ArrayList;
import java.util.List;

public class Water extends Block {
    private int flowLevel; // 0-7, where 7 is source block
    private boolean isSource; // true if this is a source block
    private Vector4DInt originPosition; // Position of the water block this one originated from
    private boolean markedForRemoval; // Flag to mark this water block for removal
    
    public Water() {
        super(Material.WATER, "Water", 999, 1);
        this.flowLevel = 7; // Default to source block
        this.isSource = true;
        this.originPosition = null; // Source blocks have no origin
        this.markedForRemoval = false;
    }
    
    public Water(int count) {
        super(Material.WATER, "Water", 999, count);
        this.flowLevel = 7; // Default to source block
        this.isSource = true;
        this.originPosition = null; // Source blocks have no origin
        this.markedForRemoval = false;
    }
    
    public Water(int flowLevel, boolean isSource) {
        super(Material.WATER, "Water", 999, 1);
        this.flowLevel = Math.max(0, Math.min(7, flowLevel));
        this.isSource = isSource;
        this.originPosition = null; // Will be set when water flows
        this.markedForRemoval = false;
    }
    
    public Water(int flowLevel, boolean isSource, Vector4DInt originPosition) {
        super(Material.WATER, "Water", 999, 1);
        this.flowLevel = Math.max(0, Math.min(7, flowLevel));
        this.isSource = isSource;
        this.originPosition = originPosition;
        this.markedForRemoval = false;
    }
    
    @Override
    public BaseItem withCount(int newCount) {
        Water newWater = new Water(newCount);
        newWater.flowLevel = this.flowLevel;
        newWater.isSource = this.isSource;
        return newWater;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isBreakable() {
        return false;
    }
    
    @Override
    public ArrayList<BaseItem> drops(BaseItem selectedItem) {
        ArrayList<BaseItem> drops = new ArrayList<BaseItem>();
        return drops;
    }

    @Override
    public int getBreakTier() {
        return 0;
    }
    
    @Override
    public String getBreakType() {
        return "bucket";
    }
    
    // Water flow methods
    public int getFlowLevel() {
        return flowLevel;
    }
    
    public void setFlowLevel(int level) {
        this.flowLevel = Math.max(0, Math.min(7, level));
    }
    
    public boolean isSource() {
        return isSource;
    }
    
    public void setSource(boolean source) {
        this.isSource = source;
    }
    
    public Vector4DInt getOriginPosition() {
        return originPosition;
    }
    
    public void setOriginPosition(Vector4DInt originPosition) {
        this.originPosition = originPosition;
    }
    
    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }
    
    public void setMarkedForRemoval(boolean markedForRemoval) {
        this.markedForRemoval = markedForRemoval;
    }
    
    @Override
    public String getTextureVariant() {
        // Return texture variant based on flow level for transparency
        // Flow level 7 (source) = no transparency
        // Flow level 6 = 1 pixel missing from top
        // Flow level 5 = 2 pixels missing from top, etc.
        if (flowLevel == 7) {
            return null; // Default texture (no transparency)
        } else {
            return "flow_" + flowLevel; // e.g., "flow_6", "flow_5", etc.
        }
    }
    
    /**
     * Updates water flow mechanics
     * @param world The world instance
     * @param position Current position of this water block
     * @return true if water should continue to exist, false if it should be removed
     */
    public void updateFlow(World world, Vector4DInt position) {
    
        // Phase 1: Check removal conditions but don't remove yet
        // Check if this water block should be marked for removal due to broken origin chain
        if (originPosition != null) {
            Block originBlock = world.getBlock(originPosition);
            if (!(originBlock instanceof Water)) {
                // Origin block is no longer water, mark for removal
                markedForRemoval = true;
            }
        }
        
        // If flow level is 0, mark for removal
        if (flowLevel < 0) {
            markedForRemoval = true;
        }
        
        // Phase 2: Handle actual removal after all blocks have been checked
        if (markedForRemoval) {
            return; // Remove this water block
        }
        
        
      
        
        // Check if there's space below - water flows down first
        Vector4DInt belowPos = new Vector4DInt(position.getX(), position.getY() - 1, position.getZ(), position.getW());
        Block belowBlock = world.getBlock(belowPos);
        
        if (belowBlock != null && canFlowInto(belowBlock)) {
            // Water flows into and breaks Grass/Flint blocks, dropping their items
            if (belowBlock.getBlockId().equals(Material.GRASS) || belowBlock.getBlockId().equals(Material.FLINT)) {
                // Get drops from the block being broken
                ArrayList<BaseItem> drops = belowBlock.drops(null);
                if (drops != null) {
                    // Calculate drop position (center of the broken block)
                    Vector4D dropPos = new Vector4D(
                        belowPos.getX() + 0.5,
                        belowPos.getY() + 0.5,
                        belowPos.getZ() + 0.5,
                        belowPos.getW() + 0.5
                    );
                    
                    // Spawn dropped items
                    for (BaseItem item : drops) {
                        DroppedItem droppedItem = 
                            new DroppedItem(
                                world.getNextEntityId(), dropPos, item);
                        world.addEntity(droppedItem);
                    }
                }
            }
            
            // Flow down with level 7 (water flowing down is always at maximum level)
            Water downwardWater = new Water(7, false, position);
            world.setBlock(belowPos, downwardWater);
            
            // Non-source blocks should only flow down if there's no block beneath
            if (!isSource) {
                return; // Don't flow horizontally, just down
            }
        } else if (belowBlock instanceof Water) {
            // Don't create new water if water already exists below!
            // This was causing infinite water generation and massive lag
            Water belowWater = (Water) belowBlock;
            
            // Only fill if the water below has less than level 7
            if (belowWater.getFlowLevel() < 7) {
                belowWater.setFlowLevel(7);
                belowWater.setSource(false);
            }
            
            // Non-source blocks should only flow down if there's no block beneath
            if (!isSource) {
                return; // Don't flow horizontally, just down
            }
        }
        
      
        
        // Flow logic:
        // - Source blocks: flow horizontally if all sides are free or can't flow down
        // - Non-source blocks: only flow horizontally if there's a solid block beneath (not water)
        boolean canFlowDown = (belowBlock != null && canFlowInto(belowBlock));
        boolean hasSolidBelow = (belowBlock != null && !canFlowInto(belowBlock) && !(belowBlock instanceof Water));
        
        if (isSource) {
            // Source blocks flow horizontally when they can't flow down or when at least one side is free
            // But not if there's water below (water below doesn't count as support)
            if ((!canFlowDown && !(belowBlock instanceof Water)) || hasAtLeastOneFreeSide(world, position)) {
                flowHorizontally(world, position);
            }
        } else {
            // Non-source blocks only flow horizontally if there's a solid block beneath (not water)
            if (hasSolidBelow) {
                flowHorizontally(world, position);
            }
        }
        
        return;
    }
     

    


     /**
      * Checks if water has at least one free side where it can flow horizontally
      */
     private boolean hasAtLeastOneFreeSide(World world, Vector4DInt position) {
         // Define horizontal directions (X, Z, W)
         Vector4DInt[] directions = {
             new Vector4DInt(1, 0, 0, 0),   // +X
             new Vector4DInt(-1, 0, 0, 0),  // -X
             new Vector4DInt(0, 0, 1, 0),   // +Z
             new Vector4DInt(0, 0, -1, 0),  // -Z
             new Vector4DInt(0, 0, 0, 1),   // +W
             new Vector4DInt(0, 0, 0, -1)   // -W
         };
         
         for (Vector4DInt direction : directions) {
             Vector4DInt neighborPos = new Vector4DInt(
                 position.getX() + direction.getX(),
                 position.getY() + direction.getY(),
                 position.getZ() + direction.getZ(),
                 position.getW() + direction.getW()
             );
             
             Block neighborBlock = world.getBlock(neighborPos);
             
             // Check if this direction has a free space where water can flow
             if (canFlowInto(neighborBlock)) {
                 return true;
             }
         }
         
         return false;
     }

     /**
      * Checks if water can flow into a block (treats Air, Grass, and Flint as flowable)
      */
     private boolean canFlowInto(Block block) {
         if (block == null) return false;
         Material blockType = block.getBlockId();
         return blockType.equals(Material.AIR) || 
                blockType.equals(Material.GRASS) || 
                blockType.equals(Material.FLINT);
     }
    
    /**
     * Handles horizontal water flow in X, Z, W directions
     */
    private void flowHorizontally(World world, Vector4DInt position) {
        if (flowLevel <= 0) return; // Can't flow if level is too low
        
        // Define horizontal directions (X, Z, W)
        Vector4DInt[] directions = {
            new Vector4DInt(1, 0, 0, 0),   // +X
            new Vector4DInt(-1, 0, 0, 0),  // -X
            new Vector4DInt(0, 0, 1, 0),   // +Z
            new Vector4DInt(0, 0, -1, 0),  // -Z
            new Vector4DInt(0, 0, 0, 1),   // +W
            new Vector4DInt(0, 0, 0, -1)   // -W
        };
        
        for (Vector4DInt direction : directions) {
            Vector4DInt targetPos = new Vector4DInt(
                position.getX() + direction.getX(),
                position.getY() + direction.getY(),
                position.getZ() + direction.getZ(),
                position.getW() + direction.getW()
            );
            
            Block targetBlock = world.getBlock(targetPos);
            
            if (targetBlock != null && canFlowInto(targetBlock)) {
                // Water flows into and breaks Grass/Flint blocks, dropping their items
                if (targetBlock.getBlockId().equals(Material.GRASS) || targetBlock.getBlockId().equals(Material.FLINT)) {
                    // Get drops from the block being broken
                    java.util.ArrayList<me.friedhof.hyperbuilder.computation.modules.items.BaseItem> drops = targetBlock.drops(null);
                    if (drops != null) {
                        // Calculate drop position (center of the broken block)
                        Vector4D dropPos = new Vector4D(
                            targetPos.getX() + 0.5,
                            targetPos.getY() + 0.5,
                            targetPos.getZ() + 0.5,
                            targetPos.getW() + 0.5
                        );
                        
                        // Spawn dropped items
                        for (me.friedhof.hyperbuilder.computation.modules.items.BaseItem item : drops) {
                            me.friedhof.hyperbuilder.computation.modules.DroppedItem droppedItem = 
                                new me.friedhof.hyperbuilder.computation.modules.DroppedItem(
                                    world.getNextEntityId(), dropPos, item);
                            world.addEntity(droppedItem);
                        }
                    }
                }
                
                // Flow to empty space with decreased level, always as non-source
                int newLevel = flowLevel - 1;
                if (newLevel >= 0) {
                    Water newWater = new Water(newLevel, false, position);
                    world.setBlock(targetPos, newWater);
                }
            } else if (targetBlock instanceof Water) {
                Water targetWater = (Water) targetBlock;
                
                // Only flow if this water has higher level (water flows downhill)
                if (flowLevel > targetWater.getFlowLevel()) {
                   
                    // Transfer water
                    int newTargetLevel = flowLevel - 1;
                    
                    targetWater.setFlowLevel(newTargetLevel);
                    // Handle source block logic
                    if (isSource && targetWater.isSource() && newTargetLevel >= 6) {
                        targetWater.setFlowLevel(7);
                    } else {
                        targetWater.setSource(false);
                    }
                
                }
            }
        }
    }
    
}
