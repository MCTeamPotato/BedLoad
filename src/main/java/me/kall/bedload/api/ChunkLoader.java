package me.kall.bedload.api;

public interface ChunkLoader {
    boolean bedLoad$isChunkLoader();
    void bedLoad$setIsChunkLoader(boolean isChunkLoader);

    int bedLoad$getChunkLoadRadius();
    void bedLoad$setChunkLoadRadius(int radius);
}
