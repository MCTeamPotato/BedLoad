package me.kall.bedload.mixin;

import me.kall.bedload.api.ChunkLoader;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements ChunkLoader {
    @Unique private boolean bedLoad$isChunkLoader;

    @Override
    public boolean bedLoad$isChunkLoader() {
        return this.bedLoad$isChunkLoader;
    }

    @Override
    public void bedLoad$setIsChunkLoader(boolean isChunkLoader) {
        this.bedLoad$isChunkLoader = isChunkLoader;
    }
}
