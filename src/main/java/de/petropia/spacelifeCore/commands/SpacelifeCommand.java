package de.petropia.spacelifeCore.commands;

import com.destroystokyo.paper.profile.ProfileProperty;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.blockdata.PlayerPlacedBlock;
import de.petropia.spacelifeCore.blockdata.PlayerPlacedBlockManager;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifePlayerLoadingListener;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.spacelifeCore.warp.Warp;
import de.petropia.turtleServer.server.TurtleServer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class SpacelifeCommand implements CommandExecutor, TabCompleter {
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
        if(args[0].equalsIgnoreCase("addwarp") && sender.hasPermission("spacelife.command.spacelife.addwarp")){
            addWarpSubcommand(sender, args);
            return true;
        }
        if(args[0].equalsIgnoreCase("removewarp") && sender.hasPermission("spacelife.command.spacelife.removewarp")){
            removeWarpSubcommand(sender, args);
            return true;
        }
        if(args[0].equalsIgnoreCase("blockInfo") && sender.hasPermission("spacelife.command.spacelife.blockInfo")){
            blockInfoSubCommand(sender);
            return true;
        }
        return false;
    }

    private void blockInfoSubCommand(CommandSender sender) {
        if(!(sender instanceof Player player)){
            return;
        }
        Block lookBlock = player.getTargetBlock(Set.of(Material.AIR), 5);
        if(lookBlock.getType() == Material.AIR){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Bitte gucke einen Block an", NamedTextColor.RED));
            return;
        }
        if(!PlayerPlacedBlockManager.isBlockPlacedByPlayer(lookBlock)){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Dieser Block wurde nicht von einem Spieler plaziert", NamedTextColor.GREEN));
            return;
        }
        PlayerPlacedBlock playerPlacedBlock = PlayerPlacedBlockManager.getPlayerPlacedBlock(lookBlock);
        TurtleServer.getMongoDBHandler().getPetropiaPlayerByUUID(playerPlacedBlock.uuid().toString()).thenAccept(user -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("Europe/Berlin"));
            String date = formatter.format(playerPlacedBlock.placedDate());
            String playerName = user.getUserName();
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player,
                    Component.text("Spieler: ", NamedTextColor.GRAY)
                    .append(Component.text(playerName, NamedTextColor.GOLD))
                    .append(Component.text(" - Datum: ", NamedTextColor.GRAY))
                    .append(Component.text(date, NamedTextColor.GOLD)));
        });
    }

    private void removeWarpSubcommand(CommandSender sender, String[] args){
        if(args.length != 2){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Bitte gib einen Warp an"));
            return;
        }
        SpacelifeDatabase.getInstance().removeWarp(args[1]);
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Löschung durchgeführt. Beachte Groß- und Kleinschreibung beim Warpnamen", NamedTextColor.RED));
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
        SpacelifePlayer spacelifePlayer = SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId());
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
            SpacelifeDatabase.getInstance().getSpacelifePlayer(player.getUniqueId()).thenAccept(SpacelifePlayer::saveInventory).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
        if(args.length == 2 && args[1].equalsIgnoreCase("loadInv")){
            if(!(sender instanceof Player player)){
                return;
            }
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Lade Inventar", NamedTextColor.GRAY));
            SpacelifeDatabase.getInstance().getSpacelifePlayer(player.getUniqueId()).thenAccept(SpacelifePlayer::loadInventory).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
        if(args.length == 3 && args[1].equalsIgnoreCase("tpToPlayer")){
            if(!(sender instanceof Player player)){
                return;
            }
            SpacelifePlayer executor = SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId());
            executor.teleportCrossServer(new CrossServerLocation(args[2])).thenAccept(bool -> {
                if (bool) {
                    SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Sucess"));
                } else {
                    SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Fail"));
                }
            });
        }
    }

    private void addWarpSubcommand(CommandSender sender, String[] args){
        if(!(sender instanceof Player player)){
            return;
        }
        if(args.length != 4){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Nutzung: [Name] [Datum dd.MM.yyyy] [Uhrzeit hh:mm]"));
            return;
        }
        String name = args[1];
        for(Warp w : SpacelifeDatabase.getInstance().getWarps()){
            if(w.getName().equalsIgnoreCase(name)){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Dieser Warp existiert bereits", NamedTextColor.RED));
                return;
            }
        }
        Date date;
        try {
            date = new SimpleDateFormat("dd.MM.yyyy hh:mm").parse(args[2] + " " + args[3]);
        } catch (ParseException e) {
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Datum falsch formatiert!"));
            return;
        }
        String ownerUUID = player.getUniqueId().toString();
        String ownerName = player.getName();
        String ownerSkin = null;
        for(ProfileProperty profileProperty : player.getPlayerProfile().getProperties()){
            if(!profileProperty.getName().equalsIgnoreCase("textures")){
                continue;
            }
            ownerSkin = profileProperty.getValue();
            break;
        }
        if(ownerSkin == null){
            return;
        }
        CrossServerLocation location = new CrossServerLocation(TurtleServer.getInstance().getCloudNetAdapter().getServerInstanceName(), player.getLocation());
        Warp warp = new Warp(name, ownerUUID, ownerSkin, ownerName, location, date);
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Warp hinzugefügt!", NamedTextColor.GREEN));
        SpacelifeDatabase.getInstance().addWarp(warp);
    }

    private void jumpSubcommand(CommandSender sender, String[] args){
        if(args.length != 2){
            helpMessage(sender, "jump [Spieler]", "Springe zu einem Spieler");
        }
        if(!(sender instanceof Player bukkitPlayer)){
            return;
        }
        CloudPlayer player = SpacelifeCore.getInstance().getCloudNetAdapter().playerManagerInstance().firstOnlinePlayer(args[1]);
        if(player == null){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Der Spieler ist nicht online", NamedTextColor.RED));
            return;
        }
        CrossServerLocation location = new CrossServerLocation(player.uniqueId().toString());
        SpacelifeDatabase.getInstance().getCachedPlayer(bukkitPlayer.getUniqueId()).teleportCrossServer(location);
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
        if(sender.hasPermission("spacelife.command.spacelife.addwarp")){
            helpMessage(sender, "addwarp", "Erstellt einen Warp an deiner Stelle");
        }
        if(sender.hasPermission("spacelife.command.spacelife.removewarp")){
            helpMessage(sender, "removewarp", "Entfernt einen Warp");
        }
    }

    private void helpMessage(CommandSender sender, String subcommand, String description){
        sender.sendMessage(Component.text(">> ", NamedTextColor.GRAY)
                .append(Component.text(subcommand, NamedTextColor.GOLD))
                .append(Component.text(" - " + description, NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // Provide completions for the first argument
            if (sender.hasPermission("spacelife.command.spacelife.debug")) {
                completions.add("debug");
            }
            if (sender.hasPermission("spacelife.command.spacelife.jump")) {
                completions.add("jump");
            }
            if (sender.hasPermission("spacelife.command.spacelife.server")) {
                completions.add("server");
            }
            if (sender.hasPermission("spacelife.command.spacelife.addwarp")) {
                completions.add("addwarp");
            }
            if (sender.hasPermission("spacelife.command.spacelife.removewarp")) {
                completions.add("removewarp");
            }
            if (sender.hasPermission("spacelife.command.spacelife.blockInfo")) {
                completions.add("blockInfo");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            // Provide completions for the second argument of the "debug" subcommand
            completions.add("saveInv");
            completions.add("loadInv");
            completions.add("tpToPlayer");
        }
        // You can add more completions for other subcommands and arguments here
        return completions;
    }
}
