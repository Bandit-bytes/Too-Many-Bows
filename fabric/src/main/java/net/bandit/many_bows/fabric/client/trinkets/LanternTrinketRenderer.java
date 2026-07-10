package net.bandit.many_bows.fabric.client.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import net.bandit.many_bows.client.renderer.LanternRenderHelper;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class LanternTrinketRenderer implements TrinketRenderer {

    @Override
    public void submit(
            ItemStack stack,
            TrinketSlotAccess slotReference,
            EntityModel<? extends LivingEntityRenderState> contextModel,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            LivingEntityRenderState renderState,
            float limbSwing,
            float limbSwingAmount
    ) {
        if (!(contextModel instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        boolean cursed = stack.is(ItemRegistry.CURSED_LANTERN.get());

        LanternRenderHelper.submitOnRightLeg(
                stack,
                poseStack,
                submitNodeCollector,
                packedLight,
                renderState,
                humanoidModel,
                cursed
        );
    }
}