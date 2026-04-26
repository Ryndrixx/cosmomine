package com.ryndrixx.cosmomine.handler;

import com.ryndrixx.cosmomine.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
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
import net.neoforged.neoforge.common.ItemAbilities;
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
        InteractionHand hand = event.getHand();

        if (isSeedItem(held) && level.getBlockState(clickedPos).is(Blocks.FARMLAND)) {
            event.setCanceled(true);
            massPlant(player, level, clickedPos, held, hand);
        } else if (held.getItem() instanceof AxeItem && level.getBlockState(clickedPos).is(BlockTags.LOGS)) {
            event.setCanceled(true);
            massStrip(player, level, clickedPos, held, hand);
        } else if (held.getItem() instanceof BlockItem blockItem && !isSeedItem(held) && clickedFace != null) {
            BlockPos placeOrigin = clickedPos.relative(clickedFace);
            massPlace(player, level, placeOrigin, clickedPos, clickedFace, blockItem, held, hand);
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

    private static void massPlant(Player player, Level level, BlockPos origin, ItemStack seeds, InteractionHand hand) {
        int max = Config.MAX_BLOCKS.get();

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int planted = 0;

        while (!queue.isEmpty() && seeds.getCount() > 0 && planted < max) {
            BlockPos pos = queue.poll();

            if (level.getBlockState(pos.above()).isAir()) {
                BlockHitResult fakeHit = new BlockHitResult(
                    Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
                UseOnContext ctx = new UseOnContext(level, player, hand, seeds, fakeHit);
                InteractionResult result = seeds.useOn(ctx);
                if (result.consumesAction()) {
                    planted++;
                    player.causeFoodExhaustion(0.005f);
                }
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

    private static void massStrip(Player player, Level level, BlockPos origin, ItemStack axe, InteractionHand hand) {
        BlockState originState = level.getBlockState(origin);
        int max = Config.MAX_BLOCKS.get();

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int stripped = 0;

        while (!queue.isEmpty() && stripped < max && !axe.isEmpty()) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() != originState.getBlock()) continue;

            BlockHitResult fakeHit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            UseOnContext ctx = new UseOnContext(level, player, hand, axe, fakeHit);
            BlockState strippedState = state.getToolModifiedState(ctx, ItemAbilities.AXE_STRIP, false);

            if (strippedState != null) {
                level.setBlock(pos, strippedState, 11);
                level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
                axe.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                stripped++;
                player.causeFoodExhaustion(0.005f);
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.add(neighbor) && level.getBlockState(neighbor).getBlock() == originState.getBlock()) {
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
        int available = held.getCount() - 1;
        if (available <= 0) return;

        int max = Config.MAX_BLOCKS.get();
        Direction[] axes = getPerpendicularAxes(clickedFace);
        int placed = 0;

        for (int a = -1; a <= 1 && available > 0 && placed < max; a++) {
            for (int b = -1; b <= 1 && available > 0 && placed < max; b++) {
                if (a == 0 && b == 0) continue;

                BlockPos pos = origin.relative(axes[0], a).relative(axes[1], b);
                BlockState existing = level.getBlockState(pos);
                if (!existing.canBeReplaced()) continue;

                BlockPos surfacePos = pos.relative(clickedFace.getOpposite());
                BlockHitResult fakeHit = new BlockHitResult(
                    Vec3.atCenterOf(pos), clickedFace.getOpposite(), surfacePos, false);
                UseOnContext fakeCtx = new UseOnContext(level, player, hand, held.copy(), fakeHit);
                BlockState toPlace = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(fakeCtx));
                if (toPlace == null) toPlace = blockItem.getBlock().defaultBlockState();
                if (!toPlace.canSurvive(level, pos)) continue;

                level.setBlock(pos, toPlace, 3);
                held.shrink(1);
                available--;
                placed++;

                player.causeFoodExhaustion(0.005f);
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
