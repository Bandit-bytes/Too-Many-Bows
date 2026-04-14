package net.bandit.many_bows.fabric.client.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.bandit.many_bows.client.renderer.LanternRenderHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LanternTrinketRenderer implements TrinketRenderer {

    @Override
    public void render(
            ItemStack stack,
            SlotReference slotReference,
            EntityModel<? extends LivingEntity> contextModel,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!(contextModel instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        LanternRenderHelper.renderOnRightLeg(
                stack,
                poseStack,
                buffer,
                packedLight,
                entity,
                humanoidModel
        );
    }
}