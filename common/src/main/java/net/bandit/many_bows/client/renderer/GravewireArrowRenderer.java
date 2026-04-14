package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.GravewireArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GravewireArrowRenderer extends ArrowRenderer<GravewireArrow> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/gravewire_arrow.png");

    public GravewireArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GravewireArrow arrow) {
        return TEXTURE;
    }
}