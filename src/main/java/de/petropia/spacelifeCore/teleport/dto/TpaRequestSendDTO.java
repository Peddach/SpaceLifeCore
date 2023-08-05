package de.petropia.spacelifeCore.teleport.dto;

import java.util.UUID;

public record TpaRequestSendDTO(
        UUID playerUUID,
        String playerName,
        UUID targetUUID
) {}
