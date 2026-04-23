package com.ryndrixx.cosmomine.client;

import com.ryndrixx.cosmomine.ShapeMode;
import com.ryndrixx.cosmomine.network.ShapeModePayload;
import com.ryndrixx.cosmomine.network.VeinmineKeyPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class VeinmineClientHandler {

    private static boolean lastKeyState = false;
    private static ShapeMode currentShape = ShapeMode.VEIN;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Sync veinmine key state to server when it changes
        boolean keyDown = KeyBindings.VEINMINE.isDown();
        if (keyDown != lastKeyState) {
            lastKeyState = keyDown;
            PacketDistributor.sendToServer(new VeinmineKeyPayload(keyDown));
        }

        // Cycle shape mode on keypress
        while (KeyBindings.CYCLE_SHAPE.consumeClick()) {
            currentShape = currentShape.next();
            PacketDistributor.sendToServer(new ShapeModePayload(currentShape));
            mc.player.displayClientMessage(
                Component.literal("§bCosmoMine§r: " + currentShape.displayName +
                    " — " + currentShape.description),
                true // action bar, not chat
            );
        }
    }

    public static ShapeMode getCurrentShape() {
        return currentShape;
    }
}
