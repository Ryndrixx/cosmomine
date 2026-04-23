package com.ryndrixx.cosmomine.network;

import com.ryndrixx.cosmomine.CosmoMine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent client→server whenever the veinmine key is pressed or released. */
public record VeinmineKeyPayload(boolean active) implements CustomPacketPayload {

    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath(CosmoMine.MODID, "veinmine_key");
    public static final Type<VeinmineKeyPayload> TYPE = new Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, VeinmineKeyPayload> CODEC =
        StreamCodec.of(
            (buf, msg) -> buf.writeBoolean(msg.active),
            buf -> new VeinmineKeyPayload(buf.readBoolean())
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
