package me.kall.bedload.mixin;

import me.kall.bedload.BedLoad;
import me.kall.bedload.api.ChunkLoader;
import me.kall.bedload.config.BedLoadConfig;
import me.kall.bedload.data.ForceLoadReasons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
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
    @Unique private boolean bedLoad$ignoreWorldGenBlocks;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.bedLoad$ignoreWorldGenBlocks = BedLoadConfig.IGNORE_WORLD_GEN_BLOCKS.get();
    }

    @Shadow public abstract boolean setChunkForced(int chunkX, int chunkZ, boolean add);
    @Shadow @Nonnull public abstract MinecraftServer getServer();

    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    private void onChunkLoaderUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (this.getServer().isSameThread()) {
            bedLoad$execute(pos, oldState, newState);
        } else {
            if (this.bedLoad$ignoreWorldGenBlocks) return;
            this.getServer().execute(() -> bedLoad$execute(pos, oldState, newState));
        }
    }

    @Unique
    private void bedLoad$execute(@NotNull BlockPos pos, @NotNull BlockState oldState, @NotNull BlockState newState) {
        final int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        final int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());

        final ChunkLoader oldLoader = (ChunkLoader) oldState.getBlock();
        final ChunkLoader newLoader = (ChunkLoader) newState.getBlock();

        if (oldLoader.bedLoad$isChunkLoader()) bedLoad$handle(chunkX, chunkZ, pos, oldLoader, false);
        if (newLoader.bedLoad$isChunkLoader()) bedLoad$handle(chunkX, chunkZ, pos, newLoader, true);
    }

    @Unique
    private void bedLoad$handle(int centerX, int centerZ, @NotNull BlockPos pos, @NotNull ChunkLoader loader, boolean add) {
        final int radius = loader.bedLoad$getChunkLoadRadius();
        final ServerLevel level = (ServerLevel)(Object)this;
        final ResourceLocation dim = level.dimension().location();
        final ForceLoadReasons reasons = ForceLoadReasons.get(level);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                long chunkLong = ChunkPos.asLong(centerX + dx, centerZ + dz);
                long blockLong = pos.asLong();

                boolean changed;
                if (add) {
                    changed = reasons.add(dim, chunkLong, blockLong);
                } else {
                    changed = reasons.remove(dim, chunkLong, blockLong);
                }

                if (changed) {
                    boolean stillLoaded = reasons.hasAny(dim, chunkLong);
                    this.setChunkForced(centerX + dx, centerZ + dz, stillLoaded);

                    if (BedLoadConfig.SHOW_MESSAGE.get()) BedLoad.note(centerX + dx, centerZ + dz, radius, stillLoaded);
                    BedLoad.LOGGER.info("Chunk [{}, {}] with radius {} updated: {}", centerX + dx, centerZ + dz, radius, stillLoaded ? "Added" : "Removed");
                }
            }
        }
    }
}
