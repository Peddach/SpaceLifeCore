package de.petropia.spacelifeCore.teleport;

import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.channel.ChannelMessage;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.channel.ChannelMessageReceiveEvent;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TpaCommand implements CommandExecutor {

    private static final HashMap<UUID, TpaRequest> REQUEST_MAP = new HashMap<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!player.hasPermission("spacelife.command.tpa")) {
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du hast keine Rechte auf diesen Command", NamedTextColor.RED));
            return false;
        }
        if (args.length == 1) {
            sendRequest(player, args[0]);
            return true;
        }
        if (args.length == 2){
            if(args[0].equalsIgnoreCase("accept")){
                sendResponse(args[1], true, player);
                return true;
            }
            if(args[0].equalsIgnoreCase("deny")){
                sendResponse(args[1], false, player);
                return true;
            }
        }
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Bitte gib einen Spieler an", NamedTextColor.RED));
        return false;
    }

    private void sendResponse(String id, boolean accept, Player sender){
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Die Anfrage ist ungültig", NamedTextColor.RED));
            return;
        }
        TpaRequest request = REQUEST_MAP.get(uuid);
        if(request == null){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(sender, Component.text("Die Anfrage ist abgelaufen", NamedTextColor.RED));
            return;
        }
        if(!request.targetUUID().equalsIgnoreCase(sender.getUniqueId().toString())){
            return;
        }
        REQUEST_MAP.remove(request.requestID());
        ChannelMessage.builder()
                .channel("spacelife_tpa_accept")
                .message("spacelife_tpa_accept")
                .json(JsonDocument.newDocument()
                        .append("requesterUUID", request.requesterUUID())
                        .append("targetName", sender.getName())
                        .append("targetUUID", request.targetUUID())
                        .append("accept", accept))
                .targetService(request.serviceID())
                .build()
                .send();
    }

    private void sendRequest(Player player, String targetName){
        CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getFirstOnlinePlayerAsync(targetName).onComplete(target -> {
            if (target == null) {
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Der Spieler konnte nicht gefunden werden", NamedTextColor.RED));
                return;
            }
            List<String> groups = List.of(target.getConnectedService().getGroups());
            if(!groups.contains("SpaceLife")){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Dieser Spieler ist nicht auf Spacelife!", NamedTextColor.RED));
                return;
            }
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du hast erfolgreich eine Teleportanfrage an ", NamedTextColor.GRAY)
                .append(Component.text(targetName, NamedTextColor.GOLD))
                .append(Component.text(" gestellt", NamedTextColor.GRAY)));
            ChannelMessage.builder()
                    .targetService(target.getConnectedService().getServiceId().getName())
                    .message("spacelife_tpa_request")
                    .json(JsonDocument.newDocument()
                            .append("playerUUID", player.getUniqueId().toString())
                            .append("playerName", player.getName())
                            .append("targetUUID", target.getUniqueId().toString()))
                    .channel("spacelife_tpa_request")
                    .build()
                    .send();
        });
    }

    @EventListener
    public void onAccept(ChannelMessageReceiveEvent event){
        if(!event.getChannel().equalsIgnoreCase("spacelife_tpa_accept")){
            return;
        }
        if(event.getChannelMessage().getJson().isEmpty()){
            return;
        }
        String requesterUUID = event.getChannelMessage().getJson().getString("requesterUUID");
        String targetUUID = event.getChannelMessage().getJson().getString("targetUUID");
        String targetName = event.getChannelMessage().getJson().getString("targetName");
        boolean accepted = event.getChannelMessage().getJson().getBoolean("accept");
        if(requesterUUID == null || targetUUID == null || targetName == null){
            return;
        }
        Player requester = Bukkit.getPlayer(UUID.fromString(requesterUUID));
        if(requester == null){
            return;
        }
        if(!accepted){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(requester, Component.text(targetName, NamedTextColor.GOLD).append(Component.text(" hat deine Teleportanfrage ", NamedTextColor.GRAY)).append(Component.text("abgelehnt", NamedTextColor.RED)));
            return;
        }
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(requester, Component.text(targetName, NamedTextColor.GOLD).append(Component.text(" hat deine Teleportanfrage angenommen", NamedTextColor.GRAY)));
        SpacelifePlayer spacelifePlayer = SpacelifeDatabase.getInstance().getCachedPlayer(requester.getUniqueId());
        spacelifePlayer.teleportCrossServer(new CrossServerLocation(targetUUID));
    }

    @EventListener
    public void onRequest(ChannelMessageReceiveEvent event) {
        if (!event.getChannel().equalsIgnoreCase("spacelife_tpa_request")) {
            return;
        }
        if (event.getChannelMessage().getJson().isEmpty()) {
            return;
        }
        String userUUID = event.getChannelMessage().getJson().getString("playerUUID");
        String userName = event.getChannelMessage().getJson().getString("playerName");
        String targetUUID = event.getChannelMessage().getJson().getString("targetUUID");
        if (targetUUID == null || userUUID == null || userName == null || targetUUID.isEmpty() || userUUID.isEmpty() || userName.isEmpty()) {
            return;
        }
        TpaRequest request = new TpaRequest(UUID.randomUUID(), userUUID, userName, targetUUID, event.getChannelMessage().getSender().getName());
        REQUEST_MAP.put(request.requestID(), request);
        Player target = Bukkit.getPlayer(UUID.fromString(targetUUID));
        if(target == null){
            return;
        }
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(target, Component.text(userName, NamedTextColor.GOLD)
                .append(Component.text(" hat dir eine Teleportanfrage gesendet. ", NamedTextColor.GRAY))
                .append(Component.text("[Annehmen] ", NamedTextColor.DARK_GREEN).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/tpa accept " + request.requestID().toString())))
                .append(Component.text("[Ablehnen] ", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/tpa deny " + request.requestID()))));
        Bukkit.getScheduler().runTaskLater(SpacelifeCore.getInstance(), () -> REQUEST_MAP.remove(request.requestID()), 10*20);
    }
}
