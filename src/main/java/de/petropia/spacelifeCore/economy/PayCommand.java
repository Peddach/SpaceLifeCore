package de.petropia.spacelifeCore.economy;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.economy.dto.PlayerPayDTO;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.turtleServer.server.TurtleServer;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class PayCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.pay")){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du hast keine Rechte dazu", NamedTextColor.RED));
            return false;
        }
        if(args.length != 2){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Bitte gib einen Spieler und danach den Betrag ein", NamedTextColor.RED));
            return false;
        }
        double amount;
        try{
            amount = Double.parseDouble(args[1]);   //String -> Double
            amount = Math.round(amount * 100.0) / 100.0;    //Multiply by 100 to shift decimal 2 right -> round -> shift back
            if(amount <= 0){    //Check if positive
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Bitte gib eine positive Zahl mit max. 2 Nachkommastellen an!", NamedTextColor.RED));
            return false;
        }
        if(player.getName().equalsIgnoreCase(args[0])){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du musst jemand anderen Geld überweisen ;-D"));
            return false;
        }
        SpacelifePlayer payer = SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId());
        final double finalAmount = amount;
        TurtleServer.getMongoDBHandler().getPetropiaPlayerByUsername(args[0]).thenAccept(petropiaPlayer -> {
            if(petropiaPlayer == null){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Der Spieler konnte nicht gefunden werden!", NamedTextColor.GRAY));
                return;
            }
            SpacelifePlayer target = SpacelifeDatabase.getInstance().getSpacelifePlayer(UUID.fromString(petropiaPlayer.getUuid())).join();
            if(!payer.subtractMoney(finalAmount)){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du hast nicht genügend Geld!"));
                return;
            }
            player.sendActionBar(Component.text("-", NamedTextColor.RED).decorate(TextDecoration.BOLD).append(Component.text(finalAmount, NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text(finalAmount + "$", NamedTextColor.GOLD).append(Component.text(" wurden an ", NamedTextColor.GRAY)).append(Component.text(petropiaPlayer.getUserName(), NamedTextColor.GOLD)).append(Component.text(" gezahl", NamedTextColor.GRAY)));
            CloudPlayer cloudPlayerTarget = SpacelifeCore.getInstance().getCloudNetAdapter().playerManagerInstance().onlinePlayer(target.getUUID());
            if(cloudPlayerTarget == null){
                target.addMoney(finalAmount);
                return;
            }
            Set<String> groups = cloudPlayerTarget.connectedService().groups();
            if(!groups.contains("SpaceLife")){
                target.addMoney(finalAmount);
                return;
            }
            PlayerPayDTO dto = new PlayerPayDTO(
                    target.getUUID(),
                    finalAmount,
                    player.getName());
            ChannelMessage message = ChannelMessage.builder()
                    .channel("spacelife_pay")
                    .buffer(DataBuf.empty().writeObject(dto))
                    .targetService(cloudPlayerTarget.connectedService().serviceId().name())
                    .build();
            message.send();
        });
        return false;
    }

    @EventListener
    public void onPayMessageReceive(ChannelMessageReceiveEvent event){
        if(event.query()){
            return;
        }
        ChannelMessage message = event.channelMessage();
        if(!message.channel().equalsIgnoreCase("spacelife_pay")){
            return;
        }
        PlayerPayDTO dto = message.content().readObject(PlayerPayDTO.class);
        UUID playerUUID = dto.targetUUID();
        double amount = dto.amount();
        String payer = dto.payer();
        Player player = Bukkit.getPlayer(playerUUID);
        if(player == null){
            return;
        }
        SpacelifePlayer target = SpacelifeDatabase.getInstance().getCachedPlayer(playerUUID);
        target.addMoney(amount);
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text(payer, NamedTextColor.GOLD).append(Component.text(" hat dir ", NamedTextColor.GRAY).append(Component.text(amount + "$ ", NamedTextColor.GOLD).append(Component.text("überwiesen", NamedTextColor.GRAY)))));
        player.sendActionBar(Component.text("+", NamedTextColor.GREEN).decorate(TextDecoration.BOLD).append(Component.text(amount, NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
    }
}
