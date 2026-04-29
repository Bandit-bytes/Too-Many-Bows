package net.bandit.many_bows.neoforge.client.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.many_bows.client.renderer.LanternRenderHelper;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class LanternCurioRenderer implements ICurioRenderer {

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            S renderState,
            RenderLayerParent<S, M> renderLayerParent,
            EntityRendererProvider.Context context,
            float yRotation,
            float xRotation
    ) {
        M model = renderLayerParent.getModel();

        if (!(model instanceof HumanoidModel<?> humanoidModel)) {
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