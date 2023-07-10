package de.petropia.spacelifeCore.scoreboard.implementation;

import de.petropia.spacelifeCore.player.SpacelifeDatabase;
import de.petropia.spacelifeCore.scoreboard.element.GlobalScoreboardElement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;

public class GlobalMoneyScoreboardElement extends GlobalScoreboardElement {

    private static final String TITLE = "Geld";

    @Override
    public List<String> getContent(Player player) {
        return List.of(String.valueOf(NumberFormat.getCurrencyInstance().format(SpacelifeDatabase.getInstance().getCachedPlayer(player.getUniqueId()).getMoney())));
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public @Nullable String getPermission() {
        return null;
    }

    @Override
    public int getPriority() {
        return 299;
    }
}
