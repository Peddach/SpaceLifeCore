package de.petropia.spacelifeCore.scoreboard.element;

import org.bukkit.util.BoundingBox;

public abstract class RegionScoreboardElement extends ScoreboardElement{

    /**
     * Specifies the Region in which the ScoreboardElement should be applied.
     * @return Representation as {@link BoundingBox}
     * @see GlobalScoreboardElement for a global element
     */
    public abstract BoundingBox getRegion();

}
