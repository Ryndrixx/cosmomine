package com.ryndrixx.cosmomine.network;

import com.ryndrixx.cosmomine.CosmoMine;
import com.ryndrixx.cosmomine.ShapeMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent client→server when the player cycles their shape mode. */
public record ShapeModePayload(ShapeMode mode) implements CustomPacketPayload {

    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath(CosmoMine.MODID, "shape_mode");
    public static final Type<ShapeModePayload> TYPE = new Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, ShapeModePayload> CODEC =
        StreamCodec.of(
            (buf, msg) -> buf.writeInt(msg.mode.ordinal()),
            buf -> {
                int ord = buf.readInt();
                ShapeMode[] values = ShapeMode.values();
                return new ShapeModePayload(values[Math.min(ord, values.length - 1)]);
            }
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
