package de.petropia.spacelifeCore.teleport;

import dev.morphia.annotations.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Entity
public class CrossServerLocation {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String playerUUID;

    public CrossServerLocation(){

    }

    /**
     * Create a completed Location out of a Bukkit Location and a Server as String
     * @param server Servername
     * @param location Bukkit Locaiton
     */
    public CrossServerLocation(String server, Location location){
        this.server = server;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
        this.world = location.getWorld().getName();
    }

    /**
     * Create a complete Location. Yaw and Pitch will be 0
     * @param server Servername
     * @param world World
     * @param x X Coordinate
     * @param y Y Coordinate
     * @param z Z Coordinate
     */
    public CrossServerLocation(String server, String world, double x, double y, double z){
        new CrossServerLocation(server, world, x, y, z, yaw, pitch);
    }

    /**
     * Create a complete Location.
     * @param server Sername
     * @param world World
     * @param x X
     * @param y Y
     * @param z Z
     * @param yaw Yaw
     * @param pitch Pitch
     */
    public CrossServerLocation(String server, String world, double x, double y, double z, float yaw, float pitch){
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.server = server;
        this.world = world;
    }

    public CrossServerLocation(String playerUUID){
        this.playerUUID = playerUUID;
    }

    public Location convertToBukkitLocation(){
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public String getServer() {
        return server;
    }

    public String getWorld(){
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setWorld(String world){
        this.world = world;
    }

    @Override
    public String toString() {
        return "X: " + x +
                "Y: " + y +
                "Z: " + z +
                "Yaw: " + yaw +
                "Pitch: " + pitch +
                "Server: " + server +
                "World: " + world +
                "UUID: " + playerUUID;
    }
}
