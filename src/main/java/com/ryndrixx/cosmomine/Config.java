package com.ryndrixx.cosmomine;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_BLOCKS;
    public static final ModConfigSpec.BooleanValue REQUIRE_CORRECT_TOOL;
    public static final ModConfigSpec.BooleanValue REQUIRE_SNEAK_TO_CYCLE;

    public static final ModConfigSpec.ConfigValue<String> OUTLINE_COLOR;
    public static final ModConfigSpec.DoubleValue OUTLINE_OPACITY;
    public static final ModConfigSpec.DoubleValue OUTLINE_WIDTH;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("veinmine");

        MAX_BLOCKS = BUILDER
            .comment("Maximum blocks to mine per veinmine operation (1-256, default 64)")
            .defineInRange("maxBlocks", 64, 1, 256);

        REQUIRE_CORRECT_TOOL = BUILDER
            .comment("Require the correct tool to veinmine a block (default true)")
            .define("requireCorrectTool", true);

        REQUIRE_SNEAK_TO_CYCLE = BUILDER
            .comment("Require holding Sneak (shift) in addition to the veinmine key to scroll-cycle shape modes (default false)")
            .define("requireSneakToCycle", false);

        BUILDER.pop();
        BUILDER.push("outline");

        OUTLINE_COLOR = BUILDER
            .comment("Highlight outline color as a hex string (e.g. #00BFFF for cyan, #FFFFFF for white)")
            .define("color", "#00BFFF");

        OUTLINE_OPACITY = BUILDER
            .comment("Highlight outline opacity (0.0 = invisible, 1.0 = fully opaque, default 0.9)")
            .defineInRange("opacity", 0.9, 0.0, 1.0);

        OUTLINE_WIDTH = BUILDER
            .comment("Highlight outline line width in pixels (0.5-8.0, default 1.5)")
            .defineInRange("lineWidth", 1.5, 0.5, 8.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    /** Parse the hex color config string into r/g/b floats (0.0-1.0). Falls back to cyan on bad input. */
    public static float[] parseOutlineColor() {
        try {
            String hex = OUTLINE_COLOR.get().trim().replace("#", "");
            int rgb = Integer.parseUnsignedInt(hex, 16);
            return new float[]{
                ((rgb >> 16) & 0xFF) / 255f,
                ((rgb >> 8)  & 0xFF) / 255f,
                ( rgb        & 0xFF) / 255f
            };
        } catch (Exception e) {
            return new float[]{0f, 0.75f, 1f}; // default cyan
        }
    }
}
