package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.IronCladArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class IroncladArrowRenderer extends ArrowRenderer<IronCladArrow> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/ironclad_arrow.png");

    public IroncladArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(IronCladArrow entity) {
        return TEXTURE;
    }
}
