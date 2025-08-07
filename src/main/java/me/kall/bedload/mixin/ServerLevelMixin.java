package me.kall.bedload.mixin;

import me.kall.bedload.BedLoad;
import me.kall.bedload.api.ChunkLoader;
import me.kall.bedload.config.BedLoadConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow public abstract boolean setChunkForced(int chunkX, int chunkZ, boolean add);

    @Inject(method = "onBlockStateChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;immutable()Lnet/minecraft/core/BlockPos;"))
    private void onChunkLoaderUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());

        ChunkLoader oldLoader = (ChunkLoader) oldState.getBlock();
        ChunkLoader newLoader = (ChunkLoader) newState.getBlock();

        if (oldLoader.bedLoad$isChunkLoader()) {
            int radius = oldLoader.bedLoad$getChunkLoadRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    this.setChunkForced(chunkX + dx, chunkZ + dz, false);
                }
            }
            if (BedLoadConfig.SHOW_MESSAGE.get()) BedLoad.players().ifPresent(players -> players.forEach(player -> player.displayClientMessage(Component.translatable("info.bedload.remove", chunkX, chunkZ, radius), false)));
        }

        if (newLoader.bedLoad$isChunkLoader()) {
            int radius = newLoader.bedLoad$getChunkLoadRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    this.setChunkForced(chunkX + dx, chunkZ + dz, true);
                }
            }
            if (BedLoadConfig.SHOW_MESSAGE.get()) BedLoad.players().ifPresent(players -> players.forEach(player -> player.displayClientMessage(Component.translatable("info.bedload.add", chunkX, chunkZ, radius), false)));
        }
    }
}
