package com.ryndrixx.cosmomine;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_BLOCKS;
    public static final ModConfigSpec.BooleanValue REQUIRE_CORRECT_TOOL;
    public static final ModConfigSpec.BooleanValue CONSUME_HUNGER;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("veinmine");

        MAX_BLOCKS = BUILDER
            .comment("Maximum blocks to mine per veinmine operation (1-256, default 64)")
            .defineInRange("maxBlocks", 64, 1, 256);

        REQUIRE_CORRECT_TOOL = BUILDER
            .comment("Require the correct tool to veinmine a block (default true)")
            .define("requireCorrectTool", true);

        CONSUME_HUNGER = BUILDER
            .comment("Each extra block mined consumes a small amount of hunger (default false)")
            .define("consumeHunger", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
