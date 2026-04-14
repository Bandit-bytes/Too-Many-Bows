package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.item.SoulhoardBow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class SoulhoardAuraRenderer {

    private static final ResourceLocation SKULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/effects/ancient_skull.png");

    private static final int FULL_BRIGHT = 0xF000F0;

    private SoulhoardAuraRenderer() {
    }

    public static void renderStoredSouls(
            AbstractClientPlayer player,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer
    ) {
        if (player.isInvisible() || player.isSpectator()) {
            return;
        }

        ItemStack soulhoard = getHeldSoulhoard(player);
        if (soulhoard.isEmpty()) {
            return;
        }

        int souls = SoulhoardBow.getSoulCount(soulhoard);
        if (souls <= 0) {
            return;
        }

        // Skip local player in first person — handled by world-space renderer
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        float age = player.tickCount + partialTick;
        float baseY = player.isCrouching() ? 1.0F : 1.18F;
        float radius = 0.75F;

        for (int i = 0; i < souls; i++) {
            float orbitOffset = (float) ((Math.PI * 2.0D) / Math.max(1, souls)) * i;
            float angle = age * 0.11F + orbitOffset;

            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;
            float y = baseY + Mth.sin(age * 0.16F + i * 1.7F) * 0.06F;

            float pulse = 1.0F + Mth.sin(age * 0.25F + i * 0.9F) * 0.08F;
            float skullSize = 0.16F * pulse;
            float roll = age * 5.0F + (i * 24.0F);
            int skullAlpha = 235;

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            Camera camera = mc.gameRenderer.getMainCamera();
            poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

            PoseStack.Pose skullPose = poseStack.last();
            VertexConsumer skullVc = buffer.getBuffer(RenderType.entityTranslucent(SKULL_TEXTURE));
            renderQuad(skullVc, skullPose, FULL_BRIGHT, skullSize, 255, 255, 255, skullAlpha);

            poseStack.popPose();
        }
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
        if (player == null || player.isInvisible() || player.isSpectator()) {
            return;
        }

        ItemStack soulhoard = getHeldSoulhoard(player);
        if (soulhoard.isEmpty()) {
            return;
        }

        int souls = SoulhoardBow.getSoulCount(soulhoard);
        if (souls <= 0) {
            return;
        }

        float age = player.tickCount + partialTick;
        double px = Mth.lerp(partialTick, player.xOld, player.getX());
        double py = Mth.lerp(partialTick, player.yOld, player.getY());
        double pz = Mth.lerp(partialTick, player.zOld, player.getZ());

        double centerX = px;
        double centerY = py + (player.isCrouching() ? 0.90D : 1.02D);
        double centerZ = pz;

        poseStack.pushPose();
        poseStack.translate(centerX - cameraX, centerY - cameraY, centerZ - cameraZ);

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        for (int i = 0; i < souls; i++) {
            float orbitOffset = (float) ((Math.PI * 2.0D) / Math.max(1, souls)) * i;
            float angle = age * 0.11F + orbitOffset;

            float x = Mth.cos(angle) * 0.72F;
            float z = Mth.sin(angle) * 0.72F;
            float y = Mth.sin(age * 0.16F + i * 1.7F) * 0.06F;

            float pulse = 1.0F + Mth.sin(age * 0.25F + i * 0.9F) * 0.08F;
            float skullSize = 0.17F * pulse;
            float roll = age * 5.0F + (i * 24.0F);
            int skullAlpha = 235;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

            PoseStack.Pose skullPose = poseStack.last();
            VertexConsumer skullVc = buffer.getBuffer(RenderType.entityTranslucent(SKULL_TEXTURE));
            renderQuad(skullVc, skullPose, FULL_BRIGHT, skullSize, 255, 255, 255, skullAlpha);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static ItemStack getHeldSoulhoard(AbstractClientPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && main.is(ItemRegistry.SOULHOARD.get())) {
            return main;
        }

        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && off.is(ItemRegistry.SOULHOARD.get())) {
            return off;
        }

        return ItemStack.EMPTY;
    }

    private static void renderQuad(
            VertexConsumer vc,
            PoseStack.Pose pose,
            int light,
            float size,
            int r,
            int g,
            int b,
            int a
    ) {
        vertex(vc, pose, light, -size, -size, 0.0F, 0.0F, 1.0F, r, g, b, a);
        vertex(vc, pose, light,  size, -size, 0.0F, 1.0F, 1.0F, r, g, b, a);
        vertex(vc, pose, light,  size,  size, 0.0F, 1.0F, 0.0F, r, g, b, a);
        vertex(vc, pose, light, -size,  size, 0.0F, 0.0F, 0.0F, r, g, b, a);
    }

    private static void vertex(
            VertexConsumer vc,
            PoseStack.Pose pose,
            int light,
            float x,
            float y,
            float z,
            float u,
            float v,
            int r,
            int g,
            int b,
            int a
    ) {
        vc.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}