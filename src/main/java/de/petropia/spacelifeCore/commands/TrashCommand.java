package de.petropia.spacelifeCore.commands;

import de.petropia.spacelifeCore.SpacelifeCore;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TrashCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }
        if(!player.hasPermission("spacelife.command.trash")){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Kein Rechte", NamedTextColor.RED));
            return false;
        }
        Gui gui = Gui.gui()
                .title(Component.text("Mülleinmer"))
                .rows(3)
                .disableItemDrop()
                .create();
        gui.setItem(26, ItemBuilder.from(Material.BARRIER)
                .name(Component.text("Entgültig Löschen", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .asGuiItem(e -> {
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> gui.close(player, false));
                    player.playSound(Sound.sound(org.bukkit.Sound.ITEM_FIRECHARGE_USE, Sound.Source.NEUTRAL, 2F, 0.4F));
                }));
        gui.setCloseGuiAction(e -> {
            List<ItemStack> items = new ArrayList<>();
            for(int i = 0; i < 26; i++){
                ItemStack item = gui.getInventory().getItem(i);
                if(item == null || item.getType() == Material.AIR){
                    continue;
                }
                items.add(item);
            }
            player.getInventory().addItem(items.toArray(new ItemStack[0]));
        });
        gui.open(player);
        return false;
    }
}
