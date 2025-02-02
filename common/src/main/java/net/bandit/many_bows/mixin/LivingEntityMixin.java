package net.bandit.many_bows.mixin;

import net.bandit.many_bows.registry.EffectRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void preventHealing(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (EffectRegistry.CURSED_FLAME.isPresent() && entity.hasEffect(EffectRegistry.CURSED_FLAME)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL")) // Ensure this runs AFTER all vanilla logic
    private void applyCursedFlameEffect(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (EffectRegistry.CURSED_FLAME.isPresent()) {
            if (entity.hasEffect(EffectRegistry.CURSED_FLAME)) {
                // Ensure the entity remains on fire if in water, rain, or lava
                if (entity.isInWaterOrRain() || entity.isInLava()) {
                    entity.setRemainingFireTicks(100);
                }
                MobEffectInstance effectInstance = entity.getEffect(EffectRegistry.CURSED_FLAME);
                if (effectInstance != null) {
                    entity.addEffect(new MobEffectInstance(EffectRegistry.CURSED_FLAME, effectInstance.getDuration(), effectInstance.getAmplifier(), false, false, true));
                }
            }
        }
    }
}
