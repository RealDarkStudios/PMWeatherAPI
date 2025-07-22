package net.nullved.pmweatherapi.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.radar.RadarStorage;

import java.util.Collection;
import java.util.Set;

/**
 * The interface defining a Storage such as {@link RadarStorage}
 * <br><br>
 * On the server-side, there is a {@link IServerStorage} for each dimension of a world.
 * On the client-side, there is one {@link IClientStorage} for each player on a world.
 * <br><br>
 * To add or remove {@link BlockPos}, use {@link #add} and {@link #remove}.
 * To get all {@link BlockPos} use {@link #getAll()} or {@link #getInChunk(ChunkPos)}
 * <br><br>
 * Optionally, Storages can be numerically versioned, however, you must write you own version mismatch handler
 * <br><br>
 * For method definitions, see {@link PMWStorage}
 *
 * @see PMWStorage
 * @since 0.15.1.1
 */
public interface IStorage {
    Level getLevel();
    ResourceLocation getId();
    int version();

    void clean();

    Set<BlockPos> getAll();
    Set<BlockPos> getAllWithinRange(BlockPos base, double radius);
    Set<BlockPos> getInChunk(ChunkPos pos);
    Set<BlockPos> getInAdjacentChunks(ChunkPos pos);

    boolean shouldRecalculate(ChunkPos pos);

    void add(BlockPos pos);
    void add(Collection<BlockPos> pos);
    void remove(BlockPos pos);
    void remove(Collection<BlockPos> pos);

    CompoundTag save(CompoundTag tag);
    void read();
}
