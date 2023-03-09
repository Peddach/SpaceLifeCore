package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.teleport.StaticTeleportPoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.spawn")){
            return false;
        }
        SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId()).teleportCrossServer(StaticTeleportPoints.SPAWN).thenAccept(bool -> {
            if(!bool){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Etwas ist schief gelaufen!", NamedTextColor.RED));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
        return true;
    }
}
