package net.bandit.many_bows.client.renderer;
import net.bandit.many_bows.entity.RelicArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
public class RelicArrowRenderer extends ArrowRenderer<RelicArrow> {
    public RelicArrowRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(RelicArrow arrow) { return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png"); }
}
