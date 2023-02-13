package de.petropia.spacelifeCore.teleport;

import de.petropia.spacelifeCore.SpacelifeCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockAnyActionListener implements Listener {

    private static final List<Player> BLOCKED_PLAYERS = new ArrayList<>();
    private static final Component MESSAGE = Component.text("Du wirst gleich teleportiert! Bitte gedulde dich kurz. Sollte dies nicht passieren rejoin!", NamedTextColor.GRAY);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickUpItem(EntityPickupItemEvent event){
        if(!(event.getEntity() instanceof Player player)){
            return;
        }
        if(BLOCKED_PLAYERS.contains(player)){
            event.setCancelled(true);
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, MESSAGE);
        }
    }

    @EventHandler
    public void onInvOpen(InventoryOpenEvent event){
        if(BLOCKED_PLAYERS.contains((Player) event.getPlayer())){
            event.setCancelled(true);
            SpacelifeCore.getInstance().getMessageUtil().sendMessage((Player) event.getPlayer(), MESSAGE);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(BLOCKED_PLAYERS.contains((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event){
        BLOCKED_PLAYERS.remove(event.getPlayer());
    }

    /**
     * Block any inventory interaction until player disconnect bc of item loss, when inv is saved, but player not teleported
     * @param player Player to block
     */
    public static void blockPlayer(Player player){
        BLOCKED_PLAYERS.add(player);
    }

    public static boolean isPlayerBlocked(Player player){
        return (BLOCKED_PLAYERS.contains(player));
    }
}
