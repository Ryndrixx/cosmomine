package com.ryndrixx.cosmomine.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ryndrixx.cosmomine.ShapeMode;
import com.ryndrixx.cosmomine.logic.VeinmineLogic;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

public class VeinHighlightRenderer {

    /** Lines render type with depth test disabled — outlines show through blocks (x-ray). */
    private static final RenderType LINES_NO_DEPTH = RenderType.create(
        "cosmomine_lines_no_depth",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        256,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .createCompositeState(false)
    );

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

        // Recompute preview every 4 ticks or when target/mode changes
        long tick = mc.level.getGameTime();
        if (tick - lastUpdateTick >= 4 || !target.equals(lastTarget) || currentMode != lastMode) {
            BlockState state = mc.level.getBlockState(target);
            previewBlocks = VeinmineLogic.getBlocks(mc.level, target, state, currentMode, mc.player);
            lastTarget = target;
            lastMode = currentMode;
            lastUpdateTick = tick;
        }

        if (previewBlocks.size() <= 1) return;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        var bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(LINES_NO_DEPTH);

        float expand = 0.004f;
        for (int i = 1; i < previewBlocks.size(); i++) { // skip [0] = origin (MC highlights it)
            BlockPos pos = previewBlocks.get(i);
            double x = pos.getX() - camPos.x;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z;

            LevelRenderer.renderLineBox(
                poseStack, lines,
                new AABB(x - expand, y - expand, z - expand,
                         x + 1 + expand, y + 1 + expand, z + 1 + expand),
                0.0f, 0.75f, 1.0f, 0.9f // bright cyan
            );
        }

        bufferSource.endBatch(LINES_NO_DEPTH);
    }
}
