package de.petropia.spacelifeCore.economy.dto;

import java.util.UUID;

public record PlayerPayDTO(
    UUID targetUUID,
    double amount,
    String payer
){}
