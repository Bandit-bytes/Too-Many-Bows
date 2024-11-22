package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.HunterArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class HunterArrowRenderer extends ArrowRenderer<HunterArrow> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("too_many_bows", "textures/entity/hunter_arrow.png");

    public HunterArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HunterArrow entity) {
        return TEXTURE;
    }
}
