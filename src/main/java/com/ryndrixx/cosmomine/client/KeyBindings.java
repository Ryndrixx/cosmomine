package com.ryndrixx.cosmomine.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final KeyMapping VEINMINE = new KeyMapping(
        "key.cosmomine.veinmine",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "key.categories.cosmomine"
    );

    public static final KeyMapping CYCLE_SHAPE = new KeyMapping(
        "key.cosmomine.cycle_shape",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_GRAVE_ACCENT, // backtick `
        "key.categories.cosmomine"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(VEINMINE);
        event.register(CYCLE_SHAPE);
    }
}
