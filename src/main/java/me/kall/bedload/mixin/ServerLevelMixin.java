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
        if (((ChunkLoader)oldState.getBlock()).bedLoad$isChunkLoader()) {
            this.setChunkForced(chunkX, chunkZ, false);
            if (BedLoadConfig.SHOW_MESSAGE.get()) BedLoad.players().ifPresent(players -> players.forEach(player -> player.displayClientMessage(Component.translatable("info.bedload.remove", String.valueOf(chunkX), String.valueOf(chunkZ)), false)));
        }
        if (((ChunkLoader)newState.getBlock()).bedLoad$isChunkLoader()) {
            this.setChunkForced(chunkX, chunkZ, true);
            if (BedLoadConfig.SHOW_MESSAGE.get()) BedLoad.players().ifPresent(players -> players.forEach(player -> player.displayClientMessage(Component.translatable("info.bedload.add", String.valueOf(chunkX), String.valueOf(chunkZ)), false)));
        }
    }
}
