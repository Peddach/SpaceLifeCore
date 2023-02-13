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
        SpacelifePlayer payer = SpacelifePlayerDatabase.getInstance().getCachedPlayer(player.getUniqueId());
        double finalAmount = amount;
        TurtleServer.getMongoDBHandler().getPetropiaPlayerByUsername(args[0]).thenAccept(petropiaPlayer -> {
            if(petropiaPlayer == null){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Der Spieler konnte nicht gefunden werden!", NamedTextColor.GRAY));
                return;
            }
            SpacelifePlayer target = SpacelifePlayerDatabase.getInstance().getSpacelifePlayer(UUID.fromString(petropiaPlayer.getUuid())).join();
            if(!payer.subtractMoney(finalAmount)){
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du hast nicht genügend Geld!"));
                return;
            }
            //TODO check if target is online
            //TODO Yes -> send message to server
            //TODO No -> Add and save
        });

        return false;
    }
}
