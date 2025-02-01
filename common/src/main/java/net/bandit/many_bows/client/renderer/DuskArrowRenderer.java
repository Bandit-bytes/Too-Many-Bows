package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.DuskReaperArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class DuskArrowRenderer extends ArrowRenderer<DuskReaperArrow> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/dusk_reaper_arrow.png");

    public DuskArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DuskReaperArrow entity) {
        return TEXTURE;
    }
}
