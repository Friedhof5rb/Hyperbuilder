package me.friedhof.hyperbuilder.save;

import me.friedhof.hyperbuilder.computation.modules.Chunk4D;
import me.friedhof.hyperbuilder.computation.modules.Vector4DInt;

import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;

/**
 * Handles lazy loading of chunks from disk.
 * Chunks are loaded on-demand when requested by the world.
 */
public class LazyChunkLoader {
    private final Path chunksDirectory;
    private final ConcurrentMap<Vector4DInt, Boolean> chunkExistsCache;
    
    /**
     * Creates a new LazyChunkLoader for the specified chunks directory.
     * 
     * @param chunksDirectory The directory containing chunk save files
     */
    public LazyChunkLoader(Path chunksDirectory) {
        this.chunksDirectory = chunksDirectory;
        this.chunkExistsCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Loads a chunk from disk if it exists.
     * 
     * @param chunkPos The position of the chunk to load
     * @return The loaded chunk, or null if it doesn't exist on disk
     */
    public Chunk4D loadChunk(Vector4DInt chunkPos) {
        // Check cache first to avoid file system calls
        Boolean exists = chunkExistsCache.get(chunkPos);
        if (exists != null && !exists) {
            return null; // We know this chunk doesn't exist
        }
        
        String chunkFileName = String.format("chunk_%d_%d_%d_%d.dat", 
            chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunkPos.getW());
        Path chunkFile = chunksDirectory.resolve(chunkFileName);
        
        if (!Files.exists(chunkFile)) {
            // Cache the fact that this chunk doesn't exist
            chunkExistsCache.put(chunkPos, false);
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(Files.newInputStream(chunkFile)))) {
            ChunkSaveData saveData = (ChunkSaveData) ois.readObject();
            Chunk4D chunk = saveData.toChunk();
            
            // Cache that this chunk exists
            chunkExistsCache.put(chunkPos, true);
            
            System.out.println("Lazy loaded chunk at " + chunkPos);
            return chunk;
            
        } catch (Exception e) {
            System.err.println("Failed to lazy load chunk from " + chunkFile + ": " + e.getMessage());
            // Cache that this chunk is problematic
            chunkExistsCache.put(chunkPos, false);
            return null;
        }
    }
    
    /**
     * Checks if a chunk exists on disk without loading it.
     * 
     * @param chunkPos The position of the chunk to check
     * @return true if the chunk exists on disk, false otherwise
     */
    public boolean chunkExists(Vector4DInt chunkPos) {
        // Check cache first
        Boolean exists = chunkExistsCache.get(chunkPos);
        if (exists != null) {
            return exists;
        }
        
        String chunkFileName = String.format("chunk_%d_%d_%d_%d.dat", 
            chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunkPos.getW());
        Path chunkFile = chunksDirectory.resolve(chunkFileName);
        
        boolean fileExists = Files.exists(chunkFile);
        chunkExistsCache.put(chunkPos, fileExists);
        
        return fileExists;
    }
    
    /**
     * Clears the chunk existence cache.
     * Should be called when chunks are saved to ensure cache consistency.
     */
    public void clearCache() {
        chunkExistsCache.clear();
    }
    
    /**
     * Gets the chunks directory path.
     * 
     * @return The path to the chunks directory
     */
    public Path getChunksDirectory() {
        return chunksDirectory;
    }
}