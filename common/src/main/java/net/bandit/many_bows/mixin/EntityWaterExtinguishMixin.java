package net.bandit.many_bows.mixin;

import net.bandit.many_bows.registry.EffectRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents water and other generic extinguish calls from removing cursed flame. */
@Mixin(Entity.class)
public abstract class EntityWaterExtinguishMixin {

    @Unique
    private static final ResourceKey<MobEffect> TMB_CURSED_KEY =
            ResourceKey.create(Registries.MOB_EFFECT, EffectRegistry.CURSED_FLAME.getId());

    @Unique
    private static boolean tmb$hasCursedByKey(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        for (var instance : living.getActiveEffects()) {
            if (instance.getEffect().is(TMB_CURSED_KEY)) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "extinguishFire", at = @At("HEAD"), cancellable = true)
    private void tmb$keepCursedFlameLit(CallbackInfo ci) {
        if (tmb$hasCursedByKey((Entity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "clearFire", at = @At("HEAD"), cancellable = true)
    private void tmb$blockDirectFireClear(CallbackInfo ci) {
        if (tmb$hasCursedByKey((Entity) (Object) this)) {
            ci.cancel();
        }
    }
}
