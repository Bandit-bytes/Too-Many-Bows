//package net.bandit.many_bows.client.renderer;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.bandit.many_bows.entity.IcicleJavelin;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.entity.EntityRenderer;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.core.particles.ParticleTypes;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.Mth;
//import net.minecraft.world.phys.Vec3;
//import org.jetbrains.annotations.NotNull;
//
//public class IcicleJavelinRenderer extends EntityRenderer<IcicleJavelin> {
//    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("too_many_bows", "textures/entity/frostbite_arrow.png");
//
//    public IcicleJavelinRenderer(EntityRendererProvider.Context context) {
//        super(context);
//    }
//
//    @Override
//    public void render(IcicleJavelin entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//        Vec3 entityPos = entity.position();
//
//        for (int i = 0; i < 5; i++) {
//            double offsetX = Mth.nextDouble(entity.level().random, -0.2, 0.2);
//            double offsetY = Mth.nextDouble(entity.level().random, -0.2, 0.2);
//            double offsetZ = Mth.nextDouble(entity.level().random, -0.2, 0.2);
//            entity.level().addParticle(ParticleTypes.SNOWFLAKE, entityPos.x + offsetX, entityPos.y + offsetY, entityPos.z + offsetZ, 0.0D, -0.05D, 0.0D);
//        }
//    }
//
//
//    @Override
//    public @NotNull ResourceLocation getTextureLocation(IcicleJavelin entity) {
//        return TEXTURE;
//    }
//}
