# 4D Coordinate Mapping System

## Overview

This document explains the coordinate mapping system used in the 4D voxel game to render a 4D world as a 7x7 grid of 2D slices.

## Core Concept

The game displays a **7x7 grid of slices**, where each slice shows a 2D cross-section of the 4D world. The key insight is that the slice grid represents the **two dimensions orthogonal to the current viewing plane**.

## Viewing Modes

The game supports three viewing modes, each showing a different 2D plane of the 4D world:

### X Mode (Default)
- **Viewing Plane**: X-Y plane
- **Slice Grid Dimensions**: 
  - Horizontal (slice grid X) → Z dimension
  - Vertical (slice grid Y) → W dimension
- **Within Each Slice**: X (horizontal) and Y (vertical) coordinates

### Z Mode
- **Viewing Plane**: Z-Y plane
- **Slice Grid Dimensions**:
  - Horizontal (slice grid X) → X dimension
  - Vertical (slice grid Y) → W dimension
- **Within Each Slice**: Z (horizontal) and Y (vertical) coordinates

### W Mode
- **Viewing Plane**: W-Y plane
- **Slice Grid Dimensions**:
  - Horizontal (slice grid X) → X dimension
  - Vertical (slice grid Y) → Z dimension
- **Within Each Slice**: W (horizontal) and Y (vertical) coordinates

## Implementation Details

### Camera.getSliceCenterWorldCoord()

This method converts slice grid coordinates (0-6, 0-6) to 4D world coordinates:

```java
// X mode: viewing X-Y plane, grid represents Z and W
case X:
    return new Vector4D(
        worldOffset.getX(),                    // Fixed for viewing plane
        worldOffset.getY(),                    // Fixed for viewing plane
        worldOffset.getZ() + horizontalOffset, // Grid horizontal → Z
        worldOffset.getW() + verticalOffset    // Grid vertical → W
    );
```

### SliceRenderer Player Positioning

Player slice calculation must match the camera mapping:

```java
// X mode: grid represents Z (horizontal) and W (vertical)
case X:
    playerSliceHorizontal = (int) Math.round(playerViewPos.getZ()) + getSliceCenter();
    playerSliceVertical = (int) Math.round(playerViewPos.getW()) + getSliceCenter();
```

### Game.screenToWorldCoordinates()

Click-to-world coordinate conversion maintains consistency:

```java
// X mode: viewing X-Y plane, slice grid represents Z and W (fixed for this slice)
case X:
    worldX = (int) Math.floor(sliceCenterWorld.getX()) - SliceRenderer.getSliceCenter() + blockX;
    worldZ = (int) Math.floor(sliceCenterWorld.getZ()); // Fixed from slice grid
    worldW = (int) Math.floor(sliceCenterWorld.getW()); // Fixed from slice grid
```

## Key Principles

1. **Orthogonal Dimensions**: The slice grid always represents the two dimensions orthogonal to the current viewing plane.

2. **Consistent Mapping**: All components (Camera, SliceRenderer, Game) use the same coordinate mapping logic.

3. **Fixed Cross-Slice Coordinates**: Within each slice, the two orthogonal dimensions remain fixed, while the viewing plane dimensions vary.

4. **Center-Based Indexing**: Slice (3,3) represents the center of the grid, with offsets calculated from this center.

## Debugging Tips

- **Duplicate Slices**: Usually caused by inconsistent coordinate mapping between components
- **Wrong Player Position**: Check that player slice calculation matches camera mapping
- **Click Misalignment**: Verify that screenToWorldCoordinates uses the same mapping as camera

## Previous Issues Fixed

- **Root Cause**: The original implementation incorrectly mapped slice grid coordinates to the viewing plane dimensions instead of the orthogonal dimensions
- **Solution**: Redesigned the coordinate system so slice grid represents orthogonal dimensions, eliminating duplication
- **Impact**: Each slice now shows a unique cross-section of the 4D world