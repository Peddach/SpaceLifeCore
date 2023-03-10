package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NightVisionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof  Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.nightvision")){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du benötigst mindestens Premium+ für Nachtsicht!", NamedTextColor.RED));
            return false;
        }
        SpacelifePlayer spacelifePlayer = SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId());
        if(spacelifePlayer == null){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Interner Fehler!", NamedTextColor.RED));
            return false;
        }
        spacelifePlayer.toggleNightvision();
        return true;
    }
}
