package de.petropia.spacelifeCore.warp;

import dev.morphia.annotations.Entity;

import java.util.Date;

@Entity
public class WarpVisit {
    private String playerUUID;
    private Date visiteDate;

    public WarpVisit(String playerUUID, Date visiteDate){
        this.visiteDate = visiteDate;
        this.playerUUID = playerUUID;
    }

    /**
     * Morphia Constructor
     */
    private WarpVisit(){}

    public String getPlayerUUID() {
        return playerUUID;
    }

    public Date getVisitExpireDate() {
        return visiteDate;
    }
}
