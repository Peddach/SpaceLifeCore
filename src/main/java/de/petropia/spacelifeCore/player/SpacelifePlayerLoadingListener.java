package de.petropia.spacelifeCore.player;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.turtleServer.server.TurtleServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class SpacelifePlayerLoadingListener implements Listener {

    private static final List<Player> INV_SAVE_BLOCK = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event){
        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED){
            return;
        }
        SpacelifeDatabase.getInstance().getSpacelifePlayer(event.getUniqueId()).thenAccept(spacelifePlayer -> {
            if(spacelifePlayer == null){
                spacelifePlayer = new SpacelifePlayer(event.getUniqueId().toString());
                spacelifePlayer.setMoney(100);
                SpacelifeCore.getInstance().getMessageUtil().showDebugMessage("New Player joined Spacelife: " + event.getUniqueId());
            }
            SpacelifeDatabase.getInstance().cachePlayer(spacelifePlayer);
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        event.joinMessage(null);
        SpacelifePlayer player = SpacelifeDatabase.getInstance().getCachedPlayer(event.getPlayer().getUniqueId());
        player.loadInventory();
        CrossServerLocation target = player.getTargetLocation();
        if(target == null){
            return;
        }
        if(!target.getServer().equalsIgnoreCase(TurtleServer.getInstance().getCloudNetAdapter().getServerInstanceName())){
            return;
        }
        event.getPlayer().teleport(player.getTargetLocation().convertToBukkitLocation());
        player.setTargetLocation(null);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        event.quitMessage(null);
        SpacelifePlayer player = SpacelifeDatabase.getInstance().getCachedPlayer(event.getPlayer().getUniqueId());
        if(!INV_SAVE_BLOCK.contains(event.getPlayer())){
            player.saveInventory().exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
            INV_SAVE_BLOCK.remove(event.getPlayer());
        }
        SpacelifeDatabase.getInstance().uncachePlayer(player);
    }

    public static void blockInvSave(Player player){
        INV_SAVE_BLOCK.add(player);
    }
}
