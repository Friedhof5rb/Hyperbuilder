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
    private long lastUpdateTime;
    
    public Water() {
        super(Material.WATER, "Water", 999, 1);
        this.flowLevel = 7; // Default to source block
        this.isSource = true;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public Water(int count) {
        super(Material.WATER, "Water", 999, count);
        this.flowLevel = 7; // Default to source block
        this.isSource = true;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public Water(int flowLevel, boolean isSource) {
        super(Material.WATER, "Water", 1, 1);
        this.flowLevel = Math.max(0, Math.min(7, flowLevel));
        this.isSource = isSource;
        this.lastUpdateTime = System.currentTimeMillis();
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
    
    /**
     * Updates water flow mechanics
     * @param world The world instance
     * @param position Current position of this water block
     * @return true if water should continue to exist, false if it should be removed
     */
    public boolean updateFlow(World world, Vector4DInt position) {
        long currentTime = System.currentTimeMillis();
        
        // Only update every 500ms to prevent too rapid flow
        if (currentTime - lastUpdateTime < 500) {
            return true;
        }
        lastUpdateTime = currentTime;
        
        // If flow level is 0, remove this water block
        if (flowLevel <= 0) {
            return false;
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
            
            // Flow down without decreasing level, but always as non-source
            Water downwardWater = new Water(flowLevel, false);
            world.setBlock(belowPos, downwardWater);
            
            // Non-source blocks should only flow down if there's no block beneath
            if (!isSource) {
                return true; // Don't flow horizontally, just down
            }
        } else if (belowBlock instanceof Water) {
            Water belowWater = (Water) belowBlock;
            // Merge with water below if this has higher level
            if (flowLevel > belowWater.getFlowLevel()) {
                belowWater.setFlowLevel(Math.max(belowWater.getFlowLevel(), flowLevel));
                // Only create source if BOTH blocks are source blocks
                if (isSource && belowWater.isSource() && flowLevel >= 6 && belowWater.getFlowLevel() >= 6) {
                    belowWater.setSource(true);
                    belowWater.setFlowLevel(7);
                } else {
                    // Otherwise, keep as non-source
                    belowWater.setSource(false);
                }
            }
        }
        
        // Flow horizontally based on block type:
        // - Source blocks: flow horizontally if all sides are free or can't flow down
        // - Non-source blocks: only flow horizontally if there's a solid block beneath (not water)
        boolean canFlowDown = (belowBlock != null && belowBlock.getBlockId().equals(Material.AIR));
        boolean hasSolidBelow = (belowBlock != null && !belowBlock.getBlockId().equals(Material.AIR) && !(belowBlock instanceof Water));
        
        if (isSource) {
            // Source blocks flow horizontally when they can't flow down or when all sides are free
            // But not if there's water below (water below doesn't count as support)
            if ((!canFlowDown && !(belowBlock instanceof Water)) || allSidesFree(world, position)) {
                flowHorizontally(world, position);
            }
        } else {
            // Non-source blocks only flow horizontally if there's a solid block beneath (not water)
            if (hasSolidBelow) {
                flowHorizontally(world, position);
            }
        }
        
        return true;
    }
    
    /**
     * Checks if all horizontal sides around this position are free (air)
     */
    private boolean allSidesFree(World world, Vector4DInt position) {
        Vector4DInt[] directions = {
            new Vector4DInt(1, 0, 0, 0),   // +X
            new Vector4DInt(-1, 0, 0, 0),  // -X
            new Vector4DInt(0, 0, 1, 0),   // +Z
            new Vector4DInt(0, 0, -1, 0),  // -Z
            new Vector4DInt(0, 0, 0, 1),   // +W
            new Vector4DInt(0, 0, 0, -1)   // -W
        };
        
        for (Vector4DInt direction : directions) {
            Vector4DInt checkPos = new Vector4DInt(
                position.getX() + direction.getX(),
                position.getY() + direction.getY(),
                position.getZ() + direction.getZ(),
                position.getW() + direction.getW()
            );
            
            Block checkBlock = world.getBlock(checkPos);
             if (checkBlock == null || !canFlowInto(checkBlock)) {
                 return false; // Found a block that water can't flow into
             }
        }
        
        return true; // All sides are free
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
        if (flowLevel <= 1) return; // Can't flow if level is too low
        
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
                if (newLevel > 0) {
                    Water newWater = new Water(newLevel, false);
                    world.setBlock(targetPos, newWater);
                }
            } else if (targetBlock instanceof Water) {
                Water targetWater = (Water) targetBlock;
                // Merge flows - if both are high level, create source
                if (isSource && targetWater.isSource() && flowLevel >= 6 && targetWater.getFlowLevel() >= 6) {
                    targetWater.setSource(true);
                    targetWater.setFlowLevel(7);
                } else if (flowLevel > targetWater.getFlowLevel()) {
                    // Equalize levels
                    int avgLevel = (flowLevel + targetWater.getFlowLevel()) / 2;
                    targetWater.setFlowLevel(avgLevel);
                    // Keep as non-source unless both original blocks are sources
                    if (!(isSource && targetWater.isSource())) {
                        targetWater.setSource(false);
                    }
                }
            }
        }
    }
}
