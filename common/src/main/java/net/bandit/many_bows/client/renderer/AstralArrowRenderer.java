package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.AstralArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;


public class AstralArrowRenderer extends ArrowRenderer<AstralArrow, ArrowRenderState> {
    private static final Identifier TEXTURE =Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/astral_arrow.png");


    public AstralArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return TEXTURE;
    }

}
