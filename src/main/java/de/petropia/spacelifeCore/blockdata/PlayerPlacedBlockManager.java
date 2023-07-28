package de.petropia.spacelifeCore.blockdata;

import com.jeff_media.customblockdata.CustomBlockData;
import de.petropia.spacelifeCore.SpacelifeCore;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

public class PlayerPlacedBlockManager {

    private static final NamespacedKey key = new NamespacedKey(SpacelifeCore.getInstance(), "playerPlaceBlock");

    /**
     * Check if a block is placed by a player
     * @param block Block to check on
     * @return true if placed by player
     */
    public static boolean isBlockPlacedByPlayer(Block block){
        PersistentDataContainer customBlockData = new CustomBlockData(block, SpacelifeCore.getInstance());
        return customBlockData.has(key, new PlayerPlacedBlockType());
    }

    /**
     * Get the block info
     * @param block Block to get the data
     * @return PlayerPlacedBlock
     */
    public static @Nullable PlayerPlacedBlock getPlayerPlacedBlock(Block block){
        PersistentDataContainer container = new CustomBlockData(block, SpacelifeCore.getInstance());
        return container.get(key, new PlayerPlacedBlockType());
    }

    /**
     * Get the Key for the PDC
     * @return the Key
     */
    public static NamespacedKey getKey(){
        return key;
    }

    public static void registerListener(){
        CustomBlockData.registerListener(SpacelifeCore.getInstance());
    }
}
