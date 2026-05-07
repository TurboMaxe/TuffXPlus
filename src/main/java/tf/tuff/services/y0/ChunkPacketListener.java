package tf.tuff.services.y0;

import com.github.puregero.multilib.MultiLib;
import lombok.AllArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tf.tuff.TuffX;

@AllArgsConstructor
public class ChunkPacketListener {
    public final Y0Service plugin;


    public void handleChunk(TuffX plugin, Player player, World world, int chunkX, int chunkZ){
        if (!this.plugin.isPlayerReady(player)) {
            return;
        }

        MultiLib.getAsyncScheduler().runNow(plugin, t -> {
            if (player.isOnline() && world.isChunkLoaded(chunkX, chunkZ)) {
                this.plugin.processAndSendChunk(player, world.getChunkAt(chunkX, chunkZ));
            }
        });
    }
}