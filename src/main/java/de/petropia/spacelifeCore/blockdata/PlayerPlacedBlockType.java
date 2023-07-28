package de.petropia.spacelifeCore.blockdata;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;

public class PlayerPlacedBlockType implements PersistentDataType<byte[], PlayerPlacedBlock> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<PlayerPlacedBlock> getComplexType() {
        return PlayerPlacedBlock.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull PlayerPlacedBlock complex, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[24]);
        bb.putLong(complex.uuid().getMostSignificantBits());
        bb.putLong(complex.uuid().getLeastSignificantBits());
        bb.putLong(complex.placedDate().getEpochSecond());
        return bb.array();
    }

    @Override
    public @NotNull PlayerPlacedBlock fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        long uuidFirstLong = bb.getLong();
        long uuidSecondLong = bb.getLong();
        long instantThirdLong = bb.getLong();
        UUID uuid = new UUID(uuidFirstLong, uuidSecondLong);
        Instant instant = Instant.ofEpochSecond(instantThirdLong);
        return new PlayerPlacedBlock(uuid, instant);
    }
}
