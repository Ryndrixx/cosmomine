package com.ryndrixx.cosmomine;

import com.ryndrixx.cosmomine.client.KeyBindings;
import com.ryndrixx.cosmomine.client.VeinHighlightRenderer;
import com.ryndrixx.cosmomine.client.VeinmineClientHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value = CosmoMine.MODID, dist = Dist.CLIENT)
public class CosmoMineClient {

    public CosmoMineClient(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::register);

        NeoForge.EVENT_BUS.addListener(VeinmineClientHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(VeinHighlightRenderer::onRenderLevel);
    }
}
