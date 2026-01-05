package tf.tuff.viablocks;

import tf.tuff.TuffX;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.scheduler.BukkitRunnable;

public class ChunkPacketListener {

    private static boolean initialized = false;
    public final ViaBlocksPlugin plugin;

    private final Queue<ChunkRequest> pendingRequests = new ConcurrentLinkedQueue<>();
    private final Map<UUID, Set<Long>> pendingKeys = new ConcurrentHashMap<>();
    private BukkitRunnable processorTask;

    public ChunkPacketListener(ViaBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    private static class ChunkRequest {
        final Player player;
        final UUID playerId;
        final World world;
        final int x, z;
        final long key;
        ChunkRequest(Player p, World w, int x, int z, long key) { 
            this.player = p;
            this.playerId = p != null ? p.getUniqueId() : null;
            this.world = w;
            this.x = x;
            this.z = z;
            this.key = key;
        }
    }

    public void start() {
        if (processorTask != null && !processorTask.isCancelled()) {
            return;
        }
        startProcessor();
    }

    private void startProcessor() {
        processorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.plugin.isEnabled()) {
                    this.cancel();
                    return;
                }

                int processed = 0;
                while (processed < 50 && !pendingRequests.isEmpty()) {
                    ChunkRequest req = pendingRequests.poll();
                    if (req == null) break;
                    
                    UUID playerId = req.playerId;
                    Set<Long> pending = playerId != null ? pendingKeys.get(playerId) : null;
                    if (pending != null) {
                        pending.remove(req.key);
                        if (pending.isEmpty()) {
                            pendingKeys.remove(playerId, pending);
                        }
                    }

                    Player p = req.player;
                    if (p == null || !p.isOnline()) continue;

                    if (req.world.isChunkLoaded(req.x, req.z)) {
                        Chunk chunk = req.world.getChunkAt(req.x, req.z);
                        plugin.chunkSenderManager.addChunkToQueue(p, chunk);
                    }
                    processed++;
                }
            }
        };
        processorTask.runTaskTimer(plugin.plugin, 1L, 1L);
    }

    public void stop() {
        if (processorTask != null && !processorTask.isCancelled()) {
            processorTask.cancel();
        }
        pendingRequests.clear();
        pendingKeys.clear();
        processorTask = null;
    }

    public static void initialize(ViaBlocksPlugin plugin) {
        if (initialized) {
            return;
        }

        initialized = true;
    }

    public void handleChunk(TuffX mainPlugin, Player player, World world, int chunkX, int chunkZ) {
        if (!plugin.isPlayerEnabled(player)) {
            return;
        }
        long key = chunkKey(chunkX, chunkZ);
        Set<Long> pending = pendingKeys.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        if (pending.add(key)) {
            pendingRequests.add(new ChunkRequest(player, world, chunkX, chunkZ, key));
        }
    }

    private static long chunkKey(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }
}
