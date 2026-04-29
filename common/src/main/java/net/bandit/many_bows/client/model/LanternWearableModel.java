package net.bandit.many_bows.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.PartPose;

public class LanternWearableModel extends EntityModel<LivingEntityRenderState> {

    public LanternWearableModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();


        root.addOrReplaceChild(
                "lantern_body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(0, 14)
                        .addBox(-2.0F, -5.5F, -2.0F, 4.0F, 1.5F, 4.0F, CubeDeformation.NONE)
                        .texOffs(16, 14)
                        .addBox(-2.0F, 4.0F, -2.0F, 4.0F, 1.0F, 4.0F, CubeDeformation.NONE)
                        .texOffs(20, 0)
                        .addBox(-1.0F, -7.0F, -1.0F, 2.0F, 1.5F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 32, 32);
    }
}