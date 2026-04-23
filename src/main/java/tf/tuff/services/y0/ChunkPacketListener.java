package tf.tuff.services.y0;

import tf.tuff.TuffX;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;

public class ChunkPacketListener {

    public final Y0Service plugin;

    public ChunkPacketListener(Y0Service plugin) {
        this.plugin = plugin;
    }

    public void handleChunk(TuffX plugin, Player player, World world, int chunkX, int chunkZ){
        if (!this.plugin.isPlayerReady(player)) {
            return;
        }

        MultiLib.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && world.isChunkLoaded(chunkX, chunkZ)) {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                this.plugin.processAndSendChunk(player, chunk);
            }
        });
    }
}