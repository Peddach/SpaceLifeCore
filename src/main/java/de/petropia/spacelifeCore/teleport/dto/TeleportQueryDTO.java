package de.petropia.spacelifeCore.teleport.dto;

public record TeleportQueryDTO(
        String result,
        double x,
        double y,
        double z,
        float pitch,
        float yaw,
        String world
) {}
