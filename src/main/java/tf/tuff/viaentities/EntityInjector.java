package tf.tuff.viaentities;

import com.viaversion.viaversion.api.Via;
import io.netty.channel.Channel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityInjector {

    private final ViaEntitiesPlugin plugin;

    public EntityInjector(ViaEntitiesPlugin plugin) {
        this.plugin = plugin;
    }

    public void inject(Player player) {
        UUID uuid = player.getUniqueId();
        var viaConnection = Via.getAPI().getConnection(uuid);
        if (viaConnection == null) return;

        Channel channel = viaConnection.getChannel();
        if (channel == null) return;

        channel.eventLoop().submit(() -> {
            try {
                if (channel.pipeline().get("viaentities_handler") != null) {
                    channel.pipeline().remove("viaentities_handler");
                }

                String targetHandler = null;
                String[] handlers = {"packet_handler", "encoder", "via-encoder"};
                for (String h : handlers) {
                    if (channel.pipeline().get(h) != null) {
                        targetHandler = h;
                        break;
                    }
                }

                if (targetHandler != null) {
                    channel.pipeline().addBefore(
                        targetHandler,
                        "viaentities_handler",
                        new EntityDataHandler(plugin, player)
                    );
                } else {
                    channel.pipeline().addFirst("viaentities_handler", new EntityDataHandler(plugin, player));
                }

                plugin.plugin.getServer().getScheduler().runTask(plugin.plugin, () -> {
                    sendExistingEntities(player);
                });

            } catch (Exception e) {
            }
        });
    }

    private void sendExistingEntities(Player player) {
        int viewDistance = player.getWorld().getViewDistance() * 16;

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.equals(player)) continue;
            if (entity instanceof Player) continue;

            double distance = entity.getLocation().distance(player.getLocation());
            if (distance > viewDistance) continue;

            String entityType = entity.getType().getKey().toString();
            if (plugin.entityMappingManager.isModernEntity(entityType)) {
                sendEntityData(player, entity.getEntityId(), entityType, entity);
            }
        }
    }

    public void sendEntityData(Player player, int entityId, String entityType, Entity entity) {
        if (!plugin.isPlayerEnabled(player.getUniqueId())) return;

        int paletteIndex = plugin.entityMappingManager.getEntityIndex(entityType);
        if (paletteIndex == -1) return;

        com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();
        out.writeUTF("SPAWN_ENTITY");
        out.writeInt(entityId);
        out.writeShort(paletteIndex);
        out.writeDouble(entity.getLocation().getX());
        out.writeDouble(entity.getLocation().getY());
        out.writeDouble(entity.getLocation().getZ());
        out.writeFloat(entity.getLocation().getYaw());
        out.writeFloat(entity.getLocation().getPitch());

        byte[] data = out.toByteArray();
        player.sendPluginMessage(plugin.plugin, ViaEntitiesPlugin.CLIENTBOUND_CHANNEL, data);
    }

    public void eject(Player player) {
        UUID uuid = player.getUniqueId();
        var viaConnection = Via.getAPI().getConnection(uuid);
        if (viaConnection == null) return;

        Channel channel = viaConnection.getChannel();
        if (channel != null && channel.isOpen()) {
            channel.eventLoop().submit(() -> {
                try {
                    if (channel.pipeline().get("viaentities_handler") != null) {
                        channel.pipeline().remove("viaentities_handler");
                    }
                } catch (Exception e) {
                }
            });
        }
    }
}
