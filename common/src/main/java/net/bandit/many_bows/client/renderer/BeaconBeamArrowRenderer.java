package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bandit.many_bows.entity.BeaconBeamArrow;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class BeaconBeamArrowRenderer extends EntityRenderer<BeaconBeamArrow> {

    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/beacon_beam.png");

    public BeaconBeamArrowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BeaconBeamArrow entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        // --- Use arrow-style rotations (prevents weird rolling/spinning) ---
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(pitch));

        float length = 0.9F;          // keep short so it looks like a "bolt" on the arrow
        float radius = 0.08F;         // thinner looks better with beacon texture

        float time = entity.level().getGameTime() + partialTicks;
        float vScroll = time * 0.02F;

        RenderType type = RenderType.beaconBeam(BEAM_TEXTURE, false);
        VertexConsumer vc = bufferSource.getBuffer(type);

        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();

        drawBeamQuad(vc, last, pose, packedLight, radius, length, vScroll, 0.85F);

        // plane 2 (crossed)
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
        last = poseStack.last();
        pose = last.pose();
        drawBeamQuad(vc, last, pose, packedLight, radius, length, vScroll, 0.85F);

        poseStack.popPose();
    }

    private static void drawBeamQuad(VertexConsumer vc,
                                     PoseStack.Pose posePose,
                                     Matrix4f pose,
                                     int packedLight,
                                     float radius,
                                     float length,
                                     float vScroll,
                                     float alpha) {

        float x1 = -radius;
        float x2 =  radius;

        float yTop =  radius;
        float yBot = -radius;

        float v0 = -vScroll;
        float v1 = length - vScroll;

        float r = 1.0F, g = 1.0F, b = 1.0F;
        float nx = 0.0F, ny = 1.0F, nz = 0.0F;

        vc.addVertex(pose, x1, yBot, 0).setColor(r, g, b, alpha).setUv(0.0F, v0).setOverlay(0).setLight(packedLight).setNormal(posePose, nx, ny, nz);
        vc.addVertex(pose, x2, yBot, 0).setColor(r, g, b, alpha).setUv(1.0F, v0).setOverlay(0).setLight(packedLight).setNormal(posePose, nx, ny, nz);
        vc.addVertex(pose, x2, yTop, length).setColor(r, g, b, alpha).setUv(1.0F, v1).setOverlay(0).setLight(packedLight).setNormal(posePose, nx, ny, nz);
        vc.addVertex(pose, x1, yTop, length).setColor(r, g, b, alpha).setUv(0.0F, v1).setOverlay(0).setLight(packedLight).setNormal(posePose, nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(BeaconBeamArrow entity) {
        return BEAM_TEXTURE;
    }
}
