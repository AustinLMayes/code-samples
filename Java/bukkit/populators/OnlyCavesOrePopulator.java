/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.games.uhc.populators;

import com.google.common.collect.Lists;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import network.walrus.games.core.util.GameTask;
import network.walrus.utils.bukkit.block.BlockUtils;
import network.walrus.utils.bukkit.block.CoordXZ;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * A block populator which removes ores from non-cave locations under y 80.
 *
 * @author Austin Mayes
 */
public class OnlyCavesOrePopulator implements Listener {

    private final List<CoordXZ> handled = Lists.newArrayList();
    private final Random RANDOM = new Random();
    private final World world;
    private List<Chunk> toPopulate = Lists.newArrayList();
    private ConcurrentHashMap<Material, ConcurrentLinkedQueue<Vector>> neededUpdates =
            new ConcurrentHashMap<>();
    private TLongList solids = new TLongArrayList();
    private TLongList air = new TLongArrayList();

    /**
     * @param world this populator should be acting in
     */
    public OnlyCavesOrePopulator(World world) {
        this.world = world;
        GameTask.of(
                "Only caves checker",
                () -> {
                    if (toPopulate.isEmpty()) {
                        return;
                    }
                    List<Chunk> populating =
                            new ArrayList<>(toPopulate.subList(0, Math.min(toPopulate.size(), 5)));
                    List<Chunk> list = new ArrayList<>();
                    for (Chunk chunk1 : populating) {
                        if (chunk1.isLoaded()) {
                            list.add(chunk1);
                        }
                    }
                    populating = list;
                    toPopulate.removeAll(populating);
                    // Bukkit.getLogger().info(toPopulate.size() + " chunks left to populate.");
                    for (Chunk chunk : populating) {
                        for (int x = 0; x < 16; x++) {
                            if (!chunk.isLoaded()) {
                                break;
                            }
                            for (int y = 10; y < 80; y++) {
                                if (!chunk.isLoaded()) {
                                    break;
                                }
                                for (int z = 0; z < 16; z++) {
                                    if (!chunk.isLoaded()) {
                                        break;
                                    }
                                    Block block = chunk.getBlock(x, y, z);
                                    if (block.getType().name().toLowerCase().contains("ore")) {
                                        if (!touchesAir(block.getState())) {
                                            if (RANDOM.nextInt(100) < 65) {
                                                neededUpdates.putIfAbsent(
                                                        Material.STONE, new ConcurrentLinkedQueue<>());
                                                neededUpdates.get(Material.STONE).add(block.getLocation().toVector());
                                            }
                                            // Bukkit.getLogger().info(block.getLocation() + " does NOT touch air");
                                        } else if (RANDOM.nextInt(100) < 35) {
                                            if (RANDOM.nextBoolean()) {
                                                replace(block.getLocation().clone().add(0, 1, 0), block.getType());
                                                replace(block.getLocation().clone().add(0, -1, 0), block.getType());
                                            }
                                            if (RANDOM.nextBoolean()) {
                                                replace(block.getLocation().clone().add(-1, 0, 0), block.getType());
                                                replace(block.getLocation().clone().add(1, 0, 0), block.getType());
                                            }
                                            if (RANDOM.nextBoolean()) {
                                                replace(block.getLocation().clone().add(0, 0, -1), block.getType());
                                                replace(block.getLocation().clone().add(0, 0, 1), block.getType());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
                .repeatAsync(0, 10);
        GameTask.of(
                "Only caves updater",
                () -> {
                    if (neededUpdates.isEmpty()) {
                        return;
                    }

                    int count = 0;
                    for (ConcurrentLinkedQueue<Vector> vectorConcurrentLinkedQueue :
                            neededUpdates.values()) {
                        int size = vectorConcurrentLinkedQueue.size();
                        count += size;
                    }
                    AtomicInteger handled = new AtomicInteger();
                    for (Entry<Material, ConcurrentLinkedQueue<Vector>> entry :
                            neededUpdates.entrySet()) {
                        Material material = entry.getKey();
                        ConcurrentLinkedQueue<Vector> vectors = entry.getValue();
                        List<Vector> toHandle = new ArrayList<>();
                        for (Vector vec : vectors) {
                            if (vec.toLocation(this.world).isChunkLoaded()) {
                                toHandle.add(vec);
                            }
                        }
                        GameTask.of(
                                "Only caves changer",
                                () -> this.world.fastBlockChange(toHandle, new MaterialData(material)))
                                .now();
                        vectors.removeAll(toHandle);
                        if (vectors.isEmpty()) {
                            neededUpdates.remove(material);
                        }
                        handled.addAndGet(toHandle.size());
                    }
                    // Bukkit.getLogger().info("Replaced " + handled.get() + " ores, " + (count -
                    // handled.get()) + " in queue");
                })
                .repeatAsync(0, 20 * 3);
    }

    private void replace(Location location, Material mat) {
        if (location.isChunkLoaded() && location.getBlock().getType() == Material.STONE) {
            // Bukkit.getLogger().info(location.toVector().toString());
            neededUpdates.putIfAbsent(mat, new ConcurrentLinkedQueue<>());
            neededUpdates.get(mat).add(location.toVector());
        }
    }

    private boolean touchesAir(BlockState state) {
        if (isAir(state.getX(), state.getY(), state.getZ())) {
            return true;
        }
        for (int x = state.getX() - 5; x < state.getX() + 5; x++) {
            for (int y = state.getY() - 2; y < state.getY() + 2; y++) {
                for (int z = state.getZ() - 5; z < state.getZ() + 5; z++) {
                    if (isAir(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAir(int x, int y, int z) {
        long encoded = BlockUtils.encodePos(x, y, x);
        if (air.contains(encoded)) {
            return true;
        }
        if (solids.contains(encoded)) {
            return false;
        }
        Location location = new Location(this.world, x, y, z);
        if (!location.isChunkLoaded()) {
            return false;
        }
        Block block = location.getBlock();
        if (block.getType() == Material.AIR
                || block.getType() == Material.WATER
                || block.getType() == Material.LAVA) {
            air.add(encoded);
            return true;
        } else {
            solids.add(encoded);
            return false;
        }
    }

    /**
     * Update ores on chunk load
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.getChunk().getWorld().getName().equals(this.world.getName())) return;

        CoordXZ chunk = new CoordXZ(event.getChunk().getX(), event.getChunk().getZ());

        if (handled.contains(chunk)) return;

        handled.add(chunk);
        toPopulate.add(event.getChunk());
    }
}
