package de.petropia.spacelifeCore.player;

import de.petropia.spacelifeCore.SpacelifeCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpacelifePlayerLoadingListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event){
        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED){
            return;
        }
        SpacelifePlayerDatabase.getInstance().getSpacelifePlayer(event.getUniqueId()).thenAccept(spacelifePlayer -> {
            if(spacelifePlayer == null){
                spacelifePlayer = new SpacelifePlayer(event.getUniqueId().toString());
                spacelifePlayer.setMoney(100);
                SpacelifeCore.getInstance().getMessageUtil().showDebugMessage("New Player joined Spacelife: " + event.getUniqueId());
            }
            SpacelifePlayerDatabase.getInstance().cachePlayer(spacelifePlayer);
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        SpacelifePlayer player = SpacelifePlayerDatabase.getInstance().getCachedPlayer(event.getPlayer().getUniqueId());
        SpacelifePlayerDatabase.getInstance().uncachePlayer(player);
    }


}
