package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.AuroraArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;


public class AuroraArrowRenderer extends ArrowRenderer<AuroraArrowEntity, ArrowRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/aurora_arrow.png");

    public AuroraArrowRenderer(EntityRendererProvider.Context context) {
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

