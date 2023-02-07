package de.petropia.spacelifeCore;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.petropia.spacelifeCore.commands.SpacelifeCommand;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayerLoadingListener;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerMessageListener;
import de.petropia.turtleServer.api.PetropiaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

public class SpacelifeCore extends PetropiaPlugin {

    private static SpacelifeCore instance;

    @Override
    public void onEnable() {
        instance = this;
        new SpacelifePlayerDatabase();
        registerListener();
        registerCommands();
    }

    private void registerListener(){
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SpacelifePlayerLoadingListener(), instance);
        pluginManager.registerEvents(new BlockAnyActionListener(), instance);
        CloudNetDriver.getInstance().getEventManager().registerListener(new CrossServerMessageListener());
    }

    private void registerCommands(){
        getCommand("spacelife").setExecutor(new SpacelifeCommand());
    }

    /**
     * Get the loaded instance of SpacelifeCore plugin
     * @return PetropiaPlugin
     */
    public static SpacelifeCore getInstance(){
        return instance;
    }

    /**
     * Helper method for Async tasks
     * @param runnable Runnable to run
     * @return a BukkitTask
     */
    public static BukkitTask runAync(Runnable runnable){
        return Bukkit.getScheduler().runTaskAsynchronously(instance, runnable);
    }
}
