package de.petropia.spacelifeCore.enderchest;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.turtleServer.server.TurtleServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EnderchestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.enderchest.self")){
            return false;
        }
        if(args.length == 0){
            new EnderchestGUI(player, player.getUniqueId(), true, null);
        }
        if(args.length != 1) {
            return false;
        }
        if(!player.hasPermission("spacelife.command.enderchest.other")){
            return false;
        }
        TurtleServer.getMongoDBHandler().getPetropiaPlayerByUsername(args[0]).thenAccept(petropiaPlayer -> {
            if (petropiaPlayer == null) {
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Der Spieler wurde nicht gefunden", NamedTextColor.RED));
                return;
            }
            Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> new EnderchestGUI(player, UUID.fromString(petropiaPlayer.getUuid()), false, null));
        }).exceptionally(e -> {
            e.printStackTrace();
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Es ist ein fehler aufgetreten!", NamedTextColor.RED));
            return null;
        });
        return false;
    }
}
