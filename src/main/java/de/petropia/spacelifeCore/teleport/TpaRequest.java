package de.petropia.spacelifeCore.teleport;

import java.util.UUID;

public record TpaRequest(UUID requestID, String requesterUUID, String requesterName, String targetUUID, String serviceID) {

}
