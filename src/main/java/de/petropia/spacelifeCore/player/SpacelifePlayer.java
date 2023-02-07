package de.petropia.spacelifeCore.player;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.spacelifeCore.teleport.CrossServerMessageListener;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Entity(value = "spacelifePlayers")
public class SpacelifePlayer {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String uuid;
    private double money;
    private Map<Integer, String> inventory;//Key for inv slot, String as base64 byte[] for serialized Item
    private CrossServerLocation targetLocation;
    /**
     * This Construtor is for manuel first time player creation
     * @param uuid The uuid of the minecraft player
     */
    public SpacelifePlayer(String uuid){
        this.uuid = uuid;
    }

    /**
     * Constructor for Morphia
     */
    public SpacelifePlayer(){

    }
    /**
     * Retrieve the uuid which is provided by Mojang
     * @return @{@link UUID} for the player
     */
    public UUID getUUID(){
        return UUID.fromString(uuid);
    }

    /**
     * Get the current money of a player
     * @return money as double
     */
    public double getMoney(){
        return money;
    }

    /**
     * Add money to a player
     * @param money money to add as double
     */
    public void addMoney(double money){
        this.money += money;
        save();
    }

    /**
     * Remove money from a player.
     * @param money money to remove as double
     * @return false when player has not enough money
     */
    public boolean subtractMoney(double money){
        if((this.money - money) < 0){
            return false;
        }
        this.money -= money;
        save();
        return true;
    }

    /**
     * Override all money from a player
     * @param money money to override as double
     */
    public void setMoney(double money){
        this.money = money;
        save();
    }

    /**
     * Saves player inventory and clears it. Player needs to be online!
     */
    public CompletableFuture<Void> saveInventory(){
        Map<Integer, String> newInventory = new HashMap<>();
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if(player == null){
            return CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException();
            });  //Need to be completed
        }
        for(int i = 0; i < player.getInventory().getContents().length; i++){
            ItemStack item = player.getInventory().getContents()[i];
            if(item == null){
               continue;
            }
            newInventory.put(i, toBase64(item.serializeAsBytes()));
        }
        player.getInventory().clear();
        if(newInventory.equals(inventory)){
            return CompletableFuture.supplyAsync(() -> null);
        }
        inventory = newInventory;
        return save();
    }

    /**
     * Clears a player's inventory and replaces it
     */
    public void loadInventory(){
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if(player == null){
            return;
        }
        if(inventory == null){
            return;
        }
        player.getInventory().clear();
        for(Map.Entry<Integer, String> indexEntry : inventory.entrySet()){
            int index = indexEntry.getKey();
            if(indexEntry.getValue() == null){
                player.getInventory().setItem(index, new ItemStack(Material.AIR));
                continue;
            }
            player.getInventory().setItem(index, ItemStack.deserializeBytes(fromBase64(indexEntry.getValue())));
        }
    }

    /**
     * Clear players Inventory on server (if online) and in DB
     */
    public void clearInventory(){
        inventory.clear();
        save();
        Player player = Bukkit.getPlayer(uuid);
        if(player != null){
            player.getInventory().clear();
        }
    }

    public CompletableFuture<Boolean> teleportCrossServer(CrossServerLocation location) {
        final Player bukkitPlayer = Bukkit.getPlayer(UUID.fromString(uuid));
        return CompletableFuture.supplyAsync(() -> {
            this.targetLocation = location;
            if (targetLocation.getPlayerUUID() != null || !targetLocation.getPlayerUUID().isEmpty()){
                targetLocation = CrossServerMessageListener.getForPlayer(location).join();
            }
            if(bukkitPlayer == null){
                return false;
            }
            if(targetLocation == null){
                return false;
            }
            if(targetLocation.getServer() == null || targetLocation.getServer().isEmpty()) {
                return false;
            }
            SpacelifePlayerLoadingListener.blockInvSave(bukkitPlayer);
            saveInventory().join();
            BlockAnyActionListener.blockPlayer(bukkitPlayer);
            CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class)
                    .getPlayerExecutor(UUID.fromString(uuid)).connect(targetLocation.getServer());
            Bukkit.getScheduler().runTaskLater(SpacelifeCore.getInstance(), () -> {
                if(bukkitPlayer.isOnline()){
                    bukkitPlayer.kick(Component.text("Technischer Fehler (Not teleported). Sollte dies öfter passieren, melde es dem Team!", NamedTextColor.RED));
                }
            },7*20);
            return true;
        });
    }

    public CrossServerLocation getTargetLocation(){
        return targetLocation;
    }

    public void setTargetLocation(CrossServerLocation location){
        this.targetLocation = location;
        save();
    }

    public ObjectId getObjectId(){
        return id;
    }

    /**
     * Internal method for saving the changed data to mongoDB
     * @return CompletableFuture with void. Completed when saved
     */
    private CompletableFuture<Void> save(){
        return SpacelifePlayerDatabase.getInstance().save(this);
    }

    private String toBase64(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] fromBase64(String base64){
        return Base64.getDecoder().decode(base64);
    }

}
