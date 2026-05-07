package tf.tuff.viaentities;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.channel.ChannelHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import tf.tuff.netty.BaseInjector;
import tf.tuff.networking.Channels;

public class EntityInjector extends BaseInjector {

	private final ViaEntitiesService plugin;

	public EntityInjector(ViaEntitiesService plugin) {
		super("viaentities_handler");
		this.plugin = plugin;
	}

	@Override
	protected ChannelHandler createHandler(Player player) {
		return new EntityDataHandler(plugin, player);
	}

	@Override
	protected void onPostInject(Player player) {
		plugin.getPlugin().getServer().getScheduler().runTask(plugin.getPlugin(), () -> {
			sendExistingEntities(player);
		});
	}

	private void sendExistingEntities(Player player) {
		int viewDistance = player.getWorld().getViewDistance() * 16;

		for (Entity entity : player.getWorld().getEntities()) {
			// if the next conditional statement skips the player objects, why need this?
			// if (entity.equals(player)) continue;
			if (entity instanceof Player) continue;
			if (entity.getLocation().distance(player.getLocation()) > viewDistance) continue;

			String entityType = entity.getType().getKey().toString();
			if (plugin.entityMappingManager.isModernEntity(entityType)) {
				sendEntityData(player, entity.getEntityId(), entityType, entity);
			}
		}
	}

	public void sendEntityData(Player player, int entityId, String entityType, Entity entity) {
		if (plugin.isPlayerEnabled(player.getUniqueId())) return;

		int paletteIndex = plugin.entityMappingManager.getEntityIndex(entityType);
		if (paletteIndex == -1) return;

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("SPAWN_ENTITY");
		out.writeInt(entityId);
		out.writeShort(paletteIndex);
		out.writeDouble(entity.getLocation().getX());
		out.writeDouble(entity.getLocation().getY());
		out.writeDouble(entity.getLocation().getZ());
		out.writeFloat(entity.getLocation().getYaw());
		out.writeFloat(entity.getLocation().getPitch());

		player.sendPluginMessage(plugin.getPlugin(), Channels.CLIENTBOUND_CHANNEL.getName(), out.toByteArray());
	}
}
