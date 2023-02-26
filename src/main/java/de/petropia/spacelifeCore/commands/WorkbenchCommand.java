package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorkbenchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.workbench")){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du benötigst mindestens den Premium Rang für diesen Command", NamedTextColor.RED));
            return false;
        }
        player.openWorkbench(null, true);
        return true;
    }
}
