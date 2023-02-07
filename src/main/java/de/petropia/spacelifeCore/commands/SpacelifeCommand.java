package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SpacelifeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0){
            showHelp(sender);
            return true;
        }
        if(args[0].equalsIgnoreCase("debug") && sender.hasPermission("spacelife.command.spacelife.debug")){
            debugSubcommand(sender, args);
            return true;
        }
        return false;
    }

    private void debugSubcommand(CommandSender sender, String[] args){
        if(args.length == 1){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Bitte gib einen der folgenden Optionen an", NamedTextColor.RED));
            helpMessage(sender, "debug saveInv", "Speichtert Inventar in DB");
            helpMessage(sender, "debug loadInv", "Lädt Inventar aus DB");
            return;
        }
        if(args.length == 2 && args[1].equalsIgnoreCase("saveInv")){
            if(!(sender instanceof Player player)){
                return;
            }
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Speichere Inventar", NamedTextColor.GRAY));
            SpacelifePlayerDatabase.getInstance().getSpacelifePlayer(player.getUniqueId()).thenAccept(SpacelifePlayer::saveInventory).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
        if(args.length == 2 && args[1].equalsIgnoreCase("loadInv")){
            if(!(sender instanceof Player player)){
                return;
            }
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Lade Inventar", NamedTextColor.GRAY));
            SpacelifePlayerDatabase.getInstance().getSpacelifePlayer(player.getUniqueId()).thenAccept(SpacelifePlayer::loadInventory).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
        if(args.length == 3 && args[1].equalsIgnoreCase("tpToPlayer")){
            if(!(sender instanceof Player player)){
                return;
            }
            SpacelifePlayer executor = SpacelifePlayerDatabase.getInstance().getCachedPlayer(player.getUniqueId());
            executor.teleportCrossServer(new CrossServerLocation(args[2])).thenAccept(bool -> {
                if (bool) {
                    SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Sucess"));
                } else {
                    SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Fail"));
                }
            });
        }
    }

    private void showHelp(CommandSender sender){
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Es gibt folgende Subcommands: ", NamedTextColor.GRAY));
        if(sender.hasPermission("spacelife.command.spacelife.debug")){
            helpMessage(sender, "debug", "Hilft beim debuggen");
        }
    }

    private void helpMessage(CommandSender sender, String subcommand, String description){
        sender.sendMessage(Component.text(">> ", NamedTextColor.GRAY)
                .append(Component.text(subcommand, NamedTextColor.GOLD))
                .append(Component.text(" - " + description, NamedTextColor.GRAY)));
    }
}
