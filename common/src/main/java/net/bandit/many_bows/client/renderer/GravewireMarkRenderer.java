package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.GravewireBowConfig;
import net.bandit.many_bows.entity.GravewireMarkEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GravewireMarkRenderer extends EntityRenderer<GravewireMarkEntity> {

    private static final ResourceLocation MARK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/effects/gravewire_mark.png");

    private static final String CONFIG_NAME = "gravewire_bow";
    private static final int FULL_BRIGHT = 0xF000F0;

    public GravewireMarkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static GravewireBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, GravewireBowConfig.class, GravewireBowConfig::new);
    }

    @Override
    public void render(
            GravewireMarkEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        GravewireBowConfig config = config();

        float totalLife = Math.max(1.0F, entity.getInitialLifetime());
        float age = entity.tickCount + partialTick;
        float progress = Mth.clamp(age / totalLife, 0.0F, 1.0F);

        float pulse = 1.0F + Mth.sin(age * 0.18F) * 0.08F;

        float fadeStart = 0.35F;
        float fadeProgress = progress <= fadeStart
                ? 0.0F
                : (progress - fadeStart) / (1.0F - fadeStart);

        float alpha = 1.0F - fadeProgress;
        float shrink = 1.0F - (fadeProgress * 0.18F);
        float size = Math.max(0.05F, config.grave_mark_scale) * pulse * shrink;

        int a = (int) (alpha * 255.0F);

        poseStack.pushPose();

        Camera camera = this.entityRenderDispatcher.camera;
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(MARK_TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        vertex(vc, pose, FULL_BRIGHT, -size, -size, 0.0F, 0.0F, 1.0F, 255, 255, 255, a);
        vertex(vc, pose, FULL_BRIGHT,  size, -size, 0.0F, 1.0F, 1.0F, 255, 255, 255, a);
        vertex(vc, pose, FULL_BRIGHT,  size,  size, 0.0F, 1.0F, 0.0F, 255, 255, 255, a);
        vertex(vc, pose, FULL_BRIGHT, -size,  size, 0.0F, 0.0F, 0.0F, 255, 255, 255, a);

        poseStack.popPose();
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
    public ResourceLocation getTextureLocation(GravewireMarkEntity entity) {
        return MARK_TEXTURE;
    }
}