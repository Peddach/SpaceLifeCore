package de.petropia.spacelifeCore.teleport.dto;

import java.util.UUID;

public record TpaRequestAcceptDTO(
        UUID requesterUUID,
        UUID targetUUID,
        String targetName,
        boolean accepted
) { }
