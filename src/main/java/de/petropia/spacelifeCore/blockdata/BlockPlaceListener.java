package de.petropia.spacelifeCore.blockdata;

import com.jeff_media.customblockdata.CustomBlockData;
import de.petropia.spacelifeCore.SpacelifeCore;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.time.Instant;

public class BlockPlaceListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        PlayerPlacedBlock playerPlacedBlock = new PlayerPlacedBlock(player.getUniqueId(), Instant.now());
        CustomBlockData blockData = new CustomBlockData(block, SpacelifeCore.getInstance());
        NamespacedKey key = PlayerPlacedBlockManager.getKey();
        blockData.set(key, new PlayerPlacedBlockType(), playerPlacedBlock);
    }
}
