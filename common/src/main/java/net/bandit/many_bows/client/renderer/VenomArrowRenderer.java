package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.VenomArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class VenomArrowRenderer extends ArrowRenderer<VenomArrow> {

    private static final ResourceLocation VENOM_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/venom_arrow.png");

    public VenomArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(VenomArrow entity) {
        return VENOM_ARROW_TEXTURE;
    }
}
