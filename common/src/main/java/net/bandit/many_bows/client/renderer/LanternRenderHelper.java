package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class LanternRenderHelper {

    private static final Identifier SOUL_LANTERN_EQUIPPED_MODEL =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "soul_lantern_equipped");

    private static final Identifier CURSED_LANTERN_EQUIPPED_MODEL =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "cursed_lantern_equipped");

    private LanternRenderHelper() {
    }

    public static void submitOnRightLeg(
            ItemStack stack,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            LivingEntityRenderState renderState,
            HumanoidModel<?> humanoidModel,
            boolean cursed
    ) {
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        humanoidModel.rightLeg.translateAndRotate(poseStack);

        poseStack.translate(-0.20D, 0.20D, 0.10D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.55F, 0.55F, 0.55F);
        ItemStack renderStack = stack.copy();
        renderStack.set(
                DataComponents.ITEM_MODEL,
                cursed ? CURSED_LANTERN_EQUIPPED_MODEL : SOUL_LANTERN_EQUIPPED_MODEL
        );

        ItemStackRenderState itemState = new ItemStackRenderState();

        Minecraft.getInstance()
                .getItemModelResolver()
                .updateForTopItem(
                        itemState,
                        renderStack,
                        ItemDisplayContext.FIXED,
                        Minecraft.getInstance().level,
                        null,
                        0
                );

        itemState.submit(
                poseStack,
                submitNodeCollector,
                packedLight,
                -1,
                0
        );

        poseStack.popPose();
    }
}