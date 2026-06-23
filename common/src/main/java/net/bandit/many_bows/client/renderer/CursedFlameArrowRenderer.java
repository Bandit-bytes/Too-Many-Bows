package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.CursedFlameArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class CursedFlameArrowRenderer extends ArrowRenderer<CursedFlameArrow, ArrowRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID,"textures/entity/cursed_flame_arrow.png");


    public CursedFlameArrowRenderer(EntityRendererProvider.Context context) {
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
