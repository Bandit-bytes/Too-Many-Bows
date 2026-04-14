package net.bandit.many_bows.neoforge.client.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.many_bows.client.renderer.LanternRenderHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class LanternCurioRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int packedLight,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        M model = renderLayerParent.getModel();

        if (!(model instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        LanternRenderHelper.renderOnRightLeg(
                stack,
                poseStack,
                buffer,
                packedLight,
                slotContext.entity(),
                humanoidModel
        );
    }
}