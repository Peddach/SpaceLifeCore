package de.petropia.spacelifeCore.warp;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Entity
public class Warp {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String name;
    private CrossServerLocation location;
    private String ownerUUID;
    private String ownerSkin;
    private String ownerName;
    private Date expireDate;
    private List<WarpVisit> visits;
    public Warp(String name, String ownerUUID, String ownerSkin, String ownerName,  CrossServerLocation location, Date expireDate){
        this.name = name;
        this.ownerName = ownerName;
        this.ownerSkin = ownerSkin;
        this.ownerUUID = ownerUUID;
        this.location = location;
        visits = new ArrayList<>();
        this.expireDate = expireDate;
    }
    public Warp(){}

    public void teleport(Player player){
        SpacelifePlayer spacelifePlayer = SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId());
        if(spacelifePlayer == null){
            return;
        }
        if(expireDate.toInstant().getEpochSecond() < Instant.now().getEpochSecond()){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Dieser Warp ist abgelaufen!", NamedTextColor.RED));
            return;
        }
        spacelifePlayer.teleportCrossServer(location);
        if(visits == null){
            visits = new ArrayList<>();
        }
        for(WarpVisit visit : visits){
            if(visit.getPlayerUUID().equals(player.getUniqueId().toString())){
                return;
            }
        }
        fetch().thenAccept(v -> {
            if(visits == null){
                visits = new ArrayList<>();
            }
            visits.add(new WarpVisit(player.getUniqueId().toString(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS))));
            save();
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

    }

    public List<WarpVisit> getWarpVisitList(){
        if(visits == null){
            visits = new ArrayList<>();
        }
        return visits;
    }

    public Date getExpireDate(){
        return expireDate;
    }
    public String getName() {
        return name;
    }

    public void setLocation(CrossServerLocation location){
        this.location = location;
    }

    public void updateLocation(CrossServerLocation location){
        fetch().thenAccept(v -> {
            this.location = location;
            save();
        });
    }

    public String getOwnerName(){
        return ownerName;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerSkin() {
        return ownerSkin;
    }

    public int getVisits() {
        if(visits == null){
            return 0;
        }
        return visits.size();
    }

    private CompletableFuture<Void> fetch(){
        return CompletableFuture.supplyAsync(() -> {
            Warp warp = SpacelifeDatabase.getInstance().queryWarpFromDB(name).join();
            if(warp == null){
                throw new RuntimeException("Warp not in db");
            }
            this.expireDate = warp.expireDate;
            this.location = warp.location;
            this.name = warp.name;
            this.ownerSkin = warp.ownerSkin;
            this.ownerUUID = warp.ownerUUID;
            this.visits = warp.visits;
            this.ownerName = warp.ownerName;
            return null;
        });
    }

    private void save(){
        SpacelifeDatabase.getInstance().updateWarp(this);
    }

}
