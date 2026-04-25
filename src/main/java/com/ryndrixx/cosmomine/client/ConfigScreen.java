package com.ryndrixx.cosmomine.client;

import com.ryndrixx.cosmomine.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

public class ConfigScreen extends Screen {

    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");

    private final Screen parent;

    // live working copies
    private int maxBlocks;
    private boolean requireCorrectTool;
    private boolean consumeHunger;
    private boolean requireSneakToCycle;
    private String outlineColor;
    private double opacity;
    private double lineWidth;

    // widgets that need reading at save time
    private EditBox colorField;

    public ConfigScreen(Screen parent) {
        super(Component.literal("CosmoMine Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Load current values
        maxBlocks          = Config.MAX_BLOCKS.get();
        requireCorrectTool = Config.REQUIRE_CORRECT_TOOL.get();
        consumeHunger      = Config.CONSUME_HUNGER.get();
        requireSneakToCycle = Config.REQUIRE_SNEAK_TO_CYCLE.get();
        outlineColor       = Config.OUTLINE_COLOR.get();
        opacity            = Config.OUTLINE_OPACITY.get();
        lineWidth          = Config.OUTLINE_WIDTH.get();

        int cx = this.width / 2;
        int leftCol = cx - 155;
        int rightCol = cx + 5;
        int colW = 150;
        int rowH = 24;
        int y = 45;

        // ── Veinmine section ──────────────────────────────────────────────

        // Max Blocks slider (1-256)
        addRenderableWidget(new SimpleSlider(leftCol, y, colW, 20,
            "Max Blocks", maxBlocks, 1, 256, true) {
            @Override protected void onValueChanged(double v) {
                maxBlocks = (int) Math.round(v);
            }
        });

        // Require Correct Tool toggle
        addRenderableWidget(Button.builder(boolLabel("Require Correct Tool", requireCorrectTool), b -> {
            requireCorrectTool = !requireCorrectTool;
            b.setMessage(boolLabel("Require Correct Tool", requireCorrectTool));
        }).bounds(rightCol, y, colW, 20).build());

        y += rowH;

        // Consume Hunger toggle
        addRenderableWidget(Button.builder(boolLabel("Consume Hunger", consumeHunger), b -> {
            consumeHunger = !consumeHunger;
            b.setMessage(boolLabel("Consume Hunger", consumeHunger));
        }).bounds(leftCol, y, colW, 20).build());

        // Require Sneak to Cycle toggle
        addRenderableWidget(Button.builder(boolLabel("Sneak to Cycle", requireSneakToCycle), b -> {
            requireSneakToCycle = !requireSneakToCycle;
            b.setMessage(boolLabel("Sneak to Cycle", requireSneakToCycle));
        }).bounds(rightCol, y, colW, 20).build());

        y += rowH + 16; // gap between sections

        // ── Outline section ───────────────────────────────────────────────

        // Color hex field
        colorField = new EditBox(this.font, leftCol, y, colW, 20,
            Component.literal("Outline Color"));
        colorField.setMaxLength(7);
        colorField.setValue(outlineColor);
        colorField.setHint(Component.literal("#00BFFF"));
        addRenderableWidget(colorField);

        // Opacity slider (0.0-1.0)
        addRenderableWidget(new SimpleSlider(rightCol, y, colW, 20,
            "Opacity", opacity, 0.0, 1.0, false) {
            @Override protected void onValueChanged(double v) {
                opacity = v;
            }
        });

        y += rowH;

        // Line Width slider (0.5-8.0)
        addRenderableWidget(new SimpleSlider(leftCol, y, colW, 20,
            "Line Width", lineWidth, 0.5, 8.0, false) {
            @Override protected void onValueChanged(double v) {
                lineWidth = v;
            }
        });

        y += rowH + 16;

        // ── Done / Cancel ─────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> saveAndClose())
            .bounds(cx - 105, this.height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(cx + 5, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        // Title
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);

        int cx = this.width / 2;
        // Section labels
        g.drawString(this.font, "— Veinmine —", cx - 155, 36, 0xAAAAAA);
        g.drawString(this.font, "— Outline —",  cx - 155, 36 + 24 + 24 + 10, 0xAAAAAA);
        // Color field label above the box
        g.drawString(this.font, "Color (hex)", cx - 155, 36 + 24 + 24 + 10 + 10, 0xCCCCCC);
        g.drawString(this.font, "Opacity",     cx + 5,   36 + 24 + 24 + 10 + 10, 0xCCCCCC);
        g.drawString(this.font, "Line Width",  cx - 155, 36 + 24 + 24 + 10 + 10 + 24, 0xCCCCCC);
    }

    private void saveAndClose() {
        Config.MAX_BLOCKS.set(Math.max(1, Math.min(256, maxBlocks)));
        Config.REQUIRE_CORRECT_TOOL.set(requireCorrectTool);
        Config.CONSUME_HUNGER.set(consumeHunger);
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

    // ── helpers ───────────────────────────────────────────────────────────

    private static Component boolLabel(String name, boolean value) {
        return Component.literal(name + ": ").append(
            value ? Component.literal("ON").withStyle(s -> s.withColor(0x55FF55))
                  : Component.literal("OFF").withStyle(s -> s.withColor(0xFF5555))
        );
    }

    // Generic slider that maps [min, max] → [0, 1] for Minecraft's slider widget
    private abstract static class SimpleSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final boolean isInt;

        SimpleSlider(int x, int y, int w, int h, String label,
                     double initialValue, double min, double max, boolean isInt) {
            super(x, y, w, h, Component.empty(),
                (initialValue - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
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
        protected void applyValue() {
            onValueChanged(realValue());
        }

        protected abstract void onValueChanged(double v);
    }
}
