package net.bandit.many_bows.mixin;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets = "net.minecraft.client.resources.model.cuboid.CuboidModelElement$Deserializer")
public abstract class RelicModelBoundsMixin {
    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private static void tmb$bounds(JsonObject json, String member, CallbackInfoReturnable<Vector3f> cir) {
        if (!json.has("tmb_relic_rotation") || !json.get("tmb_relic_rotation").getAsBoolean() || !json.has(member)) return;
        var values = json.getAsJsonArray(member);
        if (values.size() != 3) return;
        float x = values.get(0).getAsFloat(), y = values.get(1).getAsFloat(), z = values.get(2).getAsFloat();
        if (Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)
                && Math.abs(x) <= 64 && Math.abs(y) <= 64 && Math.abs(z) <= 64) cir.setReturnValue(new Vector3f(x,y,z));
    }
}
