package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Temporary 26.1.2-safe renderer.
 *
 * The old Soulhoard aura renderer used direct textured quad rendering with
 * RenderType/RenderTypes and Camera yaw/pitch helpers that are no longer
 * available in this 26.1 mapping/API set.
 *
 * Keeping these methods as no-ops lets the bow/entity logic compile and run
 * while the aura visual gets rebuilt against the newer render-state/pipeline API.
 */
public final class SoulhoardAuraRenderer {

    private SoulhoardAuraRenderer() {
    }

    public static void renderStoredSouls(
            AbstractClientPlayer player,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer
    ) {
        // Temporarily disabled for the 26.1 port.
    }

    public static void renderFirstPersonWorldSouls(
            LocalPlayer player,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        // Temporarily disabled for the 26.1 port.
    }
}