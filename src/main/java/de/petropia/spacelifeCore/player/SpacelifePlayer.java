package de.petropia.spacelifeCore.player;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity(value = "spacelifePlayers")
public class SpacelifePlayer {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String uuid;
    private double money;
    private Map<Integer, String> inventory;//Key for inv slot, String as base64 byte[] for serialized Item

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
     * Saves player inventory and clears it
     */
    public void saveInventory(){
        Map<Integer, String> newInventory = new HashMap<>();
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if(player == null){
            return;
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
            return;
        }
        inventory = newInventory;
        save();
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

    public ObjectId getObjectId(){
        return id;
    }

    /**
     * Internal method for saving the changed data to mongoDB
     */
    private void save(){
        SpacelifePlayerDatabase.getInstance().save(this);
    }

    private String toBase64(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] fromBase64(String base64){
        return Base64.getDecoder().decode(base64);
    }

}
