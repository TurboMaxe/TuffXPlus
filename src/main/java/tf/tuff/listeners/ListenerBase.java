package tf.tuff.listeners;

import tf.tuff.TuffX;
import tf.tuff.tuffactions.TuffActions;
import tf.tuff.services.viablocks.ViaBlocksService;
import tf.tuff.viaentities.ViaEntitiesService;
import tf.tuff.services.y0.Y0Service;

public abstract class ListenerBase {
    protected final ViaBlocksService viaBlocksService;
    protected final TuffActions tuffActions;
    protected final ViaEntitiesService viaEntitiesService;
    protected final Y0Service y0Service;

    public ListenerBase() {
        viaBlocksService = TuffX.getInstance().getViaBlocksService();
        tuffActions = TuffX.getInstance().getTuffActions();
        viaEntitiesService = TuffX.getInstance().getViaEntitiesService();
        y0Service = TuffX.getInstance().getY0Service();
    }
}
