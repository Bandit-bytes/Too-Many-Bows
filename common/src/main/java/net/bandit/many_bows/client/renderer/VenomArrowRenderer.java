package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.VenomArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class VenomArrowRenderer extends ArrowRenderer<VenomArrow, ArrowRenderState> {

    private static final Identifier VENOM_ARROW_TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/venom_arrow.png");

    public VenomArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return VENOM_ARROW_TEXTURE;
    }
}
