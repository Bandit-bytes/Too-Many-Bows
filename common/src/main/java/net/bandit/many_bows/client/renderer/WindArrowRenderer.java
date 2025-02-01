package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.WindProjectile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WindArrowRenderer extends ArrowRenderer<WindProjectile> {

    private static final ResourceLocation WIND_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/wind_arrow.png");


    public WindArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(WindProjectile entity) {
        return WIND_ARROW_TEXTURE;
    }
}
