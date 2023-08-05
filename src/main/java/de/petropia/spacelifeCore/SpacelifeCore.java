package de.petropia.spacelifeCore;

import de.petropia.spacelifeCore.blockdata.BlockPlaceListener;
import de.petropia.spacelifeCore.blockdata.PlayerPlacedBlockManager;
import de.petropia.spacelifeCore.commands.*;
import de.petropia.spacelifeCore.economy.BalanceCommand;
import de.petropia.spacelifeCore.economy.PayCommand;
import de.petropia.spacelifeCore.enderchest.EnderchestCommand;
import de.petropia.spacelifeCore.enderchest.EnderchestOpenListener;
import de.petropia.spacelifeCore.home.HomeCommand;
import de.petropia.spacelifeCore.player.PlayerDeathListener;
import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.player.SpacelifePlayerLoadingListener;
import de.petropia.spacelifeCore.scoreboard.ScoreboardElementRegistry;
import de.petropia.spacelifeCore.scoreboard.implementation.GlobalMoneyScoreboardElement;
import de.petropia.spacelifeCore.teleport.BlockAnyActionListener;
import de.petropia.spacelifeCore.teleport.CrossServerMessageListener;
import de.petropia.spacelifeCore.teleport.TpaCommand;
import de.petropia.spacelifeCore.warp.WarpCommand;
import de.petropia.turtleServer.api.PetropiaPlugin;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.plugin.PluginManager;

public class SpacelifeCore extends PetropiaPlugin {

    private static SpacelifeCore instance;

    @Override
    public void onEnable() {
        instance = this;
        new SpacelifeDatabase();
        registerListener();
        registerCommands();
        ScoreboardElementRegistry.registerElement(new GlobalMoneyScoreboardElement());
        ScoreboardElementRegistry.startTicking();
        PlayerPlacedBlockManager.registerListener();
    }

    private void registerListener(){
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SpacelifePlayerLoadingListener(), instance);
        pluginManager.registerEvents(new BlockAnyActionListener(), instance);
        pluginManager.registerEvents(new EnderchestOpenListener(), instance);
        pluginManager.registerEvents(new BlockPlaceListener(), instance);
        pluginManager.registerEvents(new PlayerDeathListener(), instance);
        EventManager eventManager = InjectionLayer.ext().instance(EventManager.class);
        eventManager.registerListener(new CrossServerMessageListener());
        eventManager.registerListener(new PayCommand());
        eventManager.registerListener(new TpaCommand());
    }

    private void registerCommands(){
        getCommand("spacelife").setExecutor(new SpacelifeCommand());
        getCommand("spacelife").setTabCompleter(new SpacelifeCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("workbench").setExecutor(new WorkbenchCommand());
        getCommand("tpa").setExecutor(new TpaCommand());
        getCommand("trash").setExecutor(new TrashCommand());
        getCommand("enderchest").setExecutor(new EnderchestCommand());
        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("warp").setTabCompleter(new WarpCommand());
        getCommand("nightvision").setExecutor(new NightVisionCommand());
        getCommand("fly").setExecutor(new FlyCommand());
        getCommand("hat").setExecutor(new HatCommand());
    }

    /**
     * Get the loaded instance of SpacelifeCore plugin
     * @return PetropiaPlugin
     */
    public static SpacelifeCore getInstance(){
        return instance;
    }
}
