package net.bandit.many_bows.client.renderer;


import net.bandit.many_bows.entity.CursedFlameArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CursedFlameArrowRenderer extends ArrowRenderer<CursedFlameArrow> {

    private static final ResourceLocation CURSED_FLAME_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/cursed_flame_arrow.png");

    public CursedFlameArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(CursedFlameArrow entity) {
        return CURSED_FLAME_ARROW_TEXTURE;
    }
}
