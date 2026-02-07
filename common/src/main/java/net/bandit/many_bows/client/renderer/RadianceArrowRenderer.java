package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.RadiantArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;



public class RadianceArrowRenderer extends ArrowRenderer<RadiantArrow, ArrowRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/ancient_sage_arrow.png");

    public RadianceArrowRenderer(EntityRendererProvider.Context context) {
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
