package me.kall.bedload.mixin;

import me.kall.bedload.api.ChunkLoader;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements ChunkLoader {
    @Unique private boolean bedLoad$isChunkLoader;
    @Unique private int bedLoad$chunkLoadRadius;

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
