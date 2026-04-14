package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.SoulhoardArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SoulhoardArrowRenderer extends ArrowRenderer<SoulhoardArrow> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/soulhoard_arrow.png");

    public SoulhoardArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SoulhoardArrow arrow) {
        return TEXTURE;
    }
}