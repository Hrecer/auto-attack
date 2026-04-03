package com.tfar.autoattack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("autoattack")
@Mod.EventBusSubscriber(value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "autoattack")
public class AutoAttack {
    static final String MODID = "autoattack";
    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (player.getAttackStrengthScale(0.0F) < 1.0F) return;
        if (!(mc.hitResult instanceof EntityHitResult entityHitResult)) return;

        var entity = entityHitResult.getEntity();
        if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(player)) return;
        if (player.isPassenger() && player.getVehicle() == entity) return;

        if (mc.gameMode != null) {
            mc.gameMode.attack(player, entity);
        }
    }
}
