package me.kall.bedload;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.kall.bedload.config.BedLoadConfig;
import me.kall.bedload.data.ForceLoadReasons;
import me.kall.bedload.ext.ChunkLoader;
import me.kall.duplicationless.data.ChunkData;
import me.kall.duplicationless.event.BlockChangeEvent;
import me.kall.duplicationless.util.Positions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

@Mod(BedLoad.MOD_ID)
public final class BedLoad {
    public static final String MOD_ID = "bedload";

    public BedLoad() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BedLoadConfig.CONFIG);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modBus.addListener((ModConfigEvent.Reloading event) -> BedLoadConfig.setup(event.getConfig().getModId().equals(MOD_ID)));
        forgeBus.addListener(this::blockChange);
        forgeBus.addListener((ServerStartedEvent event) -> BedLoadConfig.setup(true));
        forgeBus.addListener(EventPriority.HIGHEST, (ServerStartedEvent event) -> dataRebuild(event.getServer()));
    }

    public static void dataRebuild(@NotNull MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            ChunkData<Long, BlockState> chunkData = ForceLoadReasons.get(level);
            ResourceLocation dim = level.dimension().location();
            LongIterator oldForcedChunks = chunkData.data().getOrDefault(dim, Long2ObjectMaps.emptyMap()).keySet().longIterator();

            chunkData.rebuild(level);

            LongSet newForcedChunks = chunkData.data().getOrDefault(dim, Long2ObjectMaps.emptyMap()).keySet();
            while (oldForcedChunks.hasNext()) {
                long nextOldChunk = oldForcedChunks.nextLong();
                if (!newForcedChunks.contains(nextOldChunk)) level.setChunkForced(ChunkPos.getX(nextOldChunk), ChunkPos.getZ(nextOldChunk), false);
            }
        }
    }

    public void blockChange(@NotNull BlockChangeEvent event) {
        ServerLevel level = event.level();
        if (BedLoadConfig.IGNORE_WORLD_GEN_BLOCKS.get() && !level.getServer().isSameThread()) return;

        long chunk = event.chunkPos();
        long block = event.blockPos();

        ChunkLoader oldLoader = (ChunkLoader) event.oldState().getBlock();
        ChunkLoader newLoader = (ChunkLoader) event.newState().getBlock();

        int oldRadius = oldLoader.bedLoad$getChunkLoadRadius();
        int newRadius = newLoader.bedLoad$getChunkLoadRadius();

        if (oldLoader.bedLoad$isChunkLoader()) {
            level.getServer().execute(() -> {
                Positions.iterateAround(chunk, oldRadius, chunkKey -> {
                    ChunkData<Long, BlockState> chunkData = ForceLoadReasons.get(level);
                    chunkData.remove(level, chunkKey, block);
                    if (chunkData.viewChunk(level, chunkKey).isEmpty()) {
                        level.setChunkForced(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), false);
                    }
                });
                note(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), oldRadius, false, level);
            });
        }

        if (newLoader.bedLoad$isChunkLoader()) {
            level.getServer().execute(() -> {
                Positions.iterateAround(chunk, newRadius, chunkKey -> {
                    ForceLoadReasons.get(level).add(level, chunkKey, block);
                    level.setChunkForced(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), true);
                });
                note(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), newRadius, true, level);
            });
        }
    }

    private static void note(int chunkX, int chunkZ, int radius, boolean add, ServerLevel level) {
        if (!BedLoadConfig.SHOW_MESSAGE.get()) return;

        Component component = new TranslatableComponent(add ? "info.bedload.add" : "info.bedload.remove", chunkX, chunkZ, radius);
        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(component, false);
        }
    }
}
