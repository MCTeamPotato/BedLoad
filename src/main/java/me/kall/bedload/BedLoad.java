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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(BedLoad.MOD_ID)
public final class BedLoad {
    public static final String MOD_ID = "bedload";
    public static final String MOD_NAME = "BedLoad";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

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
        parseChunkLoaderConfig();

        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            Block block = entry.getValue();

            int radius = -1;
            if (BedLoadConfig.CHUNK_LOADERS.containsKey(id)) {
                radius = BedLoadConfig.CHUNK_LOADERS.get(id);
            } else {
                for (var tagKey : block.builtInRegistryHolder().tags().toList()) {
                    ResourceLocation tagId = tagKey.location();
                    if (BedLoadConfig.CHUNK_LOADER_TAGS.containsKey(tagId)) {
                        radius = BedLoadConfig.CHUNK_LOADER_TAGS.get(tagId);
                        break;
                    }
                }
            }

            ((ChunkLoader) block).bedLoad$setIsChunkLoader(radius >= 0);
            if (radius >= 0) ((ChunkLoader) block).bedLoad$setChunkLoadRadius(radius);
        }
    }

    private static void parseChunkLoaderConfig() {
        BedLoadConfig.CHUNK_LOADERS.clear();
        BedLoadConfig.CHUNK_LOADER_TAGS.clear();

        for (String s : BedLoadConfig.CHUNK_LOADERS_RAW.get()) {
            parseConfigEntry(s).ifPresent(e -> BedLoadConfig.CHUNK_LOADERS.put(e.getKey(), e.getValue()));
        }

        for (String s : BedLoadConfig.CHUNK_LOADER_TAGS_RAW.get()) {
            parseConfigEntry(s).ifPresent(e -> BedLoadConfig.CHUNK_LOADER_TAGS.put(e.getKey(), e.getValue()));
        }
    }

    private static Optional<Map.Entry<ResourceLocation, Integer>> parseConfigEntry(String entry) {
        try {
            String[] parts = entry.split("=");
            ResourceLocation id = ResourceLocation.parse(parts[0]);
            int radius = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return Optional.of(Map.entry(id, radius));
        } catch (Exception e) {
            BedLoad.LOGGER.warn("Invalid chunk loader config entry: {}", entry);
            return Optional.empty();
        }
    }


    public static Optional<List<ServerPlayer>> players() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return Optional.of(server.getPlayerList().getPlayers());
        return Optional.empty();
    }

    public static void note(int chunkX, int chunkZ, int radius, boolean added) {
        Component component = Component.translatable(added ? "info.bedload.add" : "info.bedload.remove", chunkX, chunkZ, radius);
        players().ifPresent(players -> players.forEach(player -> player.displayClientMessage(component, false)));
    }
}
