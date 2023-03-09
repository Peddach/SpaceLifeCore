package de.petropia.spacelifeCore.warp;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;

public class WarpGUI {

    private final PaginatedGui gui;
    private final Player player;

    public WarpGUI(Player player){
        this.player = player;
        gui = Gui.paginated()
                .disableAllInteractions()
                .title(Component.text("Warps", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .rows(5)
                .pageSize(36)
                .create();
        if(SpacelifeDatabase.getInstance().getWarps().isEmpty()){
            gui.setItem(22, ItemBuilder.from(Material.BARRIER).name(Component.text("Keine Warps vorhanden!", NamedTextColor.RED)).asGuiItem());
            gui.open(player);
            return;
        }
        gui.setItem(38, createPreviousItem());
        gui.setItem(40, createRandomWarpItem());
        gui.setItem(42, createNextItem());
        fillWithWarps();
        gui.open(player);
    }

    private void fillWithWarps(){
        for(Warp warp : SpacelifeDatabase.getInstance().getWarps()){
            Component name = Component.text(warp.getName(), NamedTextColor.GREEN);
            Component warpFrom = Component.text("Erstellt von " + warp.getOwnerName(), NamedTextColor.GRAY);
            Component visits = Component.text("Besuche: " + warp.getVisits(), NamedTextColor.GRAY);
            String expireDateFormated = new SimpleDateFormat("dd.MM.yyyy hh:mm").format(warp.getExpireDate());
            Component expireDate = Component.text("Ablaufdatum: " + expireDateFormated, NamedTextColor.GRAY);
            Component rightClick = Component.text("Linksklick", NamedTextColor.GRAY)
                    .append(Component.text(" >> ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("teleportieren", NamedTextColor.GOLD));
            GuiItem item = ItemBuilder.skull()
                    .texture(warp.getOwnerSkin(), UUID.fromString(warp.getOwnerUUID()))
                    .name(name)
                    .lore(
                            Component.empty(),
                            warpFrom,
                            expireDate,
                            visits,
                            Component.empty(),
                            rightClick,
                            Component.empty())
                    .asGuiItem(e -> Bukkit.getScheduler().runTask(SpacelifeCore.getInstance(), () -> {
                        gui.close(player);
                        warp.teleport(player);
                    }));
            gui.addItem(item);
        }
    }

    private GuiItem createNextItem(){
        Component name = Component.text("Nächste Seite", NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        return ItemBuilder.from(Material.VERDANT_FROGLIGHT)
                .name(name)
                .asGuiItem(e -> gui.next());
    }

    private GuiItem createPreviousItem(){
        Component name = Component.text("Vorherige Seite", NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
        return ItemBuilder.from(Material.OCHRE_FROGLIGHT)
                .name(name)
                .asGuiItem(e -> gui.previous());
    }

    private GuiItem createRandomWarpItem(){
        Component name = Component.text("Zufälliger Warp", NamedTextColor.RED).decorate(TextDecoration.BOLD);
        return ItemBuilder.from(Material.GLOWSTONE_DUST)
                .name(name)
                .asGuiItem(e -> {
                    Random random = new Random();
                    Warp warp = SpacelifeDatabase.getInstance().getWarps().get(random.nextInt(SpacelifeDatabase.getInstance().getWarps().size()));
                    SpacelifeCore.getInstance().getMessageUtil().sendMessage(player, Component.text("Du wirst zum Warp ", NamedTextColor.GRAY)
                            .append(Component.text(warp.getName(), NamedTextColor.GOLD))
                            .append(Component.text(" teleportiert!", NamedTextColor.GRAY)));
                    warp.teleport(player);
                });
    }
}
