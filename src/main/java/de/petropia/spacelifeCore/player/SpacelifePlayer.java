package de.petropia.spacelifeCore.player;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.spacelifeCore.teleport.CrossServerMessageListener;
import dev.morphia.annotations.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
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
    private Map<Integer, String> inventory;//Key for inv slot, String as base64 byte[] for serialized Item
    private List<String> potions;
    private CrossServerLocation targetLocation;
    @Transient
    private int autoSaveTaskID = -1;

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
    public CompletableFuture<Void> saveInventory(){
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
        if(potions != null){
            for(String string : potions){
                try(ByteArrayInputStream inputStream = new ByteArrayInputStream(fromBase64(string));BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(inputStream)){
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
        food = 20;
        save().thenAccept(v -> Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                loadInventory();
            }
        }));
    }

    public CompletableFuture<Boolean> teleportCrossServer(CrossServerLocation location) {
        final Player bukkitPlayer = Bukkit.getPlayer(UUID.fromString(uuid));
        return CompletableFuture.supplyAsync(() -> {
            if (bukkitPlayer == null) {
                return false;
            }
            if(BlockAnyActionListener.isPlayerBlocked(bukkitPlayer)){
                return false;
            }
            this.targetLocation = location;
            if (targetLocation.getPlayerUUID() != null || !targetLocation.getPlayerUUID().isEmpty()) {
                if (targetLocation.getPlayerUUID().equalsIgnoreCase(uuid)) {
                    return null;
                }
                targetLocation = CrossServerMessageListener.getForPlayer(location).join();
            }
            if (targetLocation == null) {
                return false;
            }
            if (targetLocation.getServer() == null || targetLocation.getServer().isEmpty()) {
                return false;
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

    public ObjectId getObjectId() {
        return id;
    }

    /**
     * Internal method for saving the changed data to mongoDB
     *
     * @return CompletableFuture with void. Completed when saved
     */
    private CompletableFuture<Void> save() {
        return SpacelifePlayerDatabase.getInstance().save(this);
    }

    private String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

}
