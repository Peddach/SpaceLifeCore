package de.petropia.spacelifeCore.commands;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayerLoadingListener;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
        if(args[0].equalsIgnoreCase("jump") && sender.hasPermission("spacelife.command.spacelife.jump")){
            jumpSubcommand(sender, args);
            return true;
        }
        if(args[0].equalsIgnoreCase("server") && sender.hasPermission("spacelife.command.spacelife.server")){
            serverSubCommand(sender, args);
            return true;
        }
        return false;
    }

    private void serverSubCommand(CommandSender sender, String[] args){
        if(args.length == 1){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Bitte gib einen Server an!", NamedTextColor.RED));
            return;
        }
        if(args.length != 2){
            return;
        }
        if(!(sender instanceof Player player)) {
            return;
        }
        SpacelifePlayer spacelifePlayer = SpacelifePlayerDatabase.getInstance().getCachedPlayer(player.getUniqueId());
        BlockAnyActionListener.blockPlayer(player);
        SpacelifePlayerLoadingListener.blockInvSave(player);
        spacelifePlayer.saveInventory().thenAccept(v -> {
            Bukkit.getScheduler().runTaskLater(SpacelifeCore.getInstance(), () -> SpacelifeCore.getInstance().getCloudNetAdapter().sendPlayerToServer(player, args[1]), 20);
            Bukkit.getScheduler().runTaskLater(SpacelifeCore.getInstance(), () -> {
                if(player.isOnline()){
                    player.kick(Component.text("Etwas ist schief gelaufen!", NamedTextColor.RED));
                }
            }, 5*20);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
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
        if(sender.hasPermission("spacelife.command.spacelife.jump")){
            helpMessage(sender, "jump", "Springe einem Spieler auf SL hinterher");
        }
        if(sender.hasPermission("spacelife.command.spacelife.server")){
            helpMessage(sender, "server", "Teleportiere dich auf einen Server");
        }
    }

    private void jumpSubcommand(CommandSender sender, String[] args){
        if(args.length != 2){
            helpMessage(sender, "jump [Spieler]", "Springe zu einem Spieler");
        }
        if(!(sender instanceof Player bukkitPlayer)){
            return;
        }
        ICloudPlayer player =  CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getFirstOnlinePlayer(args[1]);
        if(player == null){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Der Spieler ist nicht online", NamedTextColor.RED));
            return;
        }
        CrossServerLocation location = new CrossServerLocation(player.getUniqueId().toString());
        SpacelifePlayerDatabase.getInstance().getCachedPlayer(bukkitPlayer.getUniqueId()).teleportCrossServer(location);
    }

    private void helpMessage(CommandSender sender, String subcommand, String description){
        sender.sendMessage(Component.text(">> ", NamedTextColor.GRAY)
                .append(Component.text(subcommand, NamedTextColor.GOLD))
                .append(Component.text(" - " + description, NamedTextColor.GRAY)));
    }
}
