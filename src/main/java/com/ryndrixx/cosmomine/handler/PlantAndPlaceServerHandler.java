package com.ryndrixx.cosmomine.handler;

import com.ryndrixx.cosmomine.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

public class PlantAndPlaceServerHandler {

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        if (!VeinmineServerHandler.isActive(player.getUUID())) return;

        ItemStack held = event.getItemStack();
        if (held.isEmpty()) return;

        Level level = (Level) event.getLevel();
        BlockPos clickedPos = event.getPos();
        Direction clickedFace = event.getFace();

        if (isSeedItem(held) && level.getBlockState(clickedPos).is(Blocks.FARMLAND)) {
            // Cancel vanilla — we handle all planting including the origin
            event.setCanceled(true);
            massPlant(player, level, clickedPos, held);
        } else if (held.getItem() instanceof BlockItem blockItem && !isSeedItem(held) && clickedFace != null) {
            BlockPos placeOrigin = clickedPos.relative(clickedFace);
            massPlace(player, level, placeOrigin, clickedPos, clickedFace, blockItem, held, event.getHand());
        }
    }

    private static boolean isSeedItem(ItemStack stack) {
        if (stack.getItem() instanceof SpecialPlantable) return true;
        if (stack.getItem() instanceof BlockItem bi) {
            return bi.getBlock() instanceof CropBlock
                || bi.getBlock() instanceof StemBlock
                || bi.getBlock() instanceof NetherWartBlock;
        }
        return false;
    }

    private static void massPlant(Player player, Level level, BlockPos origin, ItemStack seeds) {
        if (!(seeds.getItem() instanceof BlockItem blockItem)) return;

        BlockState plantState = blockItem.getBlock().defaultBlockState();
        int max = Config.MAX_BLOCKS.get();

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int planted = 0;

        while (!queue.isEmpty() && seeds.getCount() > 0 && planted < max) {
            BlockPos pos = queue.poll();

            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, plantState, 3);
                seeds.shrink(1);
                planted++;
                if (Config.CONSUME_HUNGER.get()) player.causeFoodExhaustion(0.005f);
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.add(neighbor) && level.getBlockState(neighbor).is(Blocks.FARMLAND)) {
                    queue.add(neighbor);
                }
            }
        }

        if (player instanceof ServerPlayer sp) {
            sp.inventoryMenu.sendAllDataToRemote();
        }
    }

    private static void massPlace(Player player, Level level, BlockPos origin,
                                   BlockPos clickedSurface, Direction clickedFace,
                                   BlockItem blockItem, ItemStack held, InteractionHand hand) {
        // Leave 1 block for vanilla to place at the origin
        int available = held.getCount() - 1;
        if (available <= 0) return;

        int max = Config.MAX_BLOCKS.get();
        Direction[] axes = getPerpendicularAxes(clickedFace);
        int placed = 0;

        for (int a = -1; a <= 1 && available > 0 && placed < max; a++) {
            for (int b = -1; b <= 1 && available > 0 && placed < max; b++) {
                if (a == 0 && b == 0) continue; // origin — vanilla handles it

                BlockPos pos = origin.relative(axes[0], a).relative(axes[1], b);
                BlockState existing = level.getBlockState(pos);
                if (!existing.canBeReplaced()) continue;

                // Use getStateForPlacement so stairs, slabs, etc. orient correctly
                BlockPos surfacePos = pos.relative(clickedFace.getOpposite());
                BlockHitResult fakeHit = new BlockHitResult(
                    Vec3.atCenterOf(pos), clickedFace.getOpposite(), surfacePos, false
                );
                UseOnContext fakeCtx = new UseOnContext(level, player, hand, held.copy(), fakeHit);
                BlockState toPlace = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(fakeCtx));
                if (toPlace == null) toPlace = blockItem.getBlock().defaultBlockState();
                if (!toPlace.canSurvive(level, pos)) continue;

                level.setBlock(pos, toPlace, 3);
                held.shrink(1);
                available--;
                placed++;

                if (Config.CONSUME_HUNGER.get()) player.causeFoodExhaustion(0.005f);
            }
        }
    }

    private static Direction[] getPerpendicularAxes(Direction face) {
        return switch (face.getAxis()) {
            case Y -> new Direction[]{Direction.EAST, Direction.SOUTH};
            case X -> new Direction[]{Direction.SOUTH, Direction.UP};
            case Z -> new Direction[]{Direction.EAST, Direction.UP};
        };
    }
}
