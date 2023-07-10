package de.petropia.spacelifeCore.scoreboard.element;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ScoreboardElement {

    /**
     * Renders teh content for a player
     * @param player Player to render the scoreboard for
     * @return List of Strings representing the content
     */
    public abstract List<String> getContent(Player player);

    /**
     * Used for getting the title above the content. Formatting is done by the registry. Only declare raw title
     * @return title String
     */
    public abstract String getTitle();

    /**
     * Used for seting a permissoin for this entry. Return null for no specific permission
     * @return permisson as String
     */
    public abstract @Nullable String getPermission();

    /**
     * Get the priority of the scoreboard Element. The higher the priority, the higher the element. <br>
     * 0-100 = Bottom <br>
     * 101-200 = Middle <br>
     * 201-300 = Top <br>
     * @return priority as int
     */
    public abstract int getPriority();

}
