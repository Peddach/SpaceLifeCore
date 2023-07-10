package de.petropia.spacelifeCore.scoreboard.element;

import org.bukkit.util.BoundingBox;

public abstract class GlobalScoreboardElement extends RegionScoreboardElement {

    @Override
    public BoundingBox getRegion() {
        return null;
    }
}
