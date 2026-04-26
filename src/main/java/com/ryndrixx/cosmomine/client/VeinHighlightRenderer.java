package com.ryndrixx.cosmomine.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ryndrixx.cosmomine.Config;
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

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class VeinHighlightRenderer {

    private static RenderType linesNoDepth = null;

    private static RenderType getLineType() {
        if (linesNoDepth == null) {
            linesNoDepth = RenderType.create(
                "cosmomine_lines_nodepth",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                1536,
                RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0)))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.MAIN_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false)
            );
        }
        return linesNoDepth;
    }

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

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        float[] rgb = Config.parseOutlineColor();
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float a = (float) Config.OUTLINE_OPACITY.get().doubleValue();

        var bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(getLineType());

        for (BlockPos pos : previewBlocks) {
            double ox = pos.getX() - camPos.x;
            double oy = pos.getY() - camPos.y;
            double oz = pos.getZ() - camPos.z;
            LevelRenderer.renderLineBox(poseStack, consumer,
                new AABB(ox - 0.002, oy - 0.002, oz - 0.002,
                         ox + 1.002, oy + 1.002, oz + 1.002),
                r, g, b, a);
        }

        bufferSource.endBatch(getLineType());
    }
}
