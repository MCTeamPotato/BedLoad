package me.kall.bedload.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ForceLoadReasons extends SavedData {
    private static final String ID = "bedload_force_load_reasons";

    private final Object2ObjectMap<ResourceLocation, Long2ObjectMap<LongSet>> data = new Object2ObjectOpenHashMap<>();

    public boolean add(@NotNull ResourceLocation dimId, long chunkPos, long blockPos) {
        Long2ObjectMap<LongSet> dimMap = data.computeIfAbsent(dimId, k -> new Long2ObjectOpenHashMap<>());
        LongSet blocks = dimMap.computeIfAbsent(chunkPos, k -> new LongOpenHashSet());
        boolean changed = blocks.add(blockPos);
        if (changed) setDirty();
        return changed;
    }

    public boolean remove(@NotNull ResourceLocation dimId, long chunkPos, long blockPos) {
        Long2ObjectMap<LongSet> dimMap = data.get(dimId);
        if (dimMap == null) return true;
        LongSet blocks = dimMap.get(chunkPos);
        if (blocks == null) return true;

        blocks.remove(blockPos);
        if (blocks.isEmpty()) {
            dimMap.remove(chunkPos);
            if (dimMap.isEmpty()) data.remove(dimId);
            setDirty();
            return true;
        }
        setDirty();
        return false;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag dimensions = new ListTag();

        for (var dimEntry : data.object2ObjectEntrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString("dim", dimEntry.getKey().toString());

            ListTag chunkList = new ListTag();
            for (var chunkEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putLong("pos", chunkEntry.getLongKey());

                ListTag blockList = new ListTag();
                for (long blockLong : chunkEntry.getValue()) {
                    blockList.add(LongTag.valueOf(blockLong));
                }
                chunkTag.put("blocks", blockList);
                chunkList.add(chunkTag);
            }

            dimTag.put("chunks", chunkList);
            dimensions.add(dimTag);
        }

        tag.put("dimensions", dimensions);
        return tag;
    }

    public static @NotNull ForceLoadReasons load(@NotNull CompoundTag tag) {
        ForceLoadReasons reasons = new ForceLoadReasons();

        ListTag dimensions = tag.getList("dimensions", Tag.TAG_COMPOUND);
        for (Tag dimTag0 : dimensions) {
            CompoundTag dimTag = (CompoundTag) dimTag0;
            ResourceLocation dimId = ResourceLocation.parse(dimTag.getString("dim"));
            Long2ObjectMap<LongSet> dimMap = new Long2ObjectOpenHashMap<>();

            ListTag chunkList = dimTag.getList("chunks", Tag.TAG_COMPOUND);
            for (Tag chunkTag0 : chunkList) {
                CompoundTag chunkTag = (CompoundTag) chunkTag0;
                long pos = chunkTag.getLong("pos");

                LongSet blocks = new LongOpenHashSet();
                ListTag blockList = chunkTag.getList("blocks", Tag.TAG_LONG);
                for (Tag blockTag : blockList) {
                    blocks.add(((LongTag) blockTag).getAsLong());
                }

                dimMap.put(pos, blocks);
            }

            reasons.data.put(dimId, dimMap);
        }

        return reasons;
    }

    public static @NotNull ForceLoadReasons get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ForceLoadReasons::load, ForceLoadReasons::new, ID);
    }

    public boolean hasAny(@NotNull ResourceLocation dimId, long chunkPos) {
        var dimMap = data.get(dimId);
        if (dimMap == null) return false;
        var set = dimMap.get(chunkPos);
        return set != null && !set.isEmpty();
    }
}
