package de.petropia.spacelifeCore.economy;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.turtleServer.server.TurtleServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BalanceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("spacelife.command.balence")) {
            return false;
        }
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length == 0) {
            SpacelifePlayer spacelifePlayer = SpacelifePlayerDatabase.getInstance().getCachedPlayer(player.getUniqueId());
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Dein Kontostand beträgt: ", NamedTextColor.GRAY).append(Component.text(spacelifePlayer.getMoney() + "$", NamedTextColor.GOLD)));
            return true;
        }
        if (!player.hasPermission("spacelife.command.balance.other")){
            return false;
        }
        TurtleServer.getMongoDBHandler().getPetropiaPlayerByUsername(args[0]).thenAccept(petropiaPlayer -> {
            if(petropiaPlayer == null){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Spieler wurde nicht gefunden!"));
                return;
            }
            SpacelifePlayerDatabase.getInstance().getSpacelifePlayer(UUID.fromString(petropiaPlayer.getUuid())).thenAccept(target -> {
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text(petropiaPlayer.getUserName(), NamedTextColor.GOLD)
                        .append(Component.text(" hat einen Kontostand von ", NamedTextColor.GRAY))
                        .append(Component.text(target.getMoney() + "$")));
            });
        });
        return false;
    }
}
