package me.kall.bedload.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.kall.bedload.BedLoad;
import me.kall.bedload.ext.ChunkLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Map;

public class BedLoadConfig {
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHUNK_LOADERS_RAW, CHUNK_LOADER_TAGS_RAW;

    public static final ForgeConfigSpec.BooleanValue SHOW_MESSAGE;
    public static final ForgeConfigSpec.BooleanValue IGNORE_WORLD_GEN_BLOCKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("BedLoad");

        CHUNK_LOADERS_RAW = builder.defineList("ChunkLoaderBlocks", Lists.newArrayList(), Predicates.alwaysTrue());
        CHUNK_LOADER_TAGS_RAW = builder.defineList("ChunkLoaderBlockTags", Lists.newArrayList("minecraft:beds=1"), Predicates.alwaysTrue());
        SHOW_MESSAGE = builder.define("NotePlayersOnChunkLoaderUpdate", true);
        IGNORE_WORLD_GEN_BLOCKS = builder.define("IgnoreChunkLoaderBlocksDuringWorldGen", true);

        builder.pop();
        CONFIG = builder.build();
    }

    public static void setup(boolean isMod) {
        if (!isMod) return;
        Object2IntMap<ResourceLocation> chunkLoaders = new Object2IntOpenHashMap<>();
        Object2IntMap<ResourceLocation> chunkLoaderTags = new Object2IntOpenHashMap<>();

        for (String name : CHUNK_LOADERS_RAW.get()) {
            try {
                String[] parts = name.split("=");
                ResourceLocation blockID = ResourceLocation.parse(parts[0]);
                int radius = Integer.parseInt(parts[1]);
                chunkLoaders.put(blockID, radius);
            } catch (Exception exception) {
                throw new RuntimeException("Invalid entry in BedLoad config: " + name);
            }
        }

        for (String name : CHUNK_LOADER_TAGS_RAW.get()) {
            try {
                String[] parts = name.split("=");
                ResourceLocation tagID = ResourceLocation.parse(parts[0]);
                int radius = Integer.parseInt(parts[1]);
                chunkLoaderTags.put(tagID, radius);
            } catch (Exception exception) {
                throw new RuntimeException("Invalid entry in BedLoad config: " + name);
            }
        }

        ForgeRegistries.BLOCKS.getValues().stream().map(block -> ((ChunkLoader)block)).forEach(chunkLoader -> {
            chunkLoader.bedLoad$setChunkLoadRadius(-1);
            chunkLoader.bedLoad$setIsChunkLoader(false);
        });

        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            Block block = entry.getValue();

            int radius = -1;
            boolean isLoader = false;

            if (chunkLoaders.containsKey(id)) {
                radius = chunkLoaders.getInt(id);
                isLoader = true;
            } else {
                for (TagKey<Block> tagKey : block.defaultBlockState().getTags().toList()) {
                    ResourceLocation tagLoc = tagKey.location();
                    if (chunkLoaderTags.containsKey(tagLoc)) {
                        radius = chunkLoaderTags.getInt(tagLoc);
                        isLoader = true;
                        break;
                    }
                }
            }

            ((ChunkLoader) block).bedLoad$setIsChunkLoader(isLoader);
            ((ChunkLoader) block).bedLoad$setChunkLoadRadius(radius);
        }

        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            BedLoad.dataRebuild(server);

            Component component = new TranslatableComponent("info.bedload");
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.displayClientMessage(component, false);
            }
        } catch (Exception ignored) {}
    }
}
