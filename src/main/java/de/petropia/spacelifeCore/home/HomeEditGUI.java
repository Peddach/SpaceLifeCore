package de.petropia.spacelifeCore.home;

import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.turtleServer.api.chatInput.ChatInputBuilder;
import de.petropia.turtleServer.server.TurtleServer;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class HomeEditGUI {

    private final Home oldHome;
    private final Home home;
    private final SpacelifePlayer spacelifePlayer;
    private final Player viewer;
    private Gui gui;

    public HomeEditGUI(Player viewer, Home home){
        this.home = new Home();
        this.home.setMaterial(home.getMaterial().name());
        this.home.setName(home.getName());
        this.home.setLocation(home.getLocation());
        this.oldHome = home;
        this.viewer = viewer;
        this.spacelifePlayer = SpacelifePlayerDatabase.getInstance().getCachedPlayer(viewer.getUniqueId());
        if(spacelifePlayer == null){
            return;
        }
        gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(home.getName(), NamedTextColor.DARK_GRAY).append(Component.text(" bearbeiten", NamedTextColor.RED)))
                .rows(2)
                .create();
        gui.setItem(4, createHomeItem());
        gui.setItem(9, createDeleteItem());
        gui.setItem(17, createSaveItem());
        gui.setItem(12, createRenameItem());
        gui.setItem(13, createPositionItem());
        gui.setItem(14, createIconItem());
        gui.open(viewer);
    }

    private GuiItem createHomeItem(){
        Component name = Component.text(home.getName()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
        return ItemBuilder.from(home.getMaterial())
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .name(name)
                .lore(Component.empty())
                .asGuiItem();
    }

    private GuiItem createDeleteItem(){
        Component name = Component.text("Home löschen", NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Component description = Component.text("Klicke um dein Home zu löschen!", NamedTextColor.GRAY);
        return ItemBuilder.from(Material.BARRIER)
                .name(name)
                .lore(
                        Component.empty(),
                        description,
                        Component.empty())
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    spacelifePlayer.removeHome(oldHome);
                    new HomeGUI(viewer);
                });
    }

    private GuiItem createSaveItem(){
        Component name = Component.text("Home speichern", NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
        Component description = Component.text("Speichere dein Home").color(NamedTextColor.GRAY);
        return ItemBuilder.from(Material.LIME_CONCRETE)
                .name(name)
                .lore(
                        Component.empty(),
                        description,
                        Component.empty())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    spacelifePlayer.updateHome(oldHome, home);
                    new HomeGUI(viewer);
                });
    }

    private GuiItem createRenameItem(){
        Component name = Component.text("Home umbennen", NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        Component description = Component.text("Benenne dein Home um", NamedTextColor.GRAY);
        return ItemBuilder.from(Material.NAME_TAG)
                .name(name)
                .lore(
                        Component.empty(),
                        description,
                        Component.empty())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    viewer.closeInventory();
                    new ChatInputBuilder(Component.text("Gib deinen neuen Namen in den Chat ein: ", NamedTextColor.GREEN), viewer)
                            .onCancel(() -> new HomeEditGUI(viewer, home))
                            .onInputWithString(string -> {
                                home.setName(string);
                                new HomeEditGUI(viewer, home);
                            })
                            .build();
                });
    }

    private GuiItem createPositionItem(){
        Component name = Component.text("Position ändern", NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        Component description = Component.text("Setze die Home Position auf deine aktuelle Position", NamedTextColor.GRAY);

        return ItemBuilder.from(Material.LIGHTNING_ROD)
                .name(name)
                .lore(
                        Component.empty(),
                        description,
                        Component.empty())
                .asGuiItem(event -> {
                   event.setCancelled(true);
                   home.setLocation(new CrossServerLocation(TurtleServer.getInstance().getCloudNetAdapter().getServerInstanceName(), viewer.getLocation()));
                   viewer.playSound(Sound.sound(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, Sound.Source.NEUTRAL, 1F, 1F));
                });
    }

    private GuiItem createIconItem(){
        Component name = Component.text("Icon ändern").color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        Component description = Component.text("Setze das Home icon zu dem Item in deiner Hand", NamedTextColor.GRAY);
        return ItemBuilder.from(Material.GLOW_ITEM_FRAME)
                .name(name)
                .lore(Component.empty(), description, Component.empty())
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if(viewer.getInventory().getItemInMainHand().getType() != Material.AIR){
                        home.setMaterial(viewer.getInventory().getItemInMainHand().getType().name());
                        gui.updateItem(4, createHomeItem());
                        return;
                    }
                    home.setMaterial(Material.PAPER.name());
                    gui.updateItem(4, createHomeItem());
                });
    }

}
