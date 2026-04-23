package tf.tuff.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class PlayerListener extends ListenerBase implements Listener {

    public PlayerListener() {
        super();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        y0Service.handlePlayerChangeWorld(e);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        y0Service.handlePlayerJoin(e);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        y0Service.handlePlayerQuit(e);
        tuffActions.handlePlayerQuit(e);
        viaBlocksService.blockListener.handlePlayerQuit(e);
        viaEntitiesService.handlePlayerQuit(e);
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent e) {
        tuffActions.handlePlayerInventoryClick(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        y0Service.handleChunkLoad(e);
        viaBlocksService.blockListener.handleChunkLoad(e);
    }

    @EventHandler
    public void onToggleSwim(EntityToggleSwimEvent e) {
        tuffActions.handleToggleSwim(e);
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent e) {
        tuffActions.handleToggleGlide(e);
    }

}
