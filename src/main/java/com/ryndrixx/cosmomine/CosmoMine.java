package com.ryndrixx.cosmomine;

import com.mojang.logging.LogUtils;
import com.ryndrixx.cosmomine.handler.PlantAndPlaceServerHandler;
import com.ryndrixx.cosmomine.handler.VeinmineServerHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(CosmoMine.MODID)
public class CosmoMine {

    public static final String MODID = "cosmomine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CosmoMine(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(VeinmineServerHandler::registerPayloads);

        NeoForge.EVENT_BUS.addListener(VeinmineServerHandler::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(VeinmineServerHandler::onPlayerLeave);
        NeoForge.EVENT_BUS.addListener(PlantAndPlaceServerHandler::onRightClick);

        LOGGER.info("CosmoMine loaded. Hold [V] to veinmine, [` ] to cycle shapes.");
    }
}
