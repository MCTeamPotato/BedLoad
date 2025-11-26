package me.kall.bedload.ext;

public interface ChunkLoader {
    boolean bedLoad$isChunkLoader();
    void bedLoad$setIsChunkLoader(boolean isChunkLoader);

    int bedLoad$getChunkLoadRadius();
    void bedLoad$setChunkLoadRadius(int radius);
}
