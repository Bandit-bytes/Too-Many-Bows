package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.TidalArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class TidalArrowRenderer extends ArrowRenderer<TidalArrow, ArrowRenderState> {

    private static final Identifier TIDAL_ARROW_TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/tidal_arrow.png");

    public TidalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return TIDAL_ARROW_TEXTURE;
    }

}
