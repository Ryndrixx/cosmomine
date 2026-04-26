package com.ryndrixx.cosmomine.client;

import com.ryndrixx.cosmomine.Config;
import com.ryndrixx.cosmomine.ShapeMode;
import com.ryndrixx.cosmomine.network.ShapeModePayload;
import com.ryndrixx.cosmomine.network.VeinmineKeyPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VeinmineClientHandler {

    private static boolean lastKeyState = false;
    private static ShapeMode currentShape = ShapeMode.VEIN;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Sync key state to server when it changes
        boolean keyDown = KeyBindings.VEINMINE.isDown();
        if (keyDown != lastKeyState) {
            lastKeyState = keyDown;
            PacketDistributor.sendToServer(new VeinmineKeyPayload(keyDown));
        }
    }

    /** Scroll wheel while holding ~ cycles the shape mode. */
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!KeyBindings.VEINMINE.isDown()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // If requireSneakToCycle is enabled, sneak must also be held
        if (Config.REQUIRE_SNEAK_TO_CYCLE.get() && !mc.player.isShiftKeyDown()) return;

        // Consume the scroll so the inventory/hotbar doesn't also scroll
        event.setCanceled(true);

        currentShape = event.getScrollDeltaY() > 0 ? currentShape.next() : currentShape.previous();
        PacketDistributor.sendToServer(new ShapeModePayload(currentShape));

        mc.player.displayClientMessage(
            Component.literal("§bCosmoMine§r: " + currentShape.displayName +
                " — " + currentShape.description),
            true // action bar
        );
    }

    public static ShapeMode getCurrentShape() {
        return currentShape;
    }
}
