package net.bandit.many_bows.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.many_bows.client.renderer.SoulhoardAuraRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void too_many_bows$renderSoulhoardFirstPersonAura(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        if (!this.minecraft.options.getCameraType().isFirstPerson()) return;

        LocalPlayer player = this.minecraft.player;
        if (player == null) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        PoseStack poseStack = new PoseStack();
        SoulhoardAuraRenderer.renderFirstPersonWorldSouls(
                player,
                partialTick,
                poseStack,
                this.renderBuffers.bufferSource(),
                camera.getPosition().x,
                camera.getPosition().y,
                camera.getPosition().z
        );
    }
}