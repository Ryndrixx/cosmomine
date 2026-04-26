package com.ryndrixx.cosmomine.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ryndrixx.cosmomine.Config;
import com.ryndrixx.cosmomine.ShapeMode;
import com.ryndrixx.cosmomine.logic.VeinmineLogic;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class VeinHighlightRenderer {

    // For each of 6 faces: 4 edges as [corner_dx, corner_dy, corner_dz, axis(0=X,1=Y,2=Z)]
    private static final int[][][] FACE_EDGES = {
        {{0,0,0,0},{1,0,0,2},{0,0,1,0},{0,0,0,2}}, // DOWN  (y-1)
        {{0,1,0,0},{1,1,0,2},{0,1,1,0},{0,1,0,2}}, // UP    (y+1)
        {{0,0,0,0},{1,0,0,1},{0,1,0,0},{0,0,0,1}}, // NORTH (z-1)
        {{0,0,1,0},{1,0,1,1},{0,1,1,0},{0,0,1,1}}, // SOUTH (z+1)
        {{0,0,0,1},{0,1,0,2},{0,0,1,1},{0,0,0,2}}, // WEST  (x-1)
        {{1,0,0,1},{1,1,0,2},{1,0,1,1},{1,0,0,2}}, // EAST  (x+1)
    };

    private static final int[][] FACE_NEIGHBORS = {
        {0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}
    };

    private static List<BlockPos> previewBlocks = Collections.emptyList();
    private static BlockPos lastTarget = null;
    private static ShapeMode lastMode = null;
    private static long lastUpdateTick = -1;

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!KeyBindings.VEINMINE.isDown()) {
            previewBlocks = Collections.emptyList();
            return;
        }

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        BlockPos target = hit.getBlockPos();
        ShapeMode currentMode = VeinmineClientHandler.getCurrentShape();

        long tick = mc.level.getGameTime();
        if (tick - lastUpdateTick >= 4 || !target.equals(lastTarget) || currentMode != lastMode) {
            BlockState state = mc.level.getBlockState(target);
            previewBlocks = VeinmineLogic.getBlocks(mc.level, target, state, currentMode, mc.player);
            lastTarget = target;
            lastMode = currentMode;
            lastUpdateTick = tick;
        }

        if (previewBlocks.isEmpty()) return;

        Set<BlockPos> selectionSet = new HashSet<>(previewBlocks);

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (BlockPos p : previewBlocks) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
        }

        // Count how many exposed faces each edge belongs to.
        // Edges on exactly 1 exposed face are silhouette edges — render them.
        // Edges shared by 2 exposed faces are internal creases — skip them.
        Map<Long, Integer> edgeCounts = new HashMap<>();
        for (BlockPos pos : previewBlocks) {
            int bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
            for (int f = 0; f < 6; f++) {
                int[] nb = FACE_NEIGHBORS[f];
                if (selectionSet.contains(new BlockPos(bx + nb[0], by + nb[1], bz + nb[2]))) continue;
                for (int[] e : FACE_EDGES[f]) {
                    int rx = bx + e[0] - minX;
                    int ry = by + e[1] - minY;
                    int rz = bz + e[2] - minZ;
                    long key = ((long) e[3] << 27) | ((long) rx << 18) | ((long) ry << 9) | rz;
                    edgeCounts.merge(key, 1, Integer::sum);
                }
            }
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        var mat = poseStack.last();

        float[] rgb = Config.parseOutlineColor();
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float a = (float) Config.OUTLINE_OPACITY.get().doubleValue();
        float w = (float) Config.OUTLINE_WIDTH.get().doubleValue();

        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(w);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        var buffer = Tesselator.getInstance().begin(
            VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        for (Map.Entry<Long, Integer> entry : edgeCounts.entrySet()) {
            if (entry.getValue() != 1) continue;

            long key = entry.getKey();
            int axis = (int) ((key >> 27) & 0x3);
            float x1 = (float) (((int) ((key >> 18) & 0x1FF)) + minX - camPos.x);
            float y1 = (float) (((int) ((key >> 9)  & 0x1FF)) + minY - camPos.y);
            float z1 = (float) (((int) (key          & 0x1FF)) + minZ - camPos.z);
            float x2 = x1 + (axis == 0 ? 1f : 0f);
            float y2 = y1 + (axis == 1 ? 1f : 0f);
            float z2 = z1 + (axis == 2 ? 1f : 0f);

            // Normal must be non-zero and perpendicular to the line direction.
            // axis=0 (X): use Y-normal (0,1,0)
            // axis=1 (Y): use X-normal (1,0,0)
            // axis=2 (Z): use X-normal (1,0,0)
            float nx = (axis != 0) ? 1f : 0f;
            float ny = (axis == 0) ? 1f : 0f;
            float nz = 0f;

            buffer.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(mat, nx, ny, nz);
            buffer.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(mat, nx, ny, nz);
        }

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }
}
