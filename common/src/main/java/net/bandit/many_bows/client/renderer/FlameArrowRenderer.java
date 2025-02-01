package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.FlameArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FlameArrowRenderer extends ArrowRenderer<FlameArrow> {

    private static final ResourceLocation FLAME_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/flame_arrow.png");

    public FlameArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(FlameArrow entity) {
        return FLAME_ARROW_TEXTURE;
    }
}
