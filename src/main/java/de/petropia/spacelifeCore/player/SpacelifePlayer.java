package de.petropia.spacelifeCore.player;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.home.Home;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.spacelifeCore.teleport.CrossServerMessageListener;
import dev.morphia.annotations.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Entity(value = "spacelifePlayers")
public class SpacelifePlayer {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String uuid;
    private double money;
    private int xpLevel;
    private float xpPoints;
    private int food;
    private boolean hasNightvision;
    private Map<Integer, String> inventory;//Key for inv slot, String as base64 byte[] for serialized Item
    private Map<Integer, Map<Integer, String>> enderchests; //First int = enderchest number, //second map -> int = inv slot, String Itms as base64
    private List<String> potions;
    private List<Home> homes;
    private CrossServerLocation targetLocation;
    private HashMap<String, JobStats> jobStats; //Rely on autosave
    @Transient
    private int autoSaveTaskID = -1;
    @Transient
    private int nightvisionTaskID = -1;

    /**
     * This Construtor is for manuel first time player creation
     *
     * @param uuid The uuid of the minecraft player
     */
    public SpacelifePlayer(String uuid) {
        this.uuid = uuid;
        this.food = 20;
        save();
        autoSave();
    }

    /**
     * Constructor for Morphia
     */
    public SpacelifePlayer() {
        autoSave();
    }

    /**
     * Retrieve the uuid which is provided by Mojang
     *
     * @return @{@link UUID} for the player
     */
    public UUID getUUID() {
        return UUID.fromString(uuid);
    }

    /**
     * Get the current money of a player
     *
     * @return money as double
     */
    public double getMoney() {
        return money;
    }

    /**
     * Add money to a player
     *
     * @param money money to add as double
     */
    public void addMoney(double money) {
        this.money += money;
        this.money = Math.round(this.money * 100.0) / 100.0;
        save();
    }

    /**
     * Remove money from a player.
     *
     * @param money money to remove as double
     * @return false when player has not enough money
     */
    public boolean subtractMoney(double money) {
        if ((this.money - money) < 0) {
            return false;
        }
        this.money -= money;
        this.money = Math.round(this.money * 100.0) / 100.0;
        save();
        return true;
    }

    /**
     * Override all money from a player
     *
     * @param money money to override as double
     */
    public void setMoney(double money) {
        this.money = money;
        save();
    }

    private void autoSave() {
        if (autoSaveTaskID != -1) {
            return;
        }
        autoSaveTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SpacelifeCore.getInstance(), () -> {
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if (player != null && player.isOnline()) {
                save();
                return;
            }
            Bukkit.getScheduler().cancelTask(autoSaveTaskID);
        }, 3 * 60 * 20, 3 * 60 * 20);
    }

    /**
     * Saves player inventory and clears it. Player needs to be online!
     * Saves also:
     * - XP
     */
    public CompletableFuture<Void> saveInventory() {
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player == null) {
            return CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException();
            });  //Need to be completed
        }
        int xpLevel = player.getLevel();
        float xpPoints = player.getExp();
        int food = player.getFoodLevel();
        List<String> potions = new ArrayList<>();
        Map<Integer, String> newInventory = new HashMap<>();
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack item = player.getInventory().getContents()[i];
            if (item == null) {
                continue;
            }
            newInventory.put(i, toBase64(item.serializeAsBytes()));
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeObject(effect);
                potions.add(toBase64(outputStream.toByteArray()));
                player.removePotionEffect(effect.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        player.getInventory().clear();
        if (newInventory.equals(inventory)  //prevent saving unchanged data
                && this.xpLevel == xpLevel
                && this.xpPoints == xpPoints
                && this.food == food
                && potions.equals(this.potions)) {
            return CompletableFuture.supplyAsync(() -> null);
        }
        this.xpLevel = xpLevel;
        this.xpPoints = xpPoints;
        this.food = food;
        if (newInventory.size() == 0) {
            inventory = null;
        } else {
            inventory = newInventory;
        }
        if (potions.size() == 0) {
            this.potions = null;
        } else {
            this.potions = potions;
        }
        return save();
    }

    /**
     * Clears a player's inventory and replaces it. Call only Sync
     */
    public void loadInventory() {
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player == null) {
            return;
        }
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.setFoodLevel(food);
        player.setExp(xpPoints);
        player.setLevel(xpLevel);
        if (inventory != null) {
            for (Map.Entry<Integer, String> indexEntry : inventory.entrySet()) {
                int index = indexEntry.getKey();
                if (indexEntry.getValue() == null) {
                    player.getInventory().setItem(index, new ItemStack(Material.AIR));
                    continue;
                }
                player.getInventory().setItem(index, ItemStack.deserializeBytes(fromBase64(indexEntry.getValue())));
            }
        }
        if (potions != null) {
            for (String string : potions) {
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fromBase64(string)); BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(inputStream)) {
                    PotionEffect effect = (PotionEffect) bukkitStream.readObject();
                    player.addPotionEffect(effect);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Clear players Inventory on server (if online) and in DB.
     * Includes xp, food, etc.
     */
    public void clearPlayer() {
        inventory = null;
        xpLevel = 0;
        xpPoints = 0;
        homes = null;
        enderchests = null;
        targetLocation = null;
        food = 20;
        save().thenAccept(v -> Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                loadInventory();
            }
        }));
    }

    /**
     * Teleport a player across spacelife servers. Please use this only for cross server teleportation!
     *
     * @param location Location to teleport
     * @return Completeable future with boolean. true -> success
     */
    public CompletableFuture<Boolean> teleportCrossServer(CrossServerLocation location) {
        final Player bukkitPlayer = Bukkit.getPlayer(UUID.fromString(uuid));
        return CompletableFuture.supplyAsync(() -> {
            if (bukkitPlayer == null) {
                return false;
            }
            if (BlockAnyActionListener.isPlayerBlocked(bukkitPlayer)) {
                return false;
            }
            this.targetLocation = location;
            if (targetLocation.getPlayerUUID() != null && !targetLocation.getPlayerUUID().isEmpty()) {
                if (targetLocation.getPlayerUUID().equalsIgnoreCase(uuid)) {
                    return null;
                }
                targetLocation = CrossServerMessageListener.getForPlayer(location).join();
            }
            if (targetLocation == null) {
                return false;
            }
            if (targetLocation.getServer() == null || targetLocation.getServer().isEmpty()) {
                targetLocation = null;
                return false;
            }
            if (CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServiceByName(targetLocation.getServer()) == null) {
                targetLocation = null;
                return false;
            }
            if (SpacelifeCore.getInstance().getCloudNetAdapter().getServerInstanceName().equalsIgnoreCase(location.getServer())) {
                Location localLocation = new Location(Bukkit.getWorld(targetLocation.getWorld()), targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), targetLocation.getYaw(), targetLocation.getPitch());
                Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> bukkitPlayer.teleport(localLocation));
                targetLocation = null;
                return true;
            }
            SpacelifePlayerLoadingListener.blockInvSave(bukkitPlayer);
            saveInventory().join();
            BlockAnyActionListener.blockPlayer(bukkitPlayer);
            CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class)
                    .getPlayerExecutor(UUID.fromString(uuid)).connect(targetLocation.getServer());
            Bukkit.getScheduler().runTaskLater(SpacelifeCore.getInstance(), () -> {
                if (bukkitPlayer.isOnline()) {
                    bukkitPlayer.kick(Component.text("Technischer Fehler (Not teleported). Sollte dies öfter passieren, melde es dem Team!", NamedTextColor.RED));
                }
            }, 7 * 20);
            return true;
        });
    }

    public CrossServerLocation getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(CrossServerLocation location) {
        this.targetLocation = location;
        save();
    }

    /**
     * Get the enderchest for the according index. Index starts at 0.
     * 0 -> standard
     * 1 -> premium
     * 2 -> premium+
     * @param index index of chest
     * @return A map with int (inventory slot) as key and string (base64 of serialized item) as value
     */
    public Map<Integer, String> getEnderchest(int index){
        if(enderchests == null){
            return new HashMap<>();
        }
        var chest = enderchests.get(index);
        if(chest == null){
            return new HashMap<>();
        }
        return new HashMap<>(chest);
    }

    public void setEnderchest(int index, Map<Integer, String> map){
        if(getEnderchest(index).equals(map)){
            return;
        }
        if(enderchests == null){
            enderchests = new HashMap<>();
        }
        enderchests.put(index, map);
        save();
    }

    public List<Home> getHomes() {
        if (homes == null) {
            this.homes = new ArrayList<>();
        }
        return homes;
    }

    public void addHome(Home home) {
        if (this.homes == null) {
            this.homes = new ArrayList<>();
        }
        homes.add(home);
        save();
    }

    public void removeHome(Home home) {
        if (homes != null) {
            homes.remove(home);
            save();
        }
    }

    public void updateHome(Home home) {
        if (homes == null) {
            return;
        }
        Home oldHome = null;
        for (Home i : homes) {
            if (i.getUuid().equals(home.getUuid())) {
                oldHome = i;
            }
        }
        if (oldHome == null) {
            return;
        }
        if (oldHome.equals(home)) {
            return;
        }
        homes.set(homes.indexOf(oldHome), home);
        save();
    }
    public void toggleNightvision() {
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if(player == null){
            return;
        }
        hasNightvision = !hasNightvision;
        save().exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
        if(hasNightvision){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Nachtsicht aktiviert", NamedTextColor.GREEN));
            startNightNightvision();
            return;
        }
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Nachtsicht deaktiviert", NamedTextColor.RED));
        stopNightVision();
    }

    public boolean hasNightvision(){
        return hasNightvision;
    }

    protected void startNightNightvision() {
        if(nightvisionTaskID != -1){
            return;
        }
        nightvisionTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SpacelifeCore.getInstance(), () -> {
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if(player == null){
                Bukkit.getScheduler().cancelTask(nightvisionTaskID);
                nightvisionTaskID = -1;
                return;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20*30, 1, false, false, false));
        }, 1, 20*10);
    }

    protected void stopNightVision(){
        if(nightvisionTaskID == -1){
            return;
        }
        Bukkit.getScheduler().cancelTask(nightvisionTaskID);
        nightvisionTaskID = -1;
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if(player == null){
            return;
        }
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }

    public ObjectId getObjectId() {
        return id;
    }

    /**
     * Get the Job stats for each Job.
     * @return String = id, Jobstats = level,money,xp,etc
     */
    public HashMap<String, JobStats> getJobStats() {
        if(jobStats == null){
            jobStats = new HashMap<>();
        }
        return jobStats;
    }

    /**
     * Internal method for saving the changed data to mongoDB
     *
     * @return CompletableFuture with void. Completed when saved
     */
    private CompletableFuture<Void> save() {
        return SpacelifeDatabase.getInstance().save(this);
    }

    private String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

}
