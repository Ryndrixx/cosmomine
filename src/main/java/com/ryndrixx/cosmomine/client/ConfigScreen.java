package com.ryndrixx.cosmomine.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.ryndrixx.cosmomine.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {

    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");

    private final Screen parent;

    private int maxBlocks;
    private boolean requireCorrectTool;
    private boolean requireSneakToCycle;
    private String outlineColor;
    private double opacity;
    private double lineWidth;

    private EditBox colorField;

    // y positions set in init(), read in render()
    private int veinmineHeaderY;
    private int outlineHeaderY;
    private int colorLabelY;
    private int opacityLabelY;
    private int lineWidthLabelY;

    public ConfigScreen(Screen parent) {
        super(Component.literal("CosmoMine Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        maxBlocks           = Config.MAX_BLOCKS.get();
        requireCorrectTool  = Config.REQUIRE_CORRECT_TOOL.get();
        requireSneakToCycle = Config.REQUIRE_SNEAK_TO_CYCLE.get();
        outlineColor        = Config.OUTLINE_COLOR.get();
        opacity             = Config.OUTLINE_OPACITY.get();
        lineWidth           = Config.OUTLINE_WIDTH.get();

        int cx       = this.width / 2;
        int leftCol  = cx - 155;
        int rightCol = cx + 5;
        int colW     = 150;
        int rowH     = 26;

        // ── Veinmine ──────────────────────────────────────────────────────
        veinmineHeaderY = 32;
        int y = veinmineHeaderY + 16;  // 48

        // Max Blocks | Require Correct Tool
        addRenderableWidget(new SimpleSlider(leftCol, y, colW, 20,
            "Max Blocks", maxBlocks, 1, 256, true) {
            @Override protected void onValueChanged(double v) { maxBlocks = (int) Math.round(v); }
        });
        addRenderableWidget(Button.builder(boolLabel("Req. Correct Tool", requireCorrectTool), b -> {
            requireCorrectTool = !requireCorrectTool;
            b.setMessage(boolLabel("Req. Correct Tool", requireCorrectTool));
        }).bounds(rightCol, y, colW, 20).build());
        y += rowH;

        // Sneak to Cycle
        addRenderableWidget(Button.builder(boolLabel("Sneak to Cycle", requireSneakToCycle), b -> {
            requireSneakToCycle = !requireSneakToCycle;
            b.setMessage(boolLabel("Sneak to Cycle", requireSneakToCycle));
        }).bounds(leftCol, y, colW, 20).build());
        y += rowH;

        // ── Outline ───────────────────────────────────────────────────────
        y += 18;  // section gap
        outlineHeaderY = y;
        y += 16;  // below header

        colorLabelY   = y;
        opacityLabelY = y;
        y += 12;

        colorField = new EditBox(this.font, leftCol, y, colW, 20,
            Component.literal("Outline Color"));
        colorField.setMaxLength(7);
        colorField.setValue(outlineColor);
        colorField.setHint(Component.literal("#00BFFF"));
        addRenderableWidget(colorField);

        addRenderableWidget(new SimpleSlider(rightCol, y, colW, 20,
            "Opacity", opacity, 0.0, 1.0, false) {
            @Override protected void onValueChanged(double v) { opacity = v; }
        });
        y += rowH;

        lineWidthLabelY = y;
        y += 12;

        addRenderableWidget(new SimpleSlider(leftCol, y, colW, 20,
            "Line Width", lineWidth, 0.5, 8.0, false) {
            @Override protected void onValueChanged(double v) { lineWidth = v; }
        });

        // ── Done / Cancel ─────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> saveAndClose())
            .bounds(cx - 105, this.height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(cx + 5, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx      = this.width / 2;
        int leftCol = cx - 155;
        int rightCol = cx + 5;

        g.drawCenteredString(this.font, this.title, cx, 14, 0xFFFFFF);
        g.drawString(this.font, "— Veinmine —", leftCol,  veinmineHeaderY, 0xAAAAAA);
        g.drawString(this.font, "— Outline —",  leftCol,  outlineHeaderY,  0xAAAAAA);
        g.drawString(this.font, "Color (hex)",  leftCol,  colorLabelY,     0xCCCCCC);
        g.drawString(this.font, "Opacity",      rightCol, opacityLabelY,   0xCCCCCC);
        g.drawString(this.font, "Line Width",   leftCol,  lineWidthLabelY, 0xCCCCCC);
    }

    private void saveAndClose() {
        Config.MAX_BLOCKS.set(Math.max(1, Math.min(256, maxBlocks)));
        Config.REQUIRE_CORRECT_TOOL.set(requireCorrectTool);
        Config.REQUIRE_SNEAK_TO_CYCLE.set(requireSneakToCycle);

        String hex = colorField.getValue().trim();
        if (!hex.startsWith("#")) hex = "#" + hex;
        Config.OUTLINE_COLOR.set(hex);

        Config.OUTLINE_OPACITY.set(opacity);
        Config.OUTLINE_WIDTH.set(lineWidth);

        onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private static Component boolLabel(String name, boolean value) {
        return Component.literal(name + ": ").append(
            value ? Component.literal("ON").withStyle(s -> s.withColor(0x55FF55))
                  : Component.literal("OFF").withStyle(s -> s.withColor(0xFF5555))
        );
    }

    private abstract static class SimpleSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final boolean isInt;

        SimpleSlider(int x, int y, int w, int h, String label,
                     double initialValue, double min, double max, boolean isInt) {
            super(x, y, w, h, Component.empty(), (initialValue - min) / (max - min));
            this.label = label;
            this.min   = min;
            this.max   = max;
            this.isInt = isInt;
            updateMessage();
        }

        private double realValue() { return min + value * (max - min); }

        @Override
        protected void updateMessage() {
            double v = realValue();
            String display = isInt ? String.valueOf((int) Math.round(v))
                                   : (max - min <= 2 ? DF2.format(v) : DF1.format(v));
            setMessage(Component.literal(label + ": " + display));
        }

        @Override
        protected void applyValue() { onValueChanged(realValue()); }

        protected abstract void onValueChanged(double v);
    }
}
