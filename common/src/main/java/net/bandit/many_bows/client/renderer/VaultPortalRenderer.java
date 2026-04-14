package net.bandit.many_bows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bandit.many_bows.entity.VaultPortalEntity;
import net.bandit.many_bows.entity.VaultpiercerArrow;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class VaultPortalRenderer extends EntityRenderer<VaultPortalEntity> {

    private final EntityRenderDispatcher dispatcher;
    private VaultpiercerArrow previewArrow;

    public VaultPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(
            VaultPortalEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        Entity target = entity.getClientTarget();
        if (target == null) {
            return;
        }

        VaultpiercerArrow arrow = getOrCreatePreviewArrow(entity);
        if (arrow == null) {
            return;
        }

        Vec3 from = entity.position();
        Vec3 to = target.getBoundingBox().getCenter().add(0.0D, 0.15D, 0.0D);
        Vec3 diff = to.subtract(from);

        if (diff.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 dir = diff.normalize();

        float yRot = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
        float xRot = (float) -Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));

        arrow.setPos(entity.getX(), entity.getY(), entity.getZ());
        arrow.xo = entity.getX();
        arrow.yo = entity.getY();
        arrow.zo = entity.getZ();

        arrow.setYRot(yRot);
        arrow.setXRot(xRot);
        arrow.yRotO = yRot;
        arrow.xRotO = xRot;

        double offset = 0.12D;

        poseStack.pushPose();
        poseStack.translate(-dir.x * offset, -dir.y * offset, -dir.z * offset);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        dispatcher.render(
                arrow,
                0.0D,
                0.0D,
                0.0D,
                yRot,
                partialTick,
                poseStack,
                buffer,
                packedLight
        );

        poseStack.popPose();
    }

    private VaultpiercerArrow getOrCreatePreviewArrow(VaultPortalEntity entity) {
        if (entity.level() == null) {
            return null;
        }

        if (previewArrow == null || previewArrow.level() != entity.level()) {
            previewArrow = new VaultpiercerArrow(EntityRegistry.VAULTPIERCER_ARROW.get(), entity.level());
            previewArrow.setNoGravity(true);
            previewArrow.pickup = VaultpiercerArrow.Pickup.DISALLOWED;
        }

        return previewArrow;
    }

    @Override
    public ResourceLocation getTextureLocation(VaultPortalEntity entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}