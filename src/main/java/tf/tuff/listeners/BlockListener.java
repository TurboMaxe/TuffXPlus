package tf.tuff.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockListener extends ListenerBase implements Listener {

    public BlockListener() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        viaBlocksService.blockListener.handleBlockForm(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        viaBlocksService.blockListener.handleBlockFade(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        viaBlocksService.blockListener.handleBlockSpread(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        viaBlocksService.blockListener.handleBlockBreak(e);
        y0Service.handleBlockBreak(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent e) {
        viaBlocksService.blockListener.handleBlockGrow(e);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        viaBlocksService.blockListener.handleBlockPlace(e);
        y0Service.handleBlockPlace(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        y0Service.handleBlockPhysics(e);
        viaBlocksService.blockListener.handleBlockPhysics(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        viaBlocksService.blockListener.handleBlockExplode(e);
        y0Service.handleBlockExplode(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        viaBlocksService.blockListener.handleBlockFromTo(e);
        y0Service.handleBlockFromTo(e);
    }
}
