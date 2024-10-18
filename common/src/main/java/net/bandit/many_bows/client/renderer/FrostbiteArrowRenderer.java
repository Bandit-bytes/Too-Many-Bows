package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.FrostbiteArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FrostbiteArrowRenderer extends ArrowRenderer<FrostbiteArrow> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("too_many_bows", "textures/entity/frostbite_arrow.png");

    public FrostbiteArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public @NotNull ResourceLocation getTextureLocation(FrostbiteArrow entity) {
        return TEXTURE;
    }
}
