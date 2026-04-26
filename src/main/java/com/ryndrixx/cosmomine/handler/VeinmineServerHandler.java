package com.ryndrixx.cosmomine.handler;

import com.ryndrixx.cosmomine.Config;
import com.ryndrixx.cosmomine.ShapeMode;
import com.ryndrixx.cosmomine.logic.VeinmineLogic;
import com.ryndrixx.cosmomine.network.ShapeModePayload;
import com.ryndrixx.cosmomine.network.VeinmineKeyPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.*;

public class VeinmineServerHandler {

    /** Players currently holding the veinmine key. */
    private static final Set<UUID> ACTIVE = new HashSet<>();

    /** Per-player selected shape mode. */
    private static final Map<UUID, ShapeMode> SHAPES = new HashMap<>();

    /**
     * Recursion guard: players whose extra blocks are currently being broken.
     * Without this, each programmatic destroyBlock() would fire BreakEvent again.
     */
    private static final Set<UUID> IN_VEINMINE = new HashSet<>();

    // -----------------------------------------------------------------------
    // Network payload handlers — registered from CosmoMine on mod event bus
    // -----------------------------------------------------------------------

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToServer(
            VeinmineKeyPayload.TYPE,
            VeinmineKeyPayload.CODEC,
            (payload, ctx) -> ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                if (payload.active()) {
                    ACTIVE.add(player.getUUID());
                } else {
                    ACTIVE.remove(player.getUUID());
                }
            })
        );

        registrar.playToServer(
            ShapeModePayload.TYPE,
            ShapeModePayload.CODEC,
            (payload, ctx) -> ctx.enqueueWork(() -> {
                SHAPES.put(ctx.player().getUUID(), payload.mode());
            })
        );
    }

    // -----------------------------------------------------------------------
    // Block break event — fired server-side for every block the player mines
    // -----------------------------------------------------------------------

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUUID();

        // Skip if this break is one we triggered ourselves (recursion guard)
        if (IN_VEINMINE.contains(id)) return;

        // Skip if veinmine key isn't held
        if (!ACTIVE.contains(id)) return;

        // Server-side only
        if (event.getLevel().isClientSide()) return;

        BlockPos origin = event.getPos();
        BlockState targetState = event.getState();

        // Correct-tool check on the original block (MC handles this one itself;
        // we apply the same logic to extra blocks below)
        if (Config.REQUIRE_CORRECT_TOOL.get() &&
                !player.hasCorrectToolForDrops(targetState, player.level(), origin)) {
            return;
        }

        ShapeMode mode = SHAPES.getOrDefault(id, ShapeMode.VEIN);
        List<BlockPos> blocks = VeinmineLogic.getBlocks(
            player.level(), origin, targetState, mode, player);

        // blocks[0] is the origin — MC is already breaking it, skip it
        if (blocks.size() <= 1) return;

        IN_VEINMINE.add(id);
        try {
            ItemStack tool = player.getMainHandItem();
            for (int i = 1; i < blocks.size(); i++) {
                BlockPos pos = blocks.get(i);
                BlockState state = player.level().getBlockState(pos);
                if (state.isAir()) continue;
                if (!state.getFluidState().isEmpty()) continue; // skip water/lava — no durability cost

                // For non-vein shapes we don't restrict by block type
                if (mode == ShapeMode.VEIN && state.getBlock() != targetState.getBlock()) continue;

                if (Config.REQUIRE_CORRECT_TOOL.get() &&
                        !player.hasCorrectToolForDrops(state, player.level(), pos)) continue;

                // destroyBlock handles: drops (with Fortune/Silk Touch), XP, durability, sounds
                ((ServerPlayer) player).gameMode.destroyBlock(pos);

                player.causeFoodExhaustion(0.005f);
            }
        } finally {
            IN_VEINMINE.remove(id);
        }
    }

    /** Clean up when a player disconnects so state doesn't linger. */
    public static void onPlayerLeave(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        ACTIVE.remove(id);
        SHAPES.remove(id);
        IN_VEINMINE.remove(id);
    }

    public static ShapeMode getShape(UUID playerId) {
        return SHAPES.getOrDefault(playerId, ShapeMode.VEIN);
    }

    public static boolean isActive(UUID playerId) {
        return ACTIVE.contains(playerId);
    }
}
