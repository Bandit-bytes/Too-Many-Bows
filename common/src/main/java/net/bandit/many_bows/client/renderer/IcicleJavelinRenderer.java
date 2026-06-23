package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.IcicleJavelin;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;


public class IcicleJavelinRenderer extends ArrowRenderer<IcicleJavelin, ArrowRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/frostbite_arrow.png");

    public IcicleJavelinRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return TEXTURE;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
