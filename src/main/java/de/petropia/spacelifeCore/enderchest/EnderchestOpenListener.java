package de.petropia.spacelifeCore.enderchest;

import de.petropia.spacelifeCore.SpacelifeCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.EnderChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class EnderchestOpenListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        if(event.getPlayer().isSneaking()){
            return;
        }
        if(event.getClickedBlock() == null){
            return;
        }
        if(event.getClickedBlock().getType() != Material.ENDER_CHEST){
            return;
        }
        event.setCancelled(true);
        NamespacedKey key = NamespacedKey.fromString("enderchest-viewer", SpacelifeCore.getInstance());
        if(key == null){
            throw new NullPointerException("Key not found");
        }
        PersistentDataType<Integer, Integer> dataType = PersistentDataType.INTEGER;
        EnderChest enderChestState = (EnderChest) event.getClickedBlock().getState();
        int viewer = enderChestState.getPersistentDataContainer().getOrDefault(key, dataType, 0);
        if(viewer == 0){
            enderChestState.open();
        }
        viewer++;
        enderChestState.getPersistentDataContainer().set(key, dataType, viewer);
        enderChestState.update(true, false);
        new EnderchestGUI(event.getPlayer(), event.getPlayer().getUniqueId(), true, () -> {
            EnderChest enderChest = (EnderChest) event.getClickedBlock().getState();
            int onCloseViewer = enderChest.getPersistentDataContainer().getOrDefault(key, dataType, 1);
            if(onCloseViewer == 1){
                enderChest.close();
                enderChest.getPersistentDataContainer().remove(key);
                enderChest.update(true, false);
                return;
            }
            onCloseViewer--;
            enderChest.getPersistentDataContainer().set(key, dataType, onCloseViewer);
            enderChest.update(true, false);
        });
    }

}
