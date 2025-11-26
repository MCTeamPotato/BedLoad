package me.kall.bedload.mixin;

import me.kall.bedload.ext.ChunkLoader;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin implements ChunkLoader {
    @Unique private boolean bedLoad$isChunkLoader;
    @Unique private int bedLoad$chunkLoadRadius = -1;

    @Override
    public boolean bedLoad$isChunkLoader() {
        return this.bedLoad$isChunkLoader;
    }

    @Override
    public void bedLoad$setIsChunkLoader(boolean isChunkLoader) {
        this.bedLoad$isChunkLoader = isChunkLoader;
    }

    @Override
    public int bedLoad$getChunkLoadRadius() {
        return this.bedLoad$chunkLoadRadius;
    }

    @Override
    public void bedLoad$setChunkLoadRadius(int radius) {
        this.bedLoad$chunkLoadRadius = radius;
    }

}