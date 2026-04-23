package tf.tuff.networking;

import com.github.puregero.multilib.MultiLib;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ServerRegistry {
    private final JavaPlugin p;
    private final String wsUrl;
    private final String server;
    private boolean running = true;
    private WebSocket client;

    public ServerRegistry(JavaPlugin pl, String registryUrl, String serverAddr) {
        p = pl;
        wsUrl = registryUrl;
        server = serverAddr;
    }

    public void connect() {
        CompletableFuture.runAsync(this::doConnect);
    }

    private void doConnect() {
        if (!running) return;

        try {
           client =  new WebSocketFactory().createSocket(wsUrl, 30);
           client.addListeners(
            List.of(
                new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                        client.sendText("{\"type\":\"register\",\"server\":\"" + server + "\"}");
                    }
                },
                new WebSocketAdapter() {
                    @Override
                    public void onDisconnected(
                        WebSocket websocket,
                        WebSocketFrame serverCloseFrame,
                        WebSocketFrame clientCloseFrame,
                        boolean closedByServer) {

                        if (running) MultiLib.getAsyncScheduler().runDelayed(p, t -> doConnect(), 100L);
                    }
                }
            ));
        client.connect();
        } catch (Exception e) {
            if (running) p.getServer().getScheduler().runTaskLaterAsynchronously(p, this::doConnect, 100L);
        }
    }

    public void disconnect() {
        running = false;
        if (client != null) client.disconnect();
    }
}
