package net.bandit.many_bows.mixin;

import net.bandit.many_bows.registry.EffectRegistry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void preventHealing(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(EffectRegistry.CURSED_FLAME)) {
            ci.cancel();
        }
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void preventExtinguishInWater(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.hasEffect(EffectRegistry.CURSED_FLAME)) {
            if (entity.isInWaterOrRain() || entity.isInLava()) {
                entity.setRemainingFireTicks(100);
            }
        }
    }
}
