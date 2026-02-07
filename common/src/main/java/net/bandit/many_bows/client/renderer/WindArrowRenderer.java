package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.WindProjectile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class WindArrowRenderer extends ArrowRenderer<WindProjectile, ArrowRenderState> {

    private static final Identifier WIND_ARROW_TEXTURE =
            Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/wind_arrow.png");

    public WindArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return WIND_ARROW_TEXTURE;
    }

}
