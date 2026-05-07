package tf.tuff.services.viablocks.version;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public abstract class VersionAdapter {

    public abstract String getMaterialKey(Material material);

    public abstract int getClientViewDistance(Player player);

    public abstract void giveCustomBlocks(Player player);

    public abstract EnumSet<Material> getModernMaterials();
}