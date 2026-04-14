package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class LanternRenderHelper {

    private LanternRenderHelper() {
    }

    public static void renderOnRightLeg(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            HumanoidModel<?> model
    ) {
        poseStack.pushPose();

        model.rightLeg.translateAndRotate(poseStack);

        poseStack.translate(-0.20D, 0.20D, 0.10D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.60F, 0.60F, 0.60F);

        try {
            LanternRenderState.beginEquippedRender();

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
        } finally {
            LanternRenderState.endEquippedRender();
        }

        poseStack.popPose();
    }
}