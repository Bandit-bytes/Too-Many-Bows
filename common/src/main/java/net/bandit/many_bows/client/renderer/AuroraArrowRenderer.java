package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.AuroraArrowEntity;
import net.bandit.many_bows.entity.HunterXPArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class AuroraArrowRenderer extends ArrowRenderer<AuroraArrowEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("too_many_bows", "textures/entity/aurora_arrow.png");

    public AuroraArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AuroraArrowEntity entity) {
        return TEXTURE;
    }
}
