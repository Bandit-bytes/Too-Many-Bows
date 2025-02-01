package net.bandit.many_bows.client.renderer;



import net.bandit.many_bows.entity.AstralArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class AstralArrowRenderer extends ArrowRenderer<AstralArrow> {
    private static final ResourceLocation TEXTURE =ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/astral_arrow.png");


    public AstralArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AstralArrow entity) {
        return TEXTURE;
    }
}
