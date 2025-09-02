# 4D Voxel Game Design Document

## Game Concept Overview

A 4D voxel game that renders as a 2D grid of 2D grids. The player navigates a sidescroller-style 2D world (similar to Terraria) but can move between different "slices" in a structured 4D space.

## Core Mechanics

### Player Movement
- **Within Slice**: Left/right walking and jumping in a 2D sidescroller environment
- **Between Slices**: Can move to adjacent slices (left, right, up, down) if the destination area is not obstructed
- **No Rotation**: Player cannot rotate view, maintaining consistent orientation

### World Structure
- **Slice Size**: 7x7 blocks per 2D slice
- **Display Grid**: 7x7 grid of slices visible at once (49 total slices displayed)
- **Player Position**: Always displayed in the center slice
- **Overall World Size**: Several thousand blocks in each 4D direction
- **Chunk System**: 4D chunks required for the large world size

### Interaction System
- **Range**: 4D hypersphere with 3-block radius (Euclidean distance)
- **Actions**: Place and destroy blocks within range
- **Cross-Slice Interaction**: Can interact with blocks in adjacent slices if within range

### Visual System
- **Style**: Pixel-based terrain like Terraria
- **Block Textures**: 4D textures (8x8x8x8 resolution)
- **Item Textures**: 2D textures for items and item representations of blocks
- **Player Textures**: 4D textures
- **Movement**: Continuous movement between slices with terrain warping
- **Update Frequency**: Terrain updates when player moves 1/8th of a block in any direction

### World Generation
- **Biome**: Initially one grassy biome with dirt layers and stone underneath
- **Features**: Caves generated within the terrain
- **Seed-Based**: Deterministic generation for multiplayer consistency
- **Expandable**: Structure allows for adding different biomes

### Items and Inventory
- **Block Types**: Standard blocks (dirt, stone, etc.)
- **Tools**: Pickaxes and other standard tools
- **Natural Items**: Trees, wood
- **Inventory System**: Standard item storage and management

### Multiplayer Support
- **Architecture**: Client-server model
- **Player Visibility**: Players visible across slice boundaries if their 4D texture is visible from current position
- **Real-time**: Designed for eventual real-time multiplayer implementation

### Data Persistence
- **Save/Load**: Option to save and load worlds
- **Format**: World data saved to files

## Technical Architecture

The code follows a modular architecture with these principles:
- **Reusable Code Modules**
- **Dedicated "Glue" Code**
- **Separate Code and Data**
- **Control the "Time Spaghetti"**
- **Separate Gameplay and Art (Simulation and View)**
- **Two Main Systems**: Rendering and Computation (each with their own glue code and modules)

### Technology Stack
- **Language**: Java
- **Rendering**: Vanilla Java (no external frameworks)
- **Networking**: Built-in Java networking

## Class Structure

### COMPUTATION SYSTEM

#### Reusable Code Modules
- **`Vector4D`** - 4D position/direction operations
- **`Chunk4D`** - 4D chunk data structure (blocks, entities)
- **`Block`** - Individual block data (type, metadata)
- **`Entity`** - Base class for player, items, etc.
- **`Player`** - Player state and capabilities
- **`Item`** - Item definitions and behavior
- **`Inventory`** - Item storage and management
- **`WorldGenerator`** - Seed-based terrain generation
- **`PhysicsEngine`** - 4D collision detection, movement
- **`NetworkProtocol`** - Serialization for client-server

#### Dedicated Glue Code
- **`SimulationManager`** - Coordinates all simulation modules
- **`ChunkManager`** - Loads/unloads chunks, manages chunk lifecycle
- **`EntityManager`** - Handles entity updates, interactions
- **`NetworkManager`** - Handles client-server communication

#### Data Classes
- **`GameState`** - Current world state
- **`ChunkData`** - Pure chunk data (no logic)
- **`PlayerData`** - Pure player data
- **`WorldSave`** - Serialization/deserialization

### RENDERING SYSTEM

#### Reusable Code Modules
- **`Texture4D`** - 4D texture loading and slicing
- **`Texture2D`** - 2D texture for items/UI
- **`Camera4D`** - 4D viewing position and slice calculations
- **`BlockRenderer`** - Renders individual blocks from 4D textures
- **`EntityRenderer`** - Renders players and entities
- **`UIRenderer`** - Inventory, HUD, menus
- **`SliceRenderer`** - Renders a single 2D slice
- **`GridRenderer`** - Renders the 7x7 grid of slices

#### Dedicated Glue Code
- **`RenderManager`** - Coordinates all rendering modules
- **`ViewProjector`** - Projects 4D world to 2D view
- **`TextureManager`** - Loads and manages all textures
- **`RenderCache`** - Caches rendered slices for performance

#### View Data Classes
- **`RenderState`** - Current rendering state
- **`SliceView`** - Visual representation of a slice
- **`CameraState`** - Camera position and settings

### MAIN COORDINATION

#### Time Management
- **`GameLoop`** - Main game loop, fixed timestep
- **`InputHandler`** - Processes user Input WS = Direction 1, AD = Direction 2, QE = Direction 3, SPACE = Jumping in Direction 4, downward not needed.
- **`StateManager`** - Manages game states (menu, playing, etc.)

#### System Coordinators
- **`GameEngine`** - Top-level coordinator between systems
- **`ClientManager`** - Client-side coordination
- **`ServerManager`** - Server-side coordination (for multiplayer)

## Outstanding Technical Questions

1. **4D Texture Format**: How to store 8x8x8x8 textures? As a single large 2D image that gets sliced, or as separate files?

2. **Slice Transition Rendering**: For continuous movement between slices, should `ViewProjector` handle interpolating between discrete 4D texture "frames"?

3. **Chunk Size**: What 4D dimensions for chunks? (e.g., 16x16x16x16 blocks?)

4. **Network Sync**: Should the client predict movement locally, or wait for server confirmation?

## Implementation Notes

- The architecture maintains strict separation between simulation and rendering systems
- Each system has its own modules and glue code for maximum modularity
- The design supports the eventual addition of multiplayer functionality
- The 4D texture system is a key technical challenge that will require careful optimization
- Chunk management in 4D space will be crucial for performance with large Worlds


Code the game in Vanilla Java.