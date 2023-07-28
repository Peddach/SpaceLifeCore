package de.petropia.spacelifeCore.blockdata;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public record PlayerPlacedBlock(@NotNull UUID uuid, @NotNull Instant placedDate) {}
