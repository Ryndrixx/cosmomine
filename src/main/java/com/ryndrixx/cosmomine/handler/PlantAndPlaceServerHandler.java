package com.ryndrixx.cosmomine.handler;

import com.ryndrixx.cosmomine.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
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
            massPlant(player, level, clickedPos, held);
        } else if (held.getItem() instanceof BlockItem blockItem && !isSeedItem(held) && clickedFace != null) {
            BlockPos placeOrigin = clickedPos.relative(clickedFace);
            massPlace(player, level, placeOrigin, clickedFace, blockItem, held);
        }
    }

    /** Returns true if this item plants a crop on farmland. */
    private static boolean isSeedItem(ItemStack stack) {
        if (stack.getItem() instanceof SpecialPlantable) return true;
        if (stack.getItem() instanceof BlockItem bi) {
            return bi.getBlock() instanceof CropBlock || bi.getBlock() instanceof StemBlock;
        }
        return false;
    }

    private static void massPlant(Player player, Level level, BlockPos origin, ItemStack seeds) {
        // Leave 1 seed for vanilla to plant at the clicked origin
        int available = seeds.getCount() - 1;
        if (available <= 0) return;

        int max = Config.MAX_BLOCKS.get();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int planted = 0;

        // Get the plant state from the seed's block (e.g. wheat seeds → wheat crop age 0)
        BlockState plantState = ((BlockItem) seeds.getItem()).getBlock().defaultBlockState();

        while (!queue.isEmpty() && available > 0 && planted < max) {
            BlockPos pos = queue.poll();

            if (!pos.equals(origin)) {
                BlockPos above = pos.above();
                if (level.getBlockState(above).isAir() && plantState.canSurvive(level, above)) {
                    level.setBlock(above, plantState, 3);
                    seeds.shrink(1);
                    available--;
                    planted++;
                    if (Config.CONSUME_HUNGER.get()) player.causeFoodExhaustion(0.005f);
                }
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.add(neighbor) && level.getBlockState(neighbor).is(Blocks.FARMLAND)) {
                    queue.add(neighbor);
                }
            }
        }
    }

    private static void massPlace(Player player, Level level, BlockPos origin,
                                   Direction clickedFace, BlockItem blockItem, ItemStack held) {
        // Leave 1 block for vanilla to place at the origin position
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

                BlockState toPlace = blockItem.getBlock().defaultBlockState();
                if (!toPlace.canSurvive(level, pos)) continue;

                level.setBlock(pos, toPlace, 3);
                held.shrink(1);
                available--;
                placed++;

                if (Config.CONSUME_HUNGER.get()) player.causeFoodExhaustion(0.005f);
            }
        }
    }

    /** Returns two directions perpendicular to the given face for building a 3×3 grid on that face. */
    private static Direction[] getPerpendicularAxes(Direction face) {
        return switch (face.getAxis()) {
            case Y -> new Direction[]{Direction.EAST, Direction.SOUTH};   // floor/ceiling → horizontal grid
            case X -> new Direction[]{Direction.SOUTH, Direction.UP};     // east/west wall → depth × height
            case Z -> new Direction[]{Direction.EAST, Direction.UP};      // north/south wall → width × height
        };
    }
}
