package me.kall.bedload.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import me.kall.bedload.BedLoad;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class BedLoadConfig {
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHUNK_LOADERS, CHUNK_LOADER_TAGS;
    public static final ForgeConfigSpec.BooleanValue SHOW_MESSAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push(BedLoad.MOD_NAME);
        CHUNK_LOADERS = builder.defineList("ChunkLoaderBlocks", Lists.newArrayList(), Predicates.alwaysTrue());
        CHUNK_LOADER_TAGS = builder.defineList("ChunkLoaderBlockTags", Lists.newArrayList("minecraft:beds"), Predicates.alwaysTrue());
        SHOW_MESSAGE = builder.define("NotePlayersOnChunkLoaderUpdate", true);
        builder.pop();
        CONFIG = builder.build();
    }
}
