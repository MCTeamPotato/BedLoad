package me.kall.bedload.mixin;

import me.kall.bedload.BedLoad;
import me.kall.bedload.api.ChunkLoader;
import me.kall.bedload.config.BedLoadConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow public abstract boolean setChunkForced(int chunkX, int chunkZ, boolean add);

    @Shadow @Nonnull public abstract MinecraftServer getServer();

    @Inject(
            method = "onBlockStateChange",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;immutable()Lnet/minecraft/core/BlockPos;")
    )
    private void onChunkLoaderUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (!this.getServer().isSameThread()) {
            this.getServer().execute(() -> bedLoad$execute(pos, oldState, newState));
            return;
        }
        bedLoad$execute(pos, oldState, newState);
    }

    @Unique
    private void bedLoad$execute(BlockPos pos, BlockState oldState, BlockState newState) {
        final int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        final int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());

        final ChunkLoader oldLoader = (ChunkLoader) oldState.getBlock();
        final ChunkLoader newLoader = (ChunkLoader) newState.getBlock();

        if (oldLoader.bedLoad$isChunkLoader()) bedLoad$handle(chunkX, chunkZ, oldLoader, false);
        if (newLoader.bedLoad$isChunkLoader()) bedLoad$handle(chunkX, chunkZ, newLoader, true);
    }

    @Unique
    private void bedLoad$handle(int centerX, int centerZ, @NotNull ChunkLoader loader, boolean add) {
        final int radius = loader.bedLoad$getChunkLoadRadius();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                this.setChunkForced(centerX + dx, centerZ + dz, add);
            }
        }

        if (BedLoadConfig.SHOW_MESSAGE.get()) BedLoad.note(centerX, centerZ, radius, add);
        BedLoad.LOGGER.info("Chunk [{}, {}] with radius {} updated: {}", centerX, centerZ, radius, add ? "Added" : "Removed");
    }
}