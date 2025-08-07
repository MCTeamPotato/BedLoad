package me.kall.bedload.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.kall.bedload.BedLoad;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Map;

public class BedLoadConfig {
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHUNK_LOADERS_RAW, CHUNK_LOADER_TAGS_RAW;
    public static Map<ResourceLocation, Integer> CHUNK_LOADERS = new Object2IntOpenHashMap<>();
    public static Map<ResourceLocation, Integer> CHUNK_LOADER_TAGS = new Object2IntOpenHashMap<>();

    public static final ForgeConfigSpec.BooleanValue SHOW_MESSAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push(BedLoad.MOD_NAME);

        CHUNK_LOADERS_RAW = builder.defineList("ChunkLoaderBlocks", Lists.newArrayList(), Predicates.alwaysTrue());
        CHUNK_LOADER_TAGS_RAW = builder.defineList("ChunkLoaderBlockTags", Lists.newArrayList("minecraft:beds=1"), Predicates.alwaysTrue());
        SHOW_MESSAGE = builder.define("NotePlayersOnChunkLoaderUpdate", true);

        builder.pop();
        CONFIG = builder.build();
    }
}
