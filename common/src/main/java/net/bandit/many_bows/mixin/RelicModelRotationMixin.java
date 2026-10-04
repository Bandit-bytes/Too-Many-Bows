package net.bandit.many_bows.mixin;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public abstract class RelicModelRotationMixin {
    @Inject(method = "getFrom", at = @At("HEAD"), cancellable = true)
    private void tmb$relicFrom(JsonObject json, CallbackInfoReturnable<org.joml.Vector3f> cir) {
        org.joml.Vector3f vector = tmb$markedVector(json, "from");
        if (vector != null) cir.setReturnValue(vector);
    }
    @Inject(method = "getTo", at = @At("HEAD"), cancellable = true)
    private void tmb$relicTo(JsonObject json, CallbackInfoReturnable<org.joml.Vector3f> cir) {
        org.joml.Vector3f vector = tmb$markedVector(json, "to");
        if (vector != null) cir.setReturnValue(vector);
    }
    @org.spongepowered.asm.mixin.Unique
    private static org.joml.Vector3f tmb$markedVector(JsonObject json, String member) {
        if (!json.has("tmb_relic_rotation") || !json.get("tmb_relic_rotation").getAsBoolean() || !json.has(member)) return null;
        var values = json.getAsJsonArray(member); if (values.size() != 3) return null;
        float x = values.get(0).getAsFloat(), y = values.get(1).getAsFloat(), z = values.get(2).getAsFloat();
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z) || Math.abs(x) > 64 || Math.abs(y) > 64 || Math.abs(z) > 64) return null;
        return new org.joml.Vector3f(x, y, z);
    }
    @Inject(method = "getVector3f", at = @At("HEAD"), cancellable = true)
    private void tmb$relicBounds(JsonObject json, String member, CallbackInfoReturnable<org.joml.Vector3f> cir) {
        if (!json.has("tmb_relic_rotation") || !json.get("tmb_relic_rotation").getAsBoolean() || !json.has(member)) return;
        var values = json.getAsJsonArray(member); if (values.size() != 3) return;
        float x = values.get(0).getAsFloat(), y = values.get(1).getAsFloat(), z = values.get(2).getAsFloat();
        if (Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z) && Math.abs(x) <= 64 && Math.abs(y) <= 64 && Math.abs(z) <= 64) cir.setReturnValue(new org.joml.Vector3f(x, y, z));
    }

    @Inject(method = "getAngle", at = @At("HEAD"), cancellable = true)
    private void tmb$relicAngle(JsonObject rotation, CallbackInfoReturnable<Float> cir) {
        if (rotation.has("tmb_relic_rotation") && rotation.get("tmb_relic_rotation").getAsBoolean()) {
            float angle = rotation.get("angle").getAsFloat();
            if (Float.isFinite(angle) && Math.abs(angle) <= 180) cir.setReturnValue(angle);
        }
    }
}
