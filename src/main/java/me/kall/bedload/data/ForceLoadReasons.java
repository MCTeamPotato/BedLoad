package me.kall.bedload.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.bedload.ext.ChunkLoader;
import me.kall.duplicationless.data.ChunkData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Predicate;

public class ForceLoadReasons extends ChunkData.BlockData {
    private final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Set<Long>>> data = new Object2ObjectOpenHashMap<>();

    public ForceLoadReasons() {
        super("BedLoadChunkStorage");
    }

    @Override
    public @NotNull Object2ObjectMap<ResourceLocation, Long2ObjectMap<Set<Long>>> data() {
        return this.data;
    }

    @Override
    public boolean dataTrustable() {
        return false;
    }

    @Override
    public @NotNull Predicate<BlockState> validation() {
        return state -> ((ChunkLoader)state.getBlock()).bedLoad$isChunkLoader();
    }

    public static @NotNull ChunkData<Long, BlockState> get(ServerLevel level) {
        return get(level, ForceLoadReasons::new, "BedLoadChunkStorage");
    }
}
