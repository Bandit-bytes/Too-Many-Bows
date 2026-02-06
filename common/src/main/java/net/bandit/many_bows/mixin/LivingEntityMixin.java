package net.bandit.many_bows.mixin;

import net.bandit.many_bows.registry.EffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
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
    private static final ResourceKey<MobEffect> TMB_CURSED_KEY =
            ResourceKey.create(Registries.MOB_EFFECT, EffectRegistry.CURSED_FLAME.getId());

    @Unique
    private static boolean tmb$hasCursedByKey(LivingEntity self) {
        for (var inst : self.getActiveEffects()) {
            if (inst.getEffect().is(TMB_CURSED_KEY)) return true;
        }
        return false;
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void tmb$blockHeal(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (tmb$hasCursedByKey(self)) ci.cancel();
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void tmb$noSetHealthUp(float newHealth, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (tmb$hasCursedByKey(self) && newHealth > self.getHealth()) ci.cancel();
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void tmb$afterBaseTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!tmb$hasCursedByKey(self)) return;


        if (self.getRemainingFireTicks() < 20) self.setRemainingFireTicks(20);

        self.removeEffect(MobEffects.REGENERATION);
        self.removeEffect(MobEffects.INSTANT_HEALTH);

        if (!self.fireImmune() && (self.tickCount % 20 == 0)) {
            self.hurt(self.damageSources().onFire(), 1.0F);
        }
    }
}

