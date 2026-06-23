package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.AncientSageArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class AncientSageArrowRenderer extends ArrowRenderer<AncientSageArrow, ArrowRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/ancient_sage_arrow.png");

    public AncientSageArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }
}
