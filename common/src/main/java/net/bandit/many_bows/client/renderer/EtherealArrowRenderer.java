//package net.bandit.many_bows.client.renderer;
//
//import net.bandit.many_bows.ManyBowsMod;
//import net.bandit.many_bows.entity.EtherealArrow;
//import net.minecraft.client.renderer.entity.ArrowRenderer;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.resources.ResourceLocation;
//import org.jetbrains.annotations.NotNull;
//
//
//public class EtherealArrowRenderer extends ArrowRenderer<EtherealArrow> {
//    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "textures/entity/ethereal_arrow.png");
//
//    public EtherealArrowRenderer(EntityRendererProvider.Context context) {
//        super(context);
//    }
//
//    @Override
//    public @NotNull ResourceLocation getTextureLocation(EtherealArrow entity) {
//        return TEXTURE;
//    }
//}
