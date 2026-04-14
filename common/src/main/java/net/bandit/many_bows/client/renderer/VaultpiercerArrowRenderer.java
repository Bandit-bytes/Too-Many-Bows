package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.VaultpiercerArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class VaultpiercerArrowRenderer extends ArrowRenderer<VaultpiercerArrow> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/vaultpiercer_arrow.png");

    public VaultpiercerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(VaultpiercerArrow arrow) {
        return TEXTURE;
    }
}