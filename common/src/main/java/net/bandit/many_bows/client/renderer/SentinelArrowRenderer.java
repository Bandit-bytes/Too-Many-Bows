package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.SentinelWrathArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;


public class SentinelArrowRenderer extends ArrowRenderer<SentinelWrathArrow, ArrowRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/ironclad_arrow.png");

    public SentinelArrowRenderer(EntityRendererProvider.Context context) {
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
