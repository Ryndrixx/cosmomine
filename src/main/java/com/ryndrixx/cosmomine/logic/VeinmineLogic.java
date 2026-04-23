package com.ryndrixx.cosmomine.logic;

import com.ryndrixx.cosmomine.Config;
import com.ryndrixx.cosmomine.ShapeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class VeinmineLogic {

    /**
     * Returns the list of blocks to mine for the given shape mode.
     * The origin block itself is always index 0 when included.
     * Callers should skip the origin when applying extra breaks (MC handles it).
     */
    public static List<BlockPos> getBlocks(LevelReader level, BlockPos origin,
                                            BlockState targetState, ShapeMode mode,
                                            Player player) {
        return switch (mode) {
            case VEIN       -> vein(level, origin, targetState);
            case TUNNEL_1x2 -> tunnel(origin, player.getDirection(), 1, 2);
            case TUNNEL_3x3 -> tunnel(origin, player.getDirection(), 3, 3);
            case FLAT_3x3   -> flat3x3(origin, player.getDirection());
            case STAIR_DOWN -> stair(origin, player.getDirection(), false);
            case STAIR_UP   -> stair(origin, player.getDirection(), true);
        };
    }

    // -------------------------------------------------------------------------
    // VEIN: BFS, same block type, up to MAX_BLOCKS
    // -------------------------------------------------------------------------
    private static List<BlockPos> vein(LevelReader level, BlockPos origin, BlockState target) {
        int max = Config.MAX_BLOCKS.get();
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && result.size() < max) {
            BlockPos cur = queue.poll();
            result.add(cur);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = cur.relative(dir);
                if (visited.add(neighbor)) {
                    BlockState ns = level.getBlockState(neighbor);
                    if (ns.getBlock() == target.getBlock()) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // TUNNEL: width x height cross-section extending in the facing direction
    // -------------------------------------------------------------------------
    private static List<BlockPos> tunnel(BlockPos origin, Direction facing, int width, int height) {
        int max = Config.MAX_BLOCKS.get();
        List<BlockPos> result = new ArrayList<>();
        Direction right = facing.getClockWise();
        int halfWidth = width / 2;
        // depth = how far we can go forward while staying under max
        int depth = Math.min(max / Math.max(width * height, 1), 20);

        for (int d = 0; d < depth && result.size() < max; d++) {
            for (int w = -halfWidth; w <= halfWidth && result.size() < max; w++) {
                for (int h = 0; h < height && result.size() < max; h++) {
                    BlockPos pos = origin.relative(facing, d)
                                        .relative(right, w)
                                        .above(h);
                    result.add(pos);
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // FLAT 3x3: 3x3 grid in the plane perpendicular to facing direction
    // -------------------------------------------------------------------------
    private static List<BlockPos> flat3x3(BlockPos origin, Direction facing) {
        List<BlockPos> result = new ArrayList<>();
        Direction right = facing.getClockWise();

        for (int r = -1; r <= 1; r++) {
            for (int h = -1; h <= 1; h++) {
                result.add(origin.relative(right, r).above(h));
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // STAIR: diagonal extension, each step forward also steps up or down
    // -------------------------------------------------------------------------
    private static List<BlockPos> stair(BlockPos origin, Direction facing, boolean up) {
        int max = Config.MAX_BLOCKS.get();
        List<BlockPos> result = new ArrayList<>();
        BlockPos cur = origin;
        Direction vertical = up ? Direction.UP : Direction.DOWN;

        while (result.size() < max) {
            result.add(cur);
            // For a proper staircase we also mine the block in front before stepping
            BlockPos ahead = cur.relative(facing);
            if (result.size() < max && !result.contains(ahead)) {
                result.add(ahead);
            }
            cur = ahead.relative(vertical);
        }
        return result;
    }
}
