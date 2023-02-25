package de.petropia.spacelifeCore.home;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifePlayer;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.spacelifeCore.teleport.CrossServerLocation;
import de.petropia.turtleServer.api.chatInput.ChatInputBuilder;
import de.petropia.turtleServer.server.TurtleServer;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public class HomeGUI {
    private final Player viewer;
    private final SpacelifePlayer spacelifePlayer;

    public HomeGUI(Player player){
        this.viewer = player;
        spacelifePlayer = SpacelifePlayerDatabase.getInstance().getCachedPlayer(player.getUniqueId());
        if(spacelifePlayer == null){
            SpacelifeCore.getInstance().getMessageUtil().sendMessage(viewer, Component.text("Etwas ist schief gelaufen (SL_Player is null)", NamedTextColor.RED));
            return;
        }
        List<Home> homes = spacelifePlayer.getHomes();
        Gui gui = Gui.gui()
                .rows(4)
                .disableAllInteractions()
                .title(Component.text("Deine Homes", NamedTextColor.DARK_GRAY))
                .create();
        gui.setItem(13, createAddItem());
        if(homes.size() == 0){
            gui.setItem(22, ItemBuilder.from(Material.BARRIER).name(Component.text("Keine Homes vorhanden", NamedTextColor.RED)).asGuiItem());
            gui.open(viewer);
            return;
        }
        int numItems = homes.size();
        if (numItems == 1) {
            gui.setItem(22, createHomeItem(homes.get(0)));
        } else if (numItems == 2) {
            gui.setItem(21, createHomeItem(homes.get(0)));
            gui.setItem(23, createHomeItem(homes.get(1)));
        } else if (numItems == 3) {
            gui.setItem(21, createHomeItem(homes.get(0)));
            gui.setItem(22, createHomeItem(homes.get(1)));
            gui.setItem(23, createHomeItem(homes.get(2)));
        } else if (numItems == 4) {
            gui.setItem(20, createHomeItem(homes.get(0)));
            gui.setItem(21, createHomeItem(homes.get(1)));
            gui.setItem(23, createHomeItem(homes.get(2)));
            gui.setItem(24, createHomeItem(homes.get(3)));
        } else if (numItems == 5) {
            gui.setItem(20, createHomeItem(homes.get(0)));
            gui.setItem(21, createHomeItem(homes.get(1)));
            gui.setItem(22, createHomeItem(homes.get(2)));
            gui.setItem(23, createHomeItem(homes.get(3)));
            gui.setItem(24, createHomeItem(homes.get(4)));
        }
        gui.open(viewer);
    }

    private GuiItem createHomeItem(Home home){
        Component name = Component.text(home.getName(), NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        Component descriptionLeftClick = Component.text("Linksklick", NamedTextColor.GRAY)
                .append(Component.text(" >> ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Teleportieren", NamedTextColor.GOLD));
        Component descriptionRightClick = Component.text("Rechtsklick", NamedTextColor.GRAY)
                .append(Component.text(" >> ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Bearbeiten", NamedTextColor.GREEN));
        return ItemBuilder.from(home.getMaterial())
                .name(name)
                .lore(
                        Component.empty(),
                        descriptionLeftClick,
                        descriptionRightClick,
                        Component.empty())
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .asGuiItem(clickEvent -> {
                   clickEvent.setCancelled(true);
                   if(clickEvent.isLeftClick()){
                       spacelifePlayer.teleportCrossServer(home.getLocation());
                       return;
                   }
                   if(clickEvent.isRightClick()){
                       new HomeEditGUI(viewer, home);
                   }
                });
    }

    private GuiItem createAddItem(){
        Component name = Component.text("Home setzen", NamedTextColor.GREEN);
        Component description1 = Component.text("Klicke um ein Home an deiner aktuellen", NamedTextColor.GRAY);
        Component descriotion2 = Component.text("Position zu setzen", NamedTextColor.GRAY);
        return ItemBuilder.from(Material.NETHER_STAR)
                .name(name)
                .lore(
                        Component.empty(),
                        description1,
                        descriotion2,
                        Component.empty())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(clickEvent -> {
                    clickEvent.setCancelled(true);
                    if(!checkPerms()){
                        SpacelifeCore.getInstance().getMessageUtil().sendMessage(viewer, Component.text("Du brauchst einen höheren Rang für mehr Homes!", NamedTextColor.RED));
                        return;
                    }
                    viewer.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    CrossServerLocation location  = new CrossServerLocation(TurtleServer.getInstance().getCloudNetAdapter().getServerInstanceName(), viewer.getLocation());
                    new ChatInputBuilder(Component.text("Bitte gib einen Namen für dein Home an oder abbrechen: ", NamedTextColor.GREEN), viewer)
                            .onCancel(() -> new HomeGUI(viewer))
                            .onInputWithString(string -> saveHome(location, string))
                            .build();
                });
    }
    private boolean checkPerms(){
        if(viewer.hasPermission("spacelife.homes.5") && spacelifePlayer.getHomes().size() < 5){
            return true;
        }
        if(viewer.hasPermission("spacelife.homes.3") && spacelifePlayer.getHomes().size() < 3){
            return true;
        }
        return viewer.hasPermission("spacelife.homes.2") && spacelifePlayer.getHomes().size() < 2;
    }

    private void saveHome(CrossServerLocation location, String name){
        Home home = new Home();
        home.setLocation(location);
        home.setName(name);
        if(viewer.getInventory().getItemInMainHand().getType() != Material.AIR){
            home.setMaterial(viewer.getInventory().getItemInMainHand().getType().name());
        }
        spacelifePlayer.addHome(home);
        new HomeGUI(viewer);
    }
}
