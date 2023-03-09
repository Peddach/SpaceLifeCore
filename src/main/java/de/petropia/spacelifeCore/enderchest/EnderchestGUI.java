package de.petropia.spacelifeCore.enderchest;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.StorageGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnderchestGUI {

    private final boolean editable;
    private int currentChest;
    private final Player viewer;
    private SpacelifePlayer spacelifePlayer;
    private StorageGui gui;

    public EnderchestGUI(Player viewer, UUID target, boolean editable, Runnable onClose) {
        this.viewer = viewer;
        this.editable = editable;
        SpacelifeDatabase.getInstance().getSpacelifePlayer(target).thenAccept(fetchedPlayer -> Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> {
            if (fetchedPlayer == null) {
                SpacelifeCore.getInstance().getMessageUtil().sendMessage(viewer, Component.text("Profil nicht gefunden", NamedTextColor.RED));
                return;
            }
            this.spacelifePlayer = fetchedPlayer;
            currentChest = 0;
            var builder = Gui.storage()
                    .title(Component.text("Enderchest", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                    .rows(4);
            if (!editable) {
                builder.disableAllInteractions();
            }
            gui = builder.create();
            gui.setItem(29, createStandardEnderchest());
            gui.setItem(31, createPremiumEnderchest());
            gui.setItem(33, createPremiumPlusEnderchest());
            gui.setItem(27, createDummyItem());
            gui.setItem(28, createDummyItem());
            gui.setItem(30, createDummyItem());
            gui.setItem(32, createDummyItem());
            gui.setItem(34, createDummyItem());
            gui.setItem(35, createDummyItem());
            gui.setCloseGuiAction(action -> {
                if(editable){
                    saveChest();
                }
                if(onClose != null){
                    onClose.run();
                }
            });
            renderChest();
            gui.open(viewer);
        }));

    }

    private GuiItem createDummyItem() {
        return ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.empty())
                .asGuiItem(event -> event.setCancelled(true));
    }

    private GuiItem createStandardEnderchest() {
        Component name = Component.text("Standard Enderchest", NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        Component lore = Component.empty();
        if (!viewer.hasPermission("spacelife.enderchest.standard")) {
            lore = Component.text("Du benötigst einen höheren Rang für die Enderchest!", NamedTextColor.RED);
            name = name.decorate(TextDecoration.STRIKETHROUGH);
        }
        return ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdhYWRmZjlkZGM1NDZmZGNlYzZlZDU5MTljYzM5ZGZhOGQwYzA3ZmY0YmM2MTNhMTlmMmU2ZDdmMjU5MyJ9fX0=")
                .name(name)
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (!viewer.hasPermission("spacelife.enderchest.standard")) {
                        return;
                    }
                    if (currentChest == 0) {
                        return;
                    }
                    saveChest();
                    currentChest = 0;
                    renderChest();
                });
    }

    private GuiItem createPremiumEnderchest() {
        Component name = Component.text("Premium Enderchest", NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
        Component lore = Component.empty();
        if (!viewer.hasPermission("spacelife.enderchest.premium")) {
            lore = Component.text("Du benötigst mindestens den Premium Rang für diese Enderchest!", NamedTextColor.RED);
            name = name.decorate(TextDecoration.STRIKETHROUGH);
        }
        return ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM3Y2FlNWM1MWViMTU1OGVhODI4ZjU4ZTBkZmY4ZTZiN2IwYjFhMTgzZDczN2VlY2Y3MTQ2NjE3NjEifX19")
                .name(name)
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (!viewer.hasPermission("spacelife.enderchest.premium")) {
                        return;
                    }
                    if (currentChest == 1) {
                        return;
                    }
                    saveChest();
                    currentChest = 1;
                    renderChest();
                });
    }

    private GuiItem createPremiumPlusEnderchest() {
        Component name = Component.text("Premium+ Enderchest", NamedTextColor.DARK_GREEN).decorate(TextDecoration.BOLD);
        Component lore = Component.empty();
        if (!viewer.hasPermission("spacelife.enderchest.premiumplus")) {
            lore = Component.text("Du benötigst mindestens den Premium+ Rang für diese Enderchest!", NamedTextColor.RED);
            name = name.decorate(TextDecoration.STRIKETHROUGH);
        }
        return ItemBuilder.skull()
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU4MDdjYzRjM2I2OTU4YWVhNjE1NmU4NDUxOGQ5MWE0OWM1ZjMyOTcxZTZlYjI2OWEyM2EyNWEyNzE0NSJ9fX0=")
                .name(name)
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (!viewer.hasPermission("spacelife.enderchest.premiumplus")) {
                        return;
                    }
                    if (currentChest == 2) {
                        return;
                    }
                    saveChest();
                    currentChest = 2;
                    renderChest();
                });
    }

    private void saveChest() {
        if (!editable) {
            return;
        }
        Map<Integer, String> inventory = new HashMap<>();
        for (int i = 0; i < 3 * 9; i++) {
            ItemStack item = gui.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            inventory.put(i, toBase64(item.serializeAsBytes()));
        }
        spacelifePlayer.setEnderchest(currentChest, inventory);
    }

    private void renderChest() {
        var chest = spacelifePlayer.getEnderchest(currentChest);
        for (int i = 0; i < 3 * 9; i++) {
            gui.removeItem(i);
        }
        for (Map.Entry<Integer, String> entry : chest.entrySet()) {
            gui.getInventory().setItem(entry.getKey(), ItemStack.deserializeBytes(fromBase64(entry.getValue())));
        }
    }

    private String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
