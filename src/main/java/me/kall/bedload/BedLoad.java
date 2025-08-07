package me.kall.bedload;

import me.kall.bedload.api.ChunkLoader;
import me.kall.bedload.config.BedLoadConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(BedLoad.MOD_ID)
public final class BedLoad {
    public static final String MOD_ID = "bedload";
    public static final String MOD_NAME = "BedLoad";

    public BedLoad(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> updateLoaders());
        modBus.addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getModId().equals(MOD_ID)) {
                updateLoaders();
                players().ifPresent(players -> players.forEach(player -> player.displayClientMessage(Component.translatable("info.bedload"), false)));
            }
        });
        context.registerConfig(ModConfig.Type.COMMON, BedLoadConfig.CONFIG);
    }

    @SuppressWarnings("deprecation")
    private static void updateLoaders() {
        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            Block block = entry.getValue();
            ((ChunkLoader)block).bedLoad$setIsChunkLoader(BedLoadConfig.CHUNK_LOADERS.get().contains(id.toString()) || block.builtInRegistryHolder().tags().anyMatch(blockTagKey -> BedLoadConfig.CHUNK_LOADER_TAGS.get().contains(blockTagKey.location().toString())));
        }
    }

    public static Optional<List<ServerPlayer>> players() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return Optional.of(server.getPlayerList().getPlayers());
        return Optional.empty();
    }
}
