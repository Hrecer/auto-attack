package com.tfar.autoattack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("autoattack")
@Mod.EventBusSubscriber(value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "autoattack")
public class AutoAttack {
    static final String MODID = "autoattack";

    public AutoAttack(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, AutoAttackConfig.SPEC);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Only act while the attack key is actually held and no screen is open.
        if (mc.screen != null) return;
        if (!mc.options.keyAttack.isDown()) return;

        // Mechanism must stay identical to vanilla: an attack only happens when the
        // crosshair is actually on a target. Swinging at nothing (air) has no effect
        // and produces no swing animation.
        if (!(mc.hitResult instanceof EntityHitResult entityHitResult)) return;
        Entity entity = entityHitResult.getEntity();
        if (entity == null || entity.isSpectator()) return;

        // Never attack your own pets, nor the entity you are currently riding.
        if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(player)) return;
        if (player.isPassenger() && player.getVehicle() == entity) return;

        // Vanilla forbids attacking while any item is being used (eating, drawing a bow,
        // drinking, holding up a shield, ...). We keep that rule for full consistency,
        // except for one optional case: keeping the auto attack running while blocking
        // with a shield (configurable, and it works for modded shields too because we
        // test the shield_block tool action instead of a specific item class).
        if (player.isUsingItem()) {
            boolean usingShield = player.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK);
            if (!(usingShield && AutoAttackConfig.ATTACK_WHILE_BLOCKING.get())) return;
        }

        // Only swing once the attack cooldown is fully recharged. Reading it from the
        // player (instead of a hardcoded value) means the weapon's attack-speed attribute
        // decides the cadence, so high attack-speed weapons attack faster and
        // no-cooldown mods attack every tick automatically - no special casing needed.
        if (player.getAttackStrengthScale(0.0F) < 1.0F) return;

        if (mc.gameMode == null) return;

        // Replicate vanilla Minecraft#startAttack for the ENTITY case, keeping every
        // vanilla/Forge hook intact so attack-modifying mods still see and can react to
        // the attack. The missing piece that caused the animation to only play on the
        // first hit was the hand swing below: gameMode.attack() deals the damage but does
        // NOT play the swing animation, vanilla always calls swing() right after it.
        var inputEvent = ForgeHooksClient.onClickInput(0, mc.options.keyAttack, InteractionHand.MAIN_HAND);
        if (!inputEvent.isCanceled()) {
            mc.gameMode.attack(player, entity);
        }
        if (inputEvent.shouldSwingHand()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
