package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * Temporary 26.2-safe renderer.
 */
public final class SoulhoardAuraRenderer {

    private SoulhoardAuraRenderer() {
    }

    public static void renderStoredSouls(
            AbstractClientPlayer player,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        // Temporarily disabled for the 26.2 port.
    }

    public static void renderFirstPersonWorldSouls(
            LocalPlayer player,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        // Temporarily disabled for the 26.2 port.
    }
}