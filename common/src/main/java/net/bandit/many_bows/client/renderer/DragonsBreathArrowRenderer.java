package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.many_bows.entity.DragonsBreathArrow;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class DragonsBreathArrowRenderer extends EntityRenderer<DragonsBreathArrow> {

    public DragonsBreathArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public void render(DragonsBreathArrow entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 entityPos = entity.position();
        for (int i = 0; i < 5; i++) {
            entity.level().addParticle(ParticleTypes.DRAGON_BREATH, entityPos.x, entityPos.y, entityPos.z, 0.0D, 0.0D, 0.0D);
        }
    }
    @Override
    public ResourceLocation getTextureLocation(DragonsBreathArrow entity) {
        return null;
    }
}
