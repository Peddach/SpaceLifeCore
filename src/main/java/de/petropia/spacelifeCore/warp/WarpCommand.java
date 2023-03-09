package de.petropia.spacelifeCore.warp;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.warp")) {
            return false;
        }
        if(args.length == 0) {
            new WarpGUI(player);
            return true;
        }
        Warp warp = SpacelifeDatabase.getInstance().getWarp(args[0]);
        if(warp == null){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Der Warp existiert nicht!", NamedTextColor.RED));
            return false;
        }
        warp.teleport(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> warps = new ArrayList<>();
        SpacelifeDatabase.getInstance().getWarps().forEach(w -> warps.add(w.getName()));
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], warps, new ArrayList<>());
        }
        return null;
    }
}
