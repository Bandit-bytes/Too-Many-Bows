package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.FlameArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class FlameArrowRenderer extends ArrowRenderer<FlameArrow, ArrowRenderState> {

    private static final Identifier FLAME_ARROW_TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/flame_arrow.png");

    public FlameArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return FLAME_ARROW_TEXTURE;
    }

}
