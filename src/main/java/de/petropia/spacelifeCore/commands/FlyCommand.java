package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.fly")){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du benötigst einen höheren Rang auf dieser Welt für diesen Command", NamedTextColor.RED));
            return false;
        }
        player.setAllowFlight(!player.getAllowFlight());
        if(player.getAllowFlight()){
            player.playSound(Sound.sound(org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, Sound.Source.NEUTRAL, 1, 1));
            player.setFlying(true);
        }
        else {
            player.playSound(Sound.sound(org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, Sound.Source.NEUTRAL, 1, 1));
        }
        return false;
    }
}
