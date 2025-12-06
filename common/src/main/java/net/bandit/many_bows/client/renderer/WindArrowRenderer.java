package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.many_bows.entity.WindProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;

public class WindArrowRenderer extends ArrowRenderer<WindProjectile> {

    private static final ResourceLocation WIND_ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/wind_arrow.png");

    public WindArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            WindProjectile entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        var level = entity.level();
        if (level != null && level.isClientSide) {

            for (int i = 0; i < 2; i++) {
                double ox = (level.random.nextDouble() - 0.5) * 0.2;
                double oy = (level.random.nextDouble()) * 0.15;
                double oz = (level.random.nextDouble() - 0.5) * 0.2;

                level.addParticle(
                        ParticleTypes.CLOUD,
                        entity.getX() + ox,
                        entity.getY() + oy,
                        entity.getZ() + oz,
                        0.0,
                        0.01,
                        0.0
                );
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(WindProjectile entity) {
        return WIND_ARROW_TEXTURE;
    }
}
