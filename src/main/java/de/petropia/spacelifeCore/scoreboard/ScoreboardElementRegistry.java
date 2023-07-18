package de.petropia.spacelifeCore.scoreboard;

import de.petropia.spacelifeCore.SpacelifeCore;
import de.petropia.spacelifeCore.scoreboard.element.RegionScoreboardElement;
import de.petropia.spacelifeCore.scoreboard.element.ScoreboardElement;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreboardElementRegistry {

    private static final List<ScoreboardElement> REGISTRY = new ArrayList<>();
    private static final HashMap<Player, FastBoard> SCOREBOARDS = new HashMap<>();
    private static int skipScoreboardRenders = 0;

    public static void registerElement(ScoreboardElement element){
        REGISTRY.add(element);
        REGISTRY.sort((o1, o2) -> {
            int priority1 = o1.getPriority();
            int priority2 = o2.getPriority();
            // Higher priority elements should come first
            return Integer.compare(priority2, priority1);
        });
    }

    public static void unregisterElement(ScoreboardElement element){
        REGISTRY.remove(element);
    }

    public static List<ScoreboardElement> getElements(){
        return new ArrayList<>(REGISTRY);
    }

    public static void startTicking(){
        SpacelifeCore.getInstance().getLogger().info("Loading Scoreboard");
        Bukkit.getServer().getScheduler().runTaskTimer(SpacelifeCore.getInstance(), () -> {
            if(skipScoreboardRenders > 0){
                skipScoreboardRenders--;
                return;
            }
            if(Bukkit.getServer().getTPS()[0] < 16){ // Skip scoreboard when TPS too low.
                if(skipScoreboardRenders == 0) {
                    SpacelifeCore.getInstance().getLogger().info("TPS too low. Skipping Scoreboard render");
                    Bukkit.getServer().broadcast(Component.text("TPS too low for scoreboard"), "*");
                }
                skipScoreboardRenders = 10; //Next 10 times rendering will be skipped.
                return;
            }
            List<Player> playerList = new ArrayList<>(SCOREBOARDS.keySet());
            for (Player value : playerList) {
                if (!value.isOnline()) {
                    SCOREBOARDS.remove(value);
                }
            }
            for(Player player : Bukkit.getOnlinePlayers()){
                FastBoard fastBoard = SCOREBOARDS.computeIfAbsent(player, player1 -> {
                    FastBoard board = new FastBoard(player);
                    board.updateTitle("§6§lPetropia.net");
                    return board;
                });
                List<String> lines = new ArrayList<>();
                for(ScoreboardElement element : REGISTRY){
                    if(element.getPermission() != null && !player.hasPermission(element.getPermission())){
                        continue;
                    }
                    if(element instanceof RegionScoreboardElement regionScoreboardElement){
                        Location loc = player.getLocation();
                        if(regionScoreboardElement.getRegion() != null &&!regionScoreboardElement.getRegion().contains(loc.getX(), loc.getY(), loc.getZ())){
                            continue;
                        }
                    }
                    lines.add(" ");
                    lines.add("§a§l" + element.getTitle());
                    element.getContent(player).forEach(line -> lines.add("§7" + line));
                }
                fastBoard.updateLines(lines);
            }
        }, 20, 5);
    }

}
