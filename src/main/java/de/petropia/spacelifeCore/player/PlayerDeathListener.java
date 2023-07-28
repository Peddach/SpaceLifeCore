package de.petropia.spacelifeCore.player;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.teleport.StaticTeleportPoints;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Bukkit.getScheduler().runTaskLater(SpacelifeCore.getInstance(), () -> {
            event.getPlayer().spigot().respawn();
            SpacelifePlayer player = SpacelifeDatabase.getInstance().getCachedPlayer(event.getPlayer().getUniqueId());
            player.teleportCrossServer(StaticTeleportPoints.SPAWN);
        }, 1);
    }
}
