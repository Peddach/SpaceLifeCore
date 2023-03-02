package de.petropia.spacelifeCore;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.petropia.spacelifeCore.commands.SpacelifeCommand;
import de.petropia.spacelifeCore.commands.SpawnCommand;
import de.petropia.spacelifeCore.commands.TrashCommand;
import de.petropia.spacelifeCore.commands.WorkbenchCommand;
import de.petropia.spacelifeCore.economy.BalanceCommand;
import de.petropia.spacelifeCore.economy.PayCommand;
import de.petropia.spacelifeCore.enderchest.EnderchestCommand;
import de.petropia.spacelifeCore.enderchest.EnderchestOpenListener;
import de.petropia.spacelifeCore.home.HomeCommand;
import de.petropia.spacelifeCore.player.SpacelifePlayerDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayerLoadingListener;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerMessageListener;
import de.petropia.spacelifeCore.teleport.TpaCommand;
import de.petropia.turtleServer.api.PetropiaPlugin;
import org.bukkit.plugin.PluginManager;

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
        pluginManager.registerEvents(new EnderchestOpenListener(), instance);
        CloudNetDriver.getInstance().getEventManager().registerListener(new CrossServerMessageListener());
        CloudNetDriver.getInstance().getEventManager().registerListener(new PayCommand());
        CloudNetDriver.getInstance().getEventManager().registerListener(new TpaCommand());
    }

    private void registerCommands(){
        getCommand("spacelife").setExecutor(new SpacelifeCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("workbench").setExecutor(new WorkbenchCommand());
        getCommand("tpa").setExecutor(new TpaCommand());
        getCommand("trash").setExecutor(new TrashCommand());
        getCommand("enderchest").setExecutor(new EnderchestCommand());
    }

    /**
     * Get the loaded instance of SpacelifeCore plugin
     * @return PetropiaPlugin
     */
    public static SpacelifeCore getInstance(){
        return instance;
    }
}
