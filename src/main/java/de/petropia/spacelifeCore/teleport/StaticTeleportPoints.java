package de.petropia.spacelifeCore.teleport;

import de.petropia.spacelifeCore.SpacelifeCore;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Class for storing non changing crossserverlocation, read from config
 */
public class StaticTeleportPoints {

    private StaticTeleportPoints(){}

    public static final CrossServerLocation SPAWN = loadFromConfig("spawn");

    private static CrossServerLocation loadFromConfig(String id){
        String path = "teleportPoints." + id + ".";
        FileConfiguration config = SpacelifeCore.getInstance().getConfig();
        double x = config.getDouble(path + "x");
        double y = config.getDouble(path  + "y");
        double z = config.getDouble(path  + "z");
        float pitch = config.getLong(path + "pitch");
        float yaw = config.getLong(path + "yaw");
        String world = config.getString(path + "world");
        String server = config.getString(path + "server");
        return new CrossServerLocation(server, world, x, y, z, yaw, pitch);
    }
}
