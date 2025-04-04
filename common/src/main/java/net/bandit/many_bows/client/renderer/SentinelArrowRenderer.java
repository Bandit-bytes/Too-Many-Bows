package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.SentinelArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class SentinelArrowRenderer extends ArrowRenderer<SentinelArrow> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/ironclad_arrow.png");

    public SentinelArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SentinelArrow entity) {
        return TEXTURE;
    }
}
