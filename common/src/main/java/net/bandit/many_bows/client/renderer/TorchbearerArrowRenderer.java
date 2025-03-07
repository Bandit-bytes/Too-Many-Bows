package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.TorchbearerArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class TorchbearerArrowRenderer extends ArrowRenderer<TorchbearerArrow> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/arrow.png");

    public TorchbearerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(TorchbearerArrow entity) {
        return TEXTURE;
    }
}
