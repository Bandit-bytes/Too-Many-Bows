package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.FlameArrow;
import net.bandit.many_bows.entity.SolarArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class SolarArrowRenderer extends ArrowRenderer<SolarArrow, ArrowRenderState> {

    private static final Identifier FLAME_ARROW_TEXTURE = Identifier.fromNamespaceAndPath("too_many_bows", "textures/entity/flame_arrow.png");

    public SolarArrowRenderer(EntityRendererProvider.Context context) {
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
