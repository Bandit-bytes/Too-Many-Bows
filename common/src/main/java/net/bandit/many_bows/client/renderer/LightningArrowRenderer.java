package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.LightningArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LightningArrowRenderer extends ArrowRenderer<LightningArrow> {

    private static final ResourceLocation LIGHTNING_ARROW_TEXTURE = new ResourceLocation("too_many_bows", "textures/entity/lightning_arrow.png");


    public LightningArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(LightningArrow entity) {
        return LIGHTNING_ARROW_TEXTURE;
    }
}
