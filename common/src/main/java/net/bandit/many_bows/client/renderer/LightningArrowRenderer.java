package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.LightningArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class LightningArrowRenderer extends ArrowRenderer<LightningArrow, ArrowRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/lightning_arrow.png");


    public LightningArrowRenderer(EntityRendererProvider.Context context) {
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
