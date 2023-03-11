package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.hat")){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du benötigst mindestens Premium für diesen Befehl!", NamedTextColor.RED));
            return false;
        }
        if(player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.AIR){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du hast bereits einen Hut auf!", NamedTextColor.RED));
            return false;
        }
        player.getInventory().getItemInMainHand();
        if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Halte ein Item in der Hand, welches dein Hut werden soll", NamedTextColor.RED));
            return false;
        }
        ItemStack hat = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        player.getInventory().setHelmet(hat);
        SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Dein Hut wurde dir erfolgreich aufgesetzt", NamedTextColor.GREEN));
        return true;
    }
}
