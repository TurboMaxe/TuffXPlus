package tf.tuff.netty;

import com.viaversion.viaversion.api.Via;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.bukkit.entity.Player;
import tf.tuff.services.viablocks.CustomBlockListener;
import tf.tuff.services.y0.Y0Service;

import java.util.UUID;

public class ChunkInjector extends BaseInjector {

	private final CustomBlockListener viaBlocks;
	private final Y0Service y0;

	public ChunkInjector(CustomBlockListener viaBlocks, Y0Service y0) {
		super("tuff_chunk_handler");
		this.viaBlocks = viaBlocks;
		this.y0 = y0;
	}

	@Override
	protected ChannelHandler createHandler(Player player) {
		return new ChunkHandler(viaBlocks, y0, player);
	}

	@Override
	public void inject(Player player) {
		UUID uuid = player.getUniqueId();
		var viaConnection = Via.getAPI().getConnection(uuid);
		if (viaConnection == null) return;

		Channel channel = viaConnection.getChannel();
		if (channel == null) return;

		channel.eventLoop().submit(() -> {
			try {
				if (channel.pipeline().get("tuff_chunk_handler") != null) {
					channel.pipeline().remove("tuff_chunk_handler");
				}
				if (channel.pipeline().get("viablocks_chunk_handler") != null) {
					channel.pipeline().remove("viablocks_chunk_handler");
				}
				if (channel.pipeline().get("y0_chunk_handler") != null) {
					channel.pipeline().remove("y0_chunk_handler");
				}

				if (channel.pipeline().get("via-encoder") != null) {
					channel.pipeline().addBefore(
						"via-encoder",
						"tuff_chunk_handler",
						new ChunkHandler(viaBlocks, y0, player)
					);
				} else {
					channel.pipeline().addFirst("tuff_chunk_handler",
						new ChunkHandler(viaBlocks, y0, player));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
