package com.tfar.autoattack;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client-side configuration for Auto Attack.
 *
 * The file is generated at {@code config/autoattack-client.toml} and can be edited
 * in game (Mods -> Auto Attack -> Config) or by hand.
 */
public final class AutoAttackConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ATTACK_WHILE_BLOCKING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Auto Attack - client settings").push("general");

        ATTACK_WHILE_BLOCKING = builder
                .comment("Allow auto-attacking while holding up a shield (blocking).",
                        "Vanilla normally prevents attacking while using any item; this makes an",
                        "exception for shields, so you can keep your guard up and still auto-attack.",
                        "Enabled by default; set to false to restore vanilla behaviour.",
                        "Works with vanilla shields as well as modded shields that support the",
                        "forge:shield_block tool action.")
                .define("attack_while_blocking", true);

        builder.pop();

        SPEC = builder.build();
    }

    private AutoAttackConfig() {
    }
}
