package com.ryndrixx.cosmomine;

import com.ryndrixx.cosmomine.client.ConfigScreen;
import com.ryndrixx.cosmomine.client.KeyBindings;
import com.ryndrixx.cosmomine.client.VeinHighlightRenderer;
import com.ryndrixx.cosmomine.client.VeinmineClientHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CosmoMine.MODID, dist = Dist.CLIENT)
public class CosmoMineClient {

    public CosmoMineClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(KeyBindings::register);

        NeoForge.EVENT_BUS.addListener(VeinmineClientHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(VeinmineClientHandler::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(VeinHighlightRenderer::onRenderLevel);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
            (mc, parent) -> new ConfigScreen(parent));
    }
}
