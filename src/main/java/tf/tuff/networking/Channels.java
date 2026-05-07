package tf.tuff.networking;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Channels {
    CLIENTBOUND_CHANNEL("viablocks:data"),
    SERVERBOUND_CHANNEL("viablocks:handshake");

    private String name;
}
