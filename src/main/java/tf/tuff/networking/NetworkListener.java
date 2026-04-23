package tf.tuff.networking;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import org.bukkit.entity.Player;
import tf.tuff.TuffX;

public class NetworkListener implements PacketListener {

    private final TuffX plugin;

    public NetworkListener(TuffX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.CHUNK_DATA) return;

        Player player = event.getPlayer();
        WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);

        plugin.getY0Service().cpl
                .handleChunk(plugin,
                        player,
                        player.getWorld(),
                        wrapper.getColumn().getX(),
                        wrapper.getColumn().getZ()
                );

    }
}
