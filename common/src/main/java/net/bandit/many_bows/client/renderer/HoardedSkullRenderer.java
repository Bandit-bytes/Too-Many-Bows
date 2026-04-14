package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.HoardedSkullEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class HoardedSkullRenderer extends EntityRenderer<HoardedSkullEntity> {

    private static final ResourceLocation SKULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/effects/ancient_skull.png");

    private static final ResourceLocation FLAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/effects/cursed_flame.png");

    private static final int FULL_BRIGHT = 0xF000F0;

    public HoardedSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            HoardedSkullEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        float age = entity.tickCount + partialTick;

        Vec3 motion = entity.getDeltaMovement();
        double speed = motion.length();

        Vec3 dir = speed > 1.0E-4D ? motion.normalize() : new Vec3(0.0D, 0.0D, 0.0D);

        float bob = Mth.sin(age * 0.18F) * 0.025F;
        float pulse = 1.0F + Mth.sin(age * 0.22F) * 0.04F;

        float skullSize = Math.max(0.06F, entity.getRenderScale() * 0.52F) * pulse;
        float flameSize = skullSize * 0.92F;

        float trailX = (float) (-dir.x * 0.16D);
        float trailY = (float) (-dir.y * 0.10D);
        float trailZ = (float) (-dir.z * 0.16D);

        int skullAlpha = 235;
        int flameAlpha = 145 + (int) (Mth.sin(age * 0.28F) * 20.0F);

        Camera camera = this.entityRenderDispatcher.camera;

        poseStack.pushPose();
        poseStack.translate(0.0D, bob, 0.0D);

        poseStack.pushPose();
        poseStack.translate(trailX, trailY, trailZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        float flameStretch = 1.0F + (float) Math.min(0.45D, speed * 1.8D);
        poseStack.scale(1.0F, flameStretch, 1.0F);

        PoseStack.Pose flamePose = poseStack.last();
        VertexConsumer flameVc = buffer.getBuffer(RenderType.entityTranslucent(FLAME_TEXTURE));
        renderQuad(flameVc, flamePose, FULL_BRIGHT, flameSize, 255, 255, 255, flameAlpha);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        PoseStack.Pose skullPose = poseStack.last();
        VertexConsumer skullVc = buffer.getBuffer(RenderType.entityTranslucent(SKULL_TEXTURE));
        renderQuad(skullVc, skullPose, FULL_BRIGHT, skullSize, 255, 255, 255, skullAlpha);
        poseStack.popPose();

        poseStack.popPose();
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

    @Override
    public ResourceLocation getTextureLocation(HoardedSkullEntity entity) {
        return SKULL_TEXTURE;
    }
}