package com.ryndrixx.cosmomine.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyBindings {

    /** Hold to activate veinmine. Also: scroll while held to cycle shapes. */
    public static final KeyMapping VEINMINE = new KeyMapping(
        "key.cosmomine.veinmine",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_GRAVE_ACCENT, // ~ / backtick key
        "key.categories.cosmomine"
    );

    /** Press to open the CosmoMine config screen. */
    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
        "key.cosmomine.open_config",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_BACKSLASH,
        "key.categories.cosmomine"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(VEINMINE);
        event.register(OPEN_CONFIG);
    }
}
