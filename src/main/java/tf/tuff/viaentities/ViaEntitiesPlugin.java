package tf.tuff.viaentities;

import tf.tuff.TuffX;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ViaEntitiesPlugin {

    public static final String CLIENTBOUND_CHANNEL = "viaentities:data";
    public static final String SERVERBOUND_CHANNEL = "viaentities:handshake";

    public final Set<UUID> viaEntitiesEnabledPlayers = new HashSet<>();

    static ViaEntitiesPlugin instance;

    public EntityMappingManager entityMappingManager;
    private EntityInjector entityInjector;
    private boolean enabled = true;
    private boolean debug = false;
    private int maxDistance = -1;

    public TuffX plugin;

    public ViaEntitiesPlugin(TuffX plugin) {
        this.plugin = plugin;
    }

    public void onTuffXLoad() {

    }

    public void onTuffXReload() {
        loadConfig();
    }

    private void loadConfig() {
        enabled = plugin.getConfig().getBoolean("viaentities.viaentities-enabled", true);
        debug = plugin.getConfig().getBoolean("viaentities.debug", true);
        maxDistance = plugin.getConfig().getInt("viaentities.max-distance", -1);
    }

    public boolean isDebug() {
        return debug;
    }

    public void debug(String message) {
        plugin.getLogger().info(message);
    }

    public void onTuffXEnable() {
        instance = this;

        loadConfig();

        this.entityMappingManager = new EntityMappingManager();
        this.entityInjector = new EntityInjector(this);

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CLIENTBOUND_CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, SERVERBOUND_CHANNEL, plugin);

        if (enabled) {
            plugin.getLogger().info("ViaEntities enabled with " + entityMappingManager.getModernEntityCount() + " modern entities");
        } else {
            plugin.getLogger().info("ViaEntities disabled in config");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void handlePacket(Player player, byte[] message) {
        if (!enabled) return;
        if (!isPlayerEnabled(player.getUniqueId())) {
            debug("Received handshake from " + player.getName());
            setPlayerEnabled(player.getUniqueId(), true);
            entityInjector.inject(player);
            sendPaletteToClient(player);
            debug("Sent palette with " + entityMappingManager.getModernEntityCount() + " entities to " + player.getName());
        }
    }

    private void sendPaletteToClient(Player player) {
        com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();
        out.writeUTF("INIT_PALETTE");

        java.util.List<String> palette = entityMappingManager.getAllModernEntities();
        out.writeInt(palette.size());

        for (String entityType : palette) {
            out.writeUTF(entityType);
        }

        player.sendPluginMessage(plugin, CLIENTBOUND_CHANNEL, out.toByteArray());
    }

    public void handlePlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        entityInjector.eject(event.getPlayer());
        viaEntitiesEnabledPlayers.remove(event.getPlayer().getUniqueId());
    }

    public void onTuffXDisable() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CLIENTBOUND_CHANNEL);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, SERVERBOUND_CHANNEL);
    }

    public boolean isPlayerEnabled(UUID playerId) {
        return viaEntitiesEnabledPlayers.contains(playerId);
    }

    public void setPlayerEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            viaEntitiesEnabledPlayers.add(playerId);
        } else {
            viaEntitiesEnabledPlayers.remove(playerId);
        }
    }

    public EntityInjector getEntityInjector() {
        return entityInjector;
    }
}
