package me.friedhof.hyperbuilder.computation.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.midi.SysexMessage;

import me.friedhof.hyperbuilder.computation.modules.items.Block;
import me.friedhof.hyperbuilder.save.LazyChunkLoader;
import java.util.Random;
/**
 * Represents the 4D world containing chunks and entities.
 * Manages chunk loading/unloading and entity tracking.
 */
public class World {
    // The name of this world
    private final String name;
    
    // The seed used for world generation
    private final long seed;
    
    // Map of loaded chunks (chunk position -> chunk)
    private final Map<Vector4DInt, Chunk4D> chunks;
    
    /**
     * Map of pending leaves for chunks that don't exist yet.
     * Key: chunk position, Value: list of leaf positions within that chunk
     */
    private final Map<Vector4DInt, java.util.List<Vector4DInt>> pendingLeaves;
    
    // Map of entities (entity ID -> entity)
    private final Map<Integer, Entity> entities;
    
    // The next available entity ID
    private int nextEntityId;
    
    // Lazy chunk loader for loading chunks on-demand
    private LazyChunkLoader chunkLoader;
    
    /**
     * Creates a new world with the specified name and seed.
     * 
     * @param name The world name
     * @param seed The world generation seed
     */
    public World(String name, long seed) {
        this.name = name;
        this.seed = seed;
        this.chunks = new ConcurrentHashMap<>();
        this.pendingLeaves = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();
        this.nextEntityId = 1;
    }
    
    /**
     * Gets the name of this world.
     * 
     * @return The world name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the seed used for world generation.
     * 
     * @return The world seed
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * Sets the lazy chunk loader for this world.
     * 
     * @param chunkLoader The lazy chunk loader to use
     */
    public void setChunkLoader(LazyChunkLoader chunkLoader) {
        this.chunkLoader = chunkLoader;
    }
    
    /**
     * Gets the lazy chunk loader for this world.
     * 
     * @return The lazy chunk loader, or null if not set
     */
    public LazyChunkLoader getChunkLoader() {
        return chunkLoader;
    }
    
    /**
     * Gets the chunk at the specified position.
     * If the chunk is not loaded, it will try to load from disk first, then generate if needed.
     * 
     * @param position The chunk position
     * @return The chunk at the specified position
     */
    public Chunk4D getChunk(Vector4DInt position) {
        // Check if the chunk is already loaded
        Chunk4D chunk = chunks.get(position);
        
        // If not loaded, try to load from disk first
        if (chunk == null && chunkLoader != null) {
            chunk = chunkLoader.loadChunk(position);
            if (chunk != null) {
                chunks.put(position, chunk);
                // Process any pending leaves for this chunk
                processPendingLeaves(chunk, position);
                return chunk;
            }
        }
        
        // If still not found, generate it
        if (chunk == null) {
            chunk = generateChunk(position);
            chunks.put(position, chunk);
        }
        return chunk;
    }
    
    /**
     * Generates a new chunk at the specified position.
     * 
     * @param position The chunk position
     * @return The generated chunk
     */
    private Chunk4D generateChunk(Vector4DInt position) {
        // Create a new chunk
        Chunk4D chunk = new Chunk4D(position);
        
        // Generate realistic terrain with hills and valleys
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        // Convert local coordinates to world coordinates
                        int worldX = x + position.getX() * Chunk4D.CHUNK_SIZE;
                        int worldY = y + position.getY() * Chunk4D.CHUNK_SIZE;
                        int worldZ = z + position.getZ() * Chunk4D.CHUNK_SIZE;
                        int worldW = w + position.getW() * Chunk4D.CHUNK_SIZE;
                        
                        // Generate terrain height using multiple octaves of noise
                        double terrainHeight = generateTerrainHeight(worldX, worldZ, worldW);
                        
                        Block block;
                        if (worldY < terrainHeight - 3) {
                            // Deep underground: stone
                            block = ItemRegistry.createBlock(Material.STONE);
                        } else if (worldY < terrainHeight - 1) {
                            // Shallow underground: dirt
                            block =  ItemRegistry.createBlock(Material.DIRT);
                        } else if (worldY <= terrainHeight) {
                            // Surface: grass
                            block = ItemRegistry.createBlock(Material.GRASS_BLOCK);
                        } else {
                            // Above surface: air
                            block =   ItemRegistry.createBlock(Material.AIR);
                        }
                        
                        chunk.setBlock(x, y, z, w, block);
                    }
                }
            }
        }
        
        // Generate caves in this chunk
        generateCaves(chunk, position);
        
        // Generate coal ore below the surface
        generateCoalOre(chunk, position);
        
        // Generate trees for this chunk (highest priority)
        generateTrees(chunk, position);
        
        // Generate flint on grass blocks (medium priority)
        generateFlint(chunk, position);
        
        // Generate grass vegetation on grass blocks (common, but lower priority than trees and flint)
        generateGrassVegetation(chunk, position);
        
        // Process any pending leaves for this chunk
        processPendingLeaves(chunk, position);
        
        return chunk;
    }
    
    /**
     * Generates terrain height at the given world coordinates using realistic Perlin noise.
     * 
     * @param x World X coordinate
     * @param z World Z coordinate  
     * @param w World W coordinate
     * @return The terrain height at this position
     */
    private double generateTerrainHeight(int x, int z, int w) {
        // Base height around 0
        double height = 0.0;
        
        // Continental scale features (large landmasses)
        height += 20.0 * perlinNoise(x * 0.003, z * 0.003, w * 0.003, seed);
        
        // Mountain ranges and large valleys
        height += 15.0 * perlinNoise(x * 0.008, z * 0.008, w * 0.008, seed + 1000);
        
        // Hills and medium valleys
        height += 8.0 * perlinNoise(x * 0.02, z * 0.02, w * 0.02, seed + 2000);
        
        // Rolling terrain
        height += 4.0 * perlinNoise(x * 0.05, z * 0.05, w * 0.05, seed + 3000);
        
        // Surface detail and small features
        height += 2.0 * perlinNoise(x * 0.12, z * 0.12, w * 0.12, seed + 4000);
        
        // Fine surface texture
        height += 1.0 * perlinNoise(x * 0.3, z * 0.3, w * 0.3, seed + 5000);
        
        return height;
    }
    
    /**
     * Generates trees in the given chunk (optimized version).
     * 
     * @param chunk The chunk to generate trees in
     * @param chunkPosition The position of the chunk in the world
     */
    private void generateTrees(Chunk4D chunk, Vector4DInt chunkPosition) {
        // Optimized tree generation parameters
        int treesPerChunk = 2; // Fixed number of trees per chunk for predictable performance
        int minTreeHeight = 3;
        int maxTreeHeight = 5;
        
        // Generate a fixed number of trees at pseudo-random locations
        for (int treeIndex = 0; treeIndex < treesPerChunk; treeIndex++) {
            // Use chunk position and tree index to generate deterministic but varied positions
            long treeSeed = seed + chunkPosition.getX() * 1000 + chunkPosition.getZ() * 100 + chunkPosition.getW() * 10 + treeIndex;
            
            // Generate tree position within chunk using simple hash-based randomization
            int x = Math.abs((int)(treeSeed * 31)) % Chunk4D.CHUNK_SIZE;
            int z = Math.abs((int)(treeSeed * 37)) % Chunk4D.CHUNK_SIZE;
            int w = Math.abs((int)(treeSeed * 41)) % Chunk4D.CHUNK_SIZE;
            
            // Find surface level
            int surfaceY = findSurfaceLevel(chunk, x, z, w);
            if (surfaceY == -1 || surfaceY >= Chunk4D.CHUNK_SIZE - maxTreeHeight) continue;
            
            // Check if surface block is grass (suitable for tree growth)
            Block surfaceBlock = chunk.getBlock(x, surfaceY, z, w);
            if (surfaceBlock == null || !Material.GRASS_BLOCK.equals(surfaceBlock.getBlockId())) continue;
            
            // Generate tree height using simple deterministic calculation
            int treeHeight = minTreeHeight + (Math.abs((int)(treeSeed * 43)) % (maxTreeHeight - minTreeHeight + 1));
            
            // Generate the tree
             generateSimpleTree(chunk, chunkPosition, x, surfaceY + 1, z, w, treeHeight);
        }
    }
    
    /**
     * Generates flint on top of grass blocks in the given chunk.
     * 
     * @param chunk The chunk to generate flint in
     * @param chunkPosition The position of the chunk in the world
     */
    private void generateFlint(Chunk4D chunk, Vector4DInt chunkPosition) {
        // Flint generation parameters - moderately common
        int maxFlintPerChunk = 5; // Maximum flint pieces per chunk
        double flintSpawnChance = 0.25; // 25% chance per potential location
        
        // Generate flint at pseudo-random locations
        for (int attempt = 0; attempt < maxFlintPerChunk * 2; attempt++) {
            // Use chunk position and attempt to generate deterministic but varied positions
            long flintSeed = seed + chunkPosition.getX() * 2000 + chunkPosition.getZ() * 200 + chunkPosition.getW() * 20 + attempt;
            
            // Check spawn chance first to avoid unnecessary calculations
            if ((Math.abs((int)(flintSeed * 17)) % 100) >= (flintSpawnChance * 100)) {
                continue;
            }
            
            // Generate flint position within chunk using simple hash-based randomization
            int x = Math.abs((int)(flintSeed * 23)) % Chunk4D.CHUNK_SIZE;
            int z = Math.abs((int)(flintSeed * 29)) % Chunk4D.CHUNK_SIZE;
            int w = Math.abs((int)(flintSeed * 31)) % Chunk4D.CHUNK_SIZE;
            
            // Find surface level
            int surfaceY = findSurfaceLevel(chunk, x, z, w);
            if (surfaceY == -1 || surfaceY >= Chunk4D.CHUNK_SIZE - 1) continue;
            
            // Check if surface block is grass (suitable for flint spawning)
            Block surfaceBlock = chunk.getBlock(x, surfaceY, z, w);
            if (surfaceBlock == null || !Material.GRASS_BLOCK.equals(surfaceBlock.getBlockId())) continue;
            
            // Check if the block above the grass is air (where flint will be placed)
            Block aboveBlock = chunk.getBlock(x, surfaceY + 1, z, w);
            if (aboveBlock == null || !Material.AIR.equals(aboveBlock.getBlockId())) continue;
            
            // Place flint on top of the grass block
            chunk.setBlock(x, surfaceY + 1, z, w,  ItemRegistry.createBlock(Material.FLINT));
        }
    }
    
    /**
     * Generates grass vegetation on top of grass blocks in the given chunk.
     * Grass vegetation is quite common but has lower priority than trees and flint.
     * 
     * @param chunk The chunk to generate grass vegetation in
     * @param chunkPosition The position of the chunk in the world
     */
    private void generateGrassVegetation(Chunk4D chunk, Vector4DInt chunkPosition) {
        // Grass vegetation generation parameters - quite common
        int maxGrassPerChunk = 350; // Maximum grass pieces per chunk
        double grassSpawnChance = 0.6; // % chance per potential location
        
        
        
        // Generate grass at pseudo-random locations using proper random distribution
        Random random = new java.util.Random(seed + chunkPosition.getX() * 7919L + chunkPosition.getZ() * 4801L + chunkPosition.getW() * 2909L);
        
        for (int attempt = 0; attempt < maxGrassPerChunk * 2; attempt++) {
            // Check spawn chance first to avoid unnecessary calculations
            if (random.nextDouble() >= grassSpawnChance) {
                continue;
            }
            
            // Generate grass position within chunk using proper random distribution
            int x = random.nextInt(Chunk4D.CHUNK_SIZE);
            int z = random.nextInt(Chunk4D.CHUNK_SIZE);
            int w = random.nextInt(Chunk4D.CHUNK_SIZE);
            
            // Find surface level
            int surfaceY = findSurfaceLevel(chunk, x, z, w);
            if (surfaceY == -1 || surfaceY >= Chunk4D.CHUNK_SIZE - 1) continue;
            
            // Check if surface block is grass block (suitable for grass vegetation growth)
            Block surfaceBlock = chunk.getBlock(x, surfaceY, z, w);
            if (surfaceBlock == null || !Material.GRASS_BLOCK.equals(surfaceBlock.getBlockId())) continue;
            
            
            // Check if the block above the grass block is air (where grass vegetation will be placed)
            // This also ensures trees and flint have priority over grass vegetation
            Block aboveBlock = chunk.getBlock(x, surfaceY + 1, z, w);
            if (aboveBlock == null || !Material.AIR.equals(aboveBlock.getBlockId())) continue;
            
            
            // Place grass vegetation on top of the grass block
            chunk.setBlock(x, surfaceY + 1, z, w, ItemRegistry.createBlock(Material.GRASS));
            
        }
        
    }
    
    /**
     * Generates coal ore below the surface in the given chunk.
     * Coal ore spawns in stone blocks at depths between 5 and 40 blocks below the surface.
     * 
     * @param chunk The chunk to generate coal ore in
     * @param chunkPosition The position of the chunk in the world
     */
    private void generateCoalOre(Chunk4D chunk, Vector4DInt chunkPosition) {
        // Coal ore generation parameters
        double coalOreChance = 0.005; 
        int minDepthBelowSurface = 3;  // Minimum depth below surface for coal ore (reduced for easier access)
        int maxDepthBelowSurface = 50; // Maximum depth below surface for coal ore (increased range)
        
        // Generate coal ore at valid underground locations
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        // Convert to world coordinates
                        int worldX = x + chunkPosition.getX() * Chunk4D.CHUNK_SIZE;
                        int worldY = y + chunkPosition.getY() * Chunk4D.CHUNK_SIZE;
                        int worldZ = z + chunkPosition.getZ() * Chunk4D.CHUNK_SIZE;
                        int worldW = w + chunkPosition.getW() * Chunk4D.CHUNK_SIZE;
                        
                        // Calculate terrain height at this position
                        double terrainHeight = generateTerrainHeight(worldX, worldZ, worldW);
                        
                        // Check if this position is at the right depth for coal ore
                        double depthBelowSurface = terrainHeight - worldY;
                        if (depthBelowSurface < minDepthBelowSurface || depthBelowSurface > maxDepthBelowSurface) {
                            continue;
                        }
                        
                        // Only replace stone blocks with coal ore
                        Block currentBlock = chunk.getBlock(x, y, z, w);
                        if (currentBlock == null || !Material.STONE.equals(currentBlock.getBlockId())) {
                            continue;
                        }
                        
                        // Use deterministic pseudo-random generation based on world coordinates
                        long coalSeed = seed + worldX * 73L + worldY * 37L + worldZ * 19L + worldW * 11L;
                        // Use a better pseudo-random algorithm for more even distribution
                        coalSeed = coalSeed * 1103515245L + 12345L; // Linear congruential generator
                        double randomValue = Math.abs(coalSeed % 1000000) / 1000000.0;
                        
                        // Check if coal ore should spawn at this location
                        if (randomValue < coalOreChance) {
                            chunk.setBlock(x, y, z, w, ItemRegistry.createBlock(Material.COAL_ORE));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Finds the surface level (highest non-air block) at the given local coordinates.
     * 
     * @param chunk The chunk to search in
     * @param x Local x coordinate
     * @param z Local z coordinate
     * @param w Local w coordinate
     * @return The Y coordinate of the surface, or -1 if no surface found
     */
    private int findSurfaceLevel(Chunk4D chunk, int x, int z, int w) {
        for (int y = Chunk4D.CHUNK_SIZE - 1; y >= 0; y--) {
            Block block = chunk.getBlock(x, y, z, w);
            if (block != null && !block.getBlockId().equals(Material.AIR)) {
                return y;
            }
        }
        return -1;
    }
    
    /**
     * Generates a simple, efficient 4D tree at the specified location.
     * 
     * @param chunk The chunk to place the tree in
     * @param chunkPosition The position of the chunk in the world
     * @param x Local x coordinate for tree base
     * @param y Local y coordinate for tree base
     * @param z Local z coordinate for tree base
     * @param w Local w coordinate for tree base
     * @param height Height of the tree trunk
     */
    private void generateSimpleTree(Chunk4D chunk, Vector4DInt chunkPosition, int x, int y, int z, int w, int height) {
        // Generate trunk
        for (int i = 0; i < height; i++) {
            if (y + i < Chunk4D.CHUNK_SIZE) {
                chunk.setBlock(x, y + i, z, w, ItemRegistry.createBlock(Material.WOOD_LOG));
            }
        }
        
        // Generate leaves in a 4D cross pattern around the top of the tree
        int leafY = y + height - 1;
        int leafRadius = 2;
        
        // Convert tree position to world coordinates
        int worldX = x + chunkPosition.getX() * Chunk4D.CHUNK_SIZE;
        int worldY = leafY + chunkPosition.getY() * Chunk4D.CHUNK_SIZE;
        int worldZ = z + chunkPosition.getZ() * Chunk4D.CHUNK_SIZE;
        int worldW = w + chunkPosition.getW() * Chunk4D.CHUNK_SIZE;
        
        // Generate leaves in 4D cross pattern
        for (int dx = -leafRadius; dx <= leafRadius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                    for (int dw = -leafRadius; dw <= leafRadius; dw++) {
                        // 4D cross pattern: only place leaves if at most 2 coordinates are non-zero
                        int nonZeroCount = 0;
                        if (dx != 0) nonZeroCount++;
                        if (dy != 0) nonZeroCount++;
                        if (dz != 0) nonZeroCount++;
                        if (dw != 0) nonZeroCount++;
                        
                        if (nonZeroCount <= 2) {
                            int leafWorldX = worldX + dx;
                            int leafWorldY = worldY + dy;
                            int leafWorldZ = worldZ + dz;
                            int leafWorldW = worldW + dw;
                            
                            // Try to place the leaf
                            placeLeafAtWorldPosition(leafWorldX, leafWorldY, leafWorldZ, leafWorldW);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Places a leaf at the specified world position, handling cross-chunk placement.
     */
    private void placeLeafAtWorldPosition(int worldX, int worldY, int worldZ, int worldW) {
        // Calculate which chunk this position belongs to
        Vector4DInt targetChunkPos = new Vector4DInt(
            Math.floorDiv(worldX, Chunk4D.CHUNK_SIZE),
            Math.floorDiv(worldY, Chunk4D.CHUNK_SIZE),
            Math.floorDiv(worldZ, Chunk4D.CHUNK_SIZE),
            Math.floorDiv(worldW, Chunk4D.CHUNK_SIZE)
        );
        
        // Calculate local coordinates within the target chunk
        int localX = worldX - targetChunkPos.getX() * Chunk4D.CHUNK_SIZE;
        int localY = worldY - targetChunkPos.getY() * Chunk4D.CHUNK_SIZE;
        int localZ = worldZ - targetChunkPos.getZ() * Chunk4D.CHUNK_SIZE;
        int localW = worldW - targetChunkPos.getW() * Chunk4D.CHUNK_SIZE;
        
        // Check if the target chunk exists
        Chunk4D targetChunk = chunks.get(targetChunkPos);
        if (targetChunk != null) {
            // Chunk exists, place the leaf if the position is air
            Block existingBlock = targetChunk.getBlock(localX, localY, localZ, localW);
            if (existingBlock != null && existingBlock.equals(ItemRegistry.createBlock(Material.AIR))) {
                targetChunk.setBlock(localX, localY, localZ, localW, ItemRegistry.createBlock(Material.LEAVES));
            }
        } else {
            // Chunk doesn't exist, add to pending leaves
            Vector4DInt localPos = new Vector4DInt(localX, localY, localZ, localW);
            pendingLeaves.computeIfAbsent(targetChunkPos, k -> new ArrayList<>()).add(localPos);
        }
    }
    
    /**
     * Processes pending leaves for a newly generated chunk.
     */
    private void processPendingLeaves(Chunk4D chunk, Vector4DInt chunkPosition) {
        List<Vector4DInt> pendingForThisChunk = pendingLeaves.remove(chunkPosition);
        if (pendingForThisChunk != null) {
            for (Vector4DInt leafPos : pendingForThisChunk) {
                // Only place leaf if the position is air
                Block existingBlock = chunk.getBlock(leafPos.getX(), leafPos.getY(), leafPos.getZ(), leafPos.getW());
                if (existingBlock != null && existingBlock.equals(ItemRegistry.createBlock(Material.AIR))) {
                    chunk.setBlock(leafPos.getX(), leafPos.getY(), leafPos.getZ(), leafPos.getW(), ItemRegistry.createBlock(Material.LEAVES));
                }
            }
        }
    }
    
    /**
     * Generates caves in the given chunk using 3D Perlin noise and cellular automata.
     * Creates realistic cave systems with varying sizes and complexity.
     * 
     * @param chunk The chunk to generate caves in
     * @param chunkPosition The position of the chunk in the world
     */
    private void generateCaves(Chunk4D chunk, Vector4DInt chunkPosition) {
        // Cave generation parameters - 4D-optimized for balanced distribution
        double caveThreshold = 0.45; // Moderate threshold optimized for 4D noise complexity
        int minCaveDepth = 5;         // Minimum depth for cave generation
        int maxCaveDepth = 60;        // Maximum depth for cave generation
        
        // Generate sparse cave pockets using single noise layer
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        // Convert to world coordinates
                        int worldX = x + chunkPosition.getX() * Chunk4D.CHUNK_SIZE;
                        int worldY = y + chunkPosition.getY() * Chunk4D.CHUNK_SIZE;
                        int worldZ = z + chunkPosition.getZ() * Chunk4D.CHUNK_SIZE;
                        int worldW = w + chunkPosition.getW() * Chunk4D.CHUNK_SIZE;
                        
                        // Only generate caves underground
                        double terrainHeight = generateTerrainHeight(worldX, worldZ, worldW);
                        if (worldY > terrainHeight - minCaveDepth || worldY < terrainHeight - maxCaveDepth) {
                            continue;
                        }
                        
                        // Generate cave noise value
                        double caveNoise = generateCaveNoise(worldX, worldY, worldZ, worldW);
                        
                        // Check if this position should be a cave
                        if (caveNoise > caveThreshold) {
                            Block currentBlock = chunk.getBlock(x, y, z, w);
                            if (currentBlock != null && !currentBlock.getBlockId().equals(Material.AIR)) {
                                // Create cave by setting block to air
                                chunk.setBlock(x, y, z, w, ItemRegistry.createBlock(Material.AIR));
                            }
                        }
                    }
                }
            }
        }
        
        // Apply minimal smoothing to preserve small cave pockets
         smoothCaves(chunk, chunkPosition);
    }
    
    /**
     * Generates 4D-optimized cave noise for balanced cave distribution.
     * Uses multiple noise layers scaled appropriately for 4D space complexity.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param w World W coordinate
     * @return Combined noise value for cave generation
     */
    private double generateCaveNoise(int x, int y, int z, int w) {
        // Primary cave structure - medium frequency for main cave networks
        double primaryCaves = Math.abs(perlinNoise(x * 0.08, y * 0.08, z * 0.08, seed + w * 1000));
        
        // Secondary cave details - higher frequency for cave variation
        double secondaryCaves = Math.abs(perlinNoise(x * 0.15, y * 0.15, z * 0.15, seed + w * 1500)) * 0.6;
        
        // 4D connectivity layer - lower frequency to ensure caves connect across 4D
        double connectivity4D = Math.abs(perlinNoise(x * 0.04, y * 0.04, z * 0.04, seed + w * 500)) * 0.8;
        
        // Depth-based variation - caves become rarer at extreme depths
        double depthFactor = Math.sin(y * 0.05) * 0.3;
        
        // Combine all layers with 4D-appropriate weighting
        double combinedNoise = primaryCaves + secondaryCaves * 0.4 + connectivity4D * 0.3 + depthFactor;
        
        // 4D-optimized noise cap - higher than 3D but controlled
        return Math.min(Math.abs(combinedNoise), 0.85);
    }
    
    /**
     * Applies 4D-optimized smoothing to create natural cave structures.
     * Balances cave connectivity with structural integrity in 4D space.
     * 
     * @param chunk The chunk to smooth
     * @param chunkPosition The position of the chunk in the world
     */
    private void smoothCaves(Chunk4D chunk, Vector4DInt chunkPosition) {
        // Create a copy of the chunk to work with
        Block[][][][] originalBlocks = new Block[Chunk4D.CHUNK_SIZE][Chunk4D.CHUNK_SIZE][Chunk4D.CHUNK_SIZE][Chunk4D.CHUNK_SIZE];
        
        // Copy current state
        for (int x = 0; x < Chunk4D.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk4D.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk4D.CHUNK_SIZE; z++) {
                    for (int w = 0; w < Chunk4D.CHUNK_SIZE; w++) {
                        originalBlocks[x][y][z][w] = chunk.getBlock(x, y, z, w);
                    }
                }
            }
        }
        
        // Apply 4D-appropriate smoothing for natural cave formation
        for (int x = 1; x < Chunk4D.CHUNK_SIZE - 1; x++) {
            for (int y = 1; y < Chunk4D.CHUNK_SIZE - 1; y++) {
                for (int z = 1; z < Chunk4D.CHUNK_SIZE - 1; z++) {
                    for (int w = 1; w < Chunk4D.CHUNK_SIZE - 1; w++) {
                        // Count solid neighbors in 3D space (ignoring W dimension for this check)
                        int solidNeighbors = countSolidNeighbors(originalBlocks, x, y, z, w);
                        
                        // Apply 4D-appropriate rules for cave connectivity
                        Block currentBlock = originalBlocks[x][y][z][w];
                        if (currentBlock != null && currentBlock.getBlockId().equals(Material.AIR)) {
                            // Fill isolated air pockets (5+ solid neighbors in 4D)
                            if (solidNeighbors >= 5) {
                                chunk.setBlock(x, y, z, w, ItemRegistry.createBlock(Material.STONE));
                            }
                        } else if (currentBlock != null && !currentBlock.getBlockId().equals(Material.AIR)) {
                            // Create air in very isolated solid blocks (0-1 solid neighbors)
                            if (solidNeighbors <= 1) {
                                chunk.setBlock(x, y, z, w, ItemRegistry.createBlock(Material.AIR));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Counts solid neighbors around a position for cellular automata smoothing.
     * 
     * @param blocks The block array to check
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param w W coordinate
     * @return Number of solid neighbors
     */
    private int countSolidNeighbors(Block[][][][] blocks, int x, int y, int z, int w) {
        int count = 0;
        
        // Check 6 adjacent neighbors (3D adjacency)
        int[][] directions = {
            {-1, 0, 0}, {1, 0, 0},  // X axis
            {0, -1, 0}, {0, 1, 0},  // Y axis
            {0, 0, -1}, {0, 0, 1}   // Z axis
        };
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            int nz = z + dir[2];
            
            // Check bounds
            if (nx >= 0 && nx < Chunk4D.CHUNK_SIZE && 
                ny >= 0 && ny < Chunk4D.CHUNK_SIZE && 
                nz >= 0 && nz < Chunk4D.CHUNK_SIZE) {
                
                Block neighbor = blocks[nx][ny][nz][w];
                if (neighbor != null && !neighbor.getBlockId().equals(Material.AIR)) {
                    count++;
                }
            } else {
                // Treat out-of-bounds as solid for edge stability
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Gets the number of pending leaf chunks for debugging.
     */
    public int getPendingLeavesChunkCount() {
        return pendingLeaves.size();
    }
    
    /**
     * Gets the total number of pending leaves for debugging.
     */
    public int getTotalPendingLeavesCount() {
        return pendingLeaves.values().stream().mapToInt(List::size).sum();
    }
    
    /**
     * 4D Perlin noise implementation for realistic terrain generation.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param seed Random seed
     * @return Noise value between -1 and 1
     */
    private double perlinNoise(double x, double y, double z, long seed) {
        // Get integer coordinates
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        int zi = (int) Math.floor(z);
        
        // Get fractional coordinates
        double xf = x - xi;
        double yf = y - yi;
        double zf = z - zi;
        
        // Smooth the fractional coordinates using fade function
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);
        
        // Hash coordinates of the 8 cube corners
        int aaa = hash(xi, yi, zi, seed);
        int aba = hash(xi, yi + 1, zi, seed);
        int aab = hash(xi, yi, zi + 1, seed);
        int abb = hash(xi, yi + 1, zi + 1, seed);
        int baa = hash(xi + 1, yi, zi, seed);
        int bba = hash(xi + 1, yi + 1, zi, seed);
        int bab = hash(xi + 1, yi, zi + 1, seed);
        int bbb = hash(xi + 1, yi + 1, zi + 1, seed);
        
        // Calculate gradients at each corner
        double x1 = lerp(grad(aaa, xf, yf, zf), grad(baa, xf - 1, yf, zf), u);
        double x2 = lerp(grad(aba, xf, yf - 1, zf), grad(bba, xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);
        
        x1 = lerp(grad(aab, xf, yf, zf - 1), grad(bab, xf - 1, yf, zf - 1), u);
        x2 = lerp(grad(abb, xf, yf - 1, zf - 1), grad(bbb, xf - 1, yf - 1, zf - 1), u);
        double y2 = lerp(x1, x2, v);
        
        return lerp(y1, y2, w);
    }
    
    /**
     * Fade function for smooth interpolation (6t^5 - 15t^4 + 10t^3).
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    /**
     * Linear interpolation between two values.
     */
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    
    /**
     * Hash function for generating pseudo-random values.
     */
    private int hash(int x, int y, int z, long seed) {
        long hash = seed;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return (int) (hash & 255);
    }
    
    /**
     * Gradient function for Perlin noise.
     */
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    /**
     * Unloads the chunk at the specified position.
     * 
     * @param position The chunk position
     * @return true if the chunk was unloaded, false if it wasn't loaded
     */
    public boolean unloadChunk(Vector4DInt position) {
        return chunks.remove(position) != null;
    }
    
    /**
     * Gets the block at the specified position.
     * 
     * @param position The block position
     * @return The block at the specified position, or null if the chunk is not loaded
     */
    public Block getBlock(Vector4DInt position) {
        // Calculate the chunk position
        Vector4DInt chunkPos = new Vector4DInt(
            Math.floorDiv(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Calculate the local position within the chunk
        Vector4DInt localPos = new Vector4DInt(
            Math.floorMod(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Get the chunk
        Chunk4D chunk = getChunk(chunkPos);
        
        // Check if chunk exists
        if (chunk == null) {
            return null; // Return null for non-existent chunks
        }
        
        // Get the block
        return chunk.getBlock(localPos);
    }
    
    /**
     * Sets the block at the specified position.
     * 
     * @param position The block position
     * @param block The block to set
     * @return true if the block was set, false otherwise
     */
    public boolean setBlock(Vector4DInt position, Block block) {
        // Calculate the chunk position
        Vector4DInt chunkPos = new Vector4DInt(
            Math.floorDiv(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Calculate the local position within the chunk
        Vector4DInt localPos = new Vector4DInt(
            Math.floorMod(position.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorMod(position.getW(), Chunk4D.CHUNK_SIZE)
        );
        
        // Get the chunk
        Chunk4D chunk = getChunk(chunkPos);
        
        // Check if chunk exists
        if (chunk == null) {
            return false; // Cannot set block in non-existent chunk
        }
        
        // Set the block
        return chunk.setBlock(localPos, block);
    }
    
    /**
     * Adds an entity to the world.
     * 
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        entities.put(entity.getId(), entity);
        nextEntityId++;
    }
    
    /**
     * Removes an entity from the world.
     * 
     * @param entityId The ID of the entity to remove
     * @return The removed entity, or null if not found
     */
    public Entity removeEntity(int entityId) {
        return entities.remove(entityId);
    }
    
    /**
     * Gets an entity by its ID.
     * 
     * @param entityId The ID of the entity to get
     * @return The entity, or null if not found
     */
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    /**
     * Gets all entities in the world.
     * 
     * @return A map of entity IDs to entities
     */
    public Map<Integer, Entity> getEntities() {
        return new HashMap<>(entities);
    }
    
    /**
     * Gets all entities in the world as a list.
     * 
     * @return A list of all entities
     */
    public java.util.List<Entity> getEntitiesList() {
        return new java.util.ArrayList<>(entities.values());
    }
    
    /**
     * Gets the next available entity ID.
     * 
     * @return The next entity ID
     */
    public int getNextEntityId() {
        return nextEntityId++;
    }
    
    /**
     * Creates a new player in the world.
     * 
     * @param username The player's username
     * @param position The initial position of the player
     * @return The created player
     */
    public Player createPlayer(String username, Vector4D position) {
        int id = nextEntityId;
        Player player = new Player(id, position, username);
        addEntity(player);
        return player;
    }
    
    /**
     * Finds a safe spawn position by starting at Y=100 and moving down until solid ground is found.
     * 
     * @param x The X coordinate for the spawn position
     * @param z The Z coordinate for the spawn position
     * @param w The W coordinate for the spawn position
     * @return A safe spawn position with the player positioned just above solid ground
     */
    public Vector4D findSafeSpawnPosition(double x, double z, double w) {
        int startY = 100;
        int minY = -50; // Don't search below this level to avoid infinite loops
        
        System.out.println("Searching for safe spawn position at X=" + x + ", Z=" + z + ", W=" + w);
        
        // Start from Y=100 and move down
        for (int y = startY; y >= minY; y--) {
            Vector4DInt blockPos = new Vector4DInt((int)Math.floor(x), y, (int)Math.floor(z), (int)Math.floor(w));
            Block block = getBlock(blockPos);
            
            // Check if this block is solid (not air)
            if (block != null && !block.getBlockId().equals(Material.AIR)) {
                // Found solid ground, spawn player 1.25 blocks above it
                // (Player center at Y+1.25 means feet at Y+1, just above the solid block at Y)
                double spawnY = y + 1.25;
                Vector4D spawnPos = new Vector4D(x, spawnY, z, w);
                System.out.println("Safe spawn position found: (" + x + ", " + spawnY + ", " + z + ", " + w + ") above block at Y=" + y);
                return spawnPos;
            }
        }
        
        // If no solid ground found, spawn at a default safe position
        System.out.println("No solid ground found, using default spawn position");
        return new Vector4D(x, 10, z, w); // Default to Y=10 if no ground found
    }
    
    /**
     * Updates all entities in the world.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    public void update(double deltaTime) {
        // Update all entities
        for (Entity entity : entities.values()) {
            entity.update(deltaTime, this);
        }
        
        // Clean up despawned dropped items
        cleanupDespawnedItems();
    }
    
    /**
     * Removes all dropped items that are marked for despawn.
     */
    private void cleanupDespawnedItems() {
        java.util.Iterator<java.util.Map.Entry<Integer, Entity>> iterator = entities.entrySet().iterator();
        
        while (iterator.hasNext()) {
            java.util.Map.Entry<Integer, Entity> entry = iterator.next();
            Entity entity = entry.getValue();
            
            if (entity instanceof DroppedItem) {
                DroppedItem droppedItem = (DroppedItem) entity;
                if (droppedItem.shouldDespawn()) {
                    iterator.remove();
                    
                    // Also remove from chunk
                    Vector4DInt chunkPos = getChunkPosition(entity.getPosition());
                    Chunk4D chunk = getChunk(chunkPos);
                    if (chunk != null) {
                        chunk.removeEntity(entity.getId());
                    }
                }
            }
        }
    }
    
    /**
     * Calculates the chunk position for a given world position.
     * 
     * @param worldPos The world position
     * @return The chunk position
     */
    public Vector4DInt getChunkPosition(Vector4D worldPos) {
        return new Vector4DInt(
            Math.floorDiv((int)worldPos.getX(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv((int)worldPos.getY(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv((int)worldPos.getZ(), Chunk4D.CHUNK_SIZE),
            Math.floorDiv((int)worldPos.getW(), Chunk4D.CHUNK_SIZE)
        );
    }

    /**
     * Gets all loaded chunks in the world.
     * 
     * @return A map of chunk positions to chunks
     */
    public Map<Vector4DInt, Chunk4D> getLoadedChunks() {
        return new HashMap<>(chunks);
    }
    
    /**
     * Sets a chunk at the specified position.
     * Used for loading chunks from save data.
     * Also registers all entities from the chunk into the world's entity map.
     * 
     * @param position The chunk position
     * @param chunk The chunk to set
     */
    public void setChunk(Vector4DInt position, Chunk4D chunk) {
        chunks.put(position, chunk);
        
        // Register all entities from the chunk into the world's entity map
        for (Entity entity : chunk.getEntities().values()) {
            entities.put(entity.getId(), entity);
            
            // Update nextEntityId to avoid conflicts
            if (entity.getId() >= nextEntityId) {
                nextEntityId = entity.getId() + 1;
            }
        }
    }
    
    /**
     * Syncs all entities from the world entity map to their appropriate chunk entity maps.
     * This ensures that entities are properly saved with their chunks.
     * Should be called before saving the world.
     */
    public void syncEntitiesToChunks() {
        // Clear all non-player entities from chunks first to avoid duplicates
        for (Chunk4D chunk : chunks.values()) {
            // Remove non-player entities from chunk
            java.util.Iterator<java.util.Map.Entry<Integer, Entity>> iterator = 
                chunk.getEntities().entrySet().iterator();
            while (iterator.hasNext()) {
                java.util.Map.Entry<Integer, Entity> entry = iterator.next();
                if (!(entry.getValue() instanceof Player)) {
                    iterator.remove();
                }
            }
        }
        
        // Add all world entities to their appropriate chunks
        for (Entity entity : entities.values()) {
            // Skip players as they are saved separately
            if (entity instanceof Player) {
                continue;
            }
            
            Vector4DInt chunkPos = getChunkPosition(entity.getPosition());
            Chunk4D chunk = getChunk(chunkPos);
            
            if (chunk != null) {
                chunk.addEntity(entity);
            }
        }
    }
}