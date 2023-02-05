package de.petropia.spacelifeCore.player;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

import java.util.UUID;

@Entity(value = "spacelifePlayers")
public class SpacelifePlayer {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String uuid;
    private double money;

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

    public ObjectId getObjectId(){
        return id;
    }

    /**
     * Internal method for saving the changed data to mongoDB
     */
    private void save(){
        SpacelifePlayerDatabase.getInstance().save(this);
    }

}
