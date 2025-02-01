package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.TidalArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TidalArrowRenderer extends ArrowRenderer<TidalArrow> {

    private static final ResourceLocation TIDAL_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/tidal_arrow.png");

    public TidalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(TidalArrow entity) {
        return TIDAL_ARROW_TEXTURE;
    }
}
