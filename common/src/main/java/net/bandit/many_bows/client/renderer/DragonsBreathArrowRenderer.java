package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.DragonsBreathArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class DragonsBreathArrowRenderer extends ArrowRenderer<DragonsBreathArrow, ArrowRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/arrow.png");

    public DragonsBreathArrowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
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
