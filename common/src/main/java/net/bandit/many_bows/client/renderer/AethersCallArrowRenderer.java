package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.many_bows.entity.AethersCallArrow;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;

public class AethersCallArrowRenderer extends EntityRenderer<AethersCallArrow> {

    public AethersCallArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AethersCallArrow entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        var rand = entity.level().random;

        for (int i = 0; i < 3; i++) {
            double ox = rand.nextDouble() * 0.2 - 0.1;
            double oy = rand.nextDouble() * 0.2;
            double oz = rand.nextDouble() * 0.2 - 0.1;

            entity.level().addParticle(
                    ParticleTypes.END_ROD,
                    entity.getX() + ox,
                    entity.getY() + oy,
                    entity.getZ() + oz,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AethersCallArrow entity) {
        return null;
    }
}
