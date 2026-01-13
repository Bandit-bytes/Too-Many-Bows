package net.bandit.many_bows.mixin;

import net.bandit.many_bows.registry.EffectRegistry;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static boolean tmb$hasCursed(LivingEntity e) {
        return e.hasEffect(EffectRegistry.CURSED_FLAME.get());
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void tmb$preventHealing(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (tmb$hasCursed(self)) ci.cancel();
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void tmb$noSetHealthUp(float newHealth, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (tmb$hasCursed(self) && newHealth > self.getHealth()) ci.cancel();
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void tmb$afterBaseTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!tmb$hasCursed(self)) return;

        // keep at least 1 second of fire at all times
        if (self.getRemainingFireTicks() < 20) self.setRemainingFireTicks(20);

        // remove common healing effects
        self.removeEffect(MobEffects.REGENERATION);
        self.removeEffect(MobEffects.HEAL);

        // apply our own burn damage cadence (even if wet)
        if (!self.fireImmune() && (self.tickCount % 20 == 0)) {
            self.hurt(self.damageSources().onFire(), 1.0F);
        }
    }
}
