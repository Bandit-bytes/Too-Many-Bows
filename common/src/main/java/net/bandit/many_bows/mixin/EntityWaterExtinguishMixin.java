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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityWaterExtinguishMixin {

    @Unique
    private static final ResourceKey<MobEffect> TMB_CURSED_KEY =
            ResourceKey.create(Registries.MOB_EFFECT, EffectRegistry.CURSED_FLAME.getId());

    @Unique
    private static boolean tmb$hasCursedByKey(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        for (var inst : le.getActiveEffects()) {
            if (inst.getEffect().is(TMB_CURSED_KEY)) return true;
        }
        return false;
    }

    @Inject(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At("TAIL"))
    private void tmb$afterWaterCurrentPushing(CallbackInfo ci) {
        Entity e = (Entity)(Object)this;
        if (tmb$hasCursedByKey(e)) {
            LivingEntity le = (LivingEntity)e;
            if (le.getRemainingFireTicks() < 20) le.setRemainingFireTicks(20);
        }
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("TAIL"))
    private void tmb$afterFluidPushing(CallbackInfoReturnable<Boolean> cir) {
        Entity e = (Entity)(Object)this;
        if (tmb$hasCursedByKey(e)) {
            LivingEntity le = (LivingEntity)e;
            if (le.getRemainingFireTicks() < 20) le.setRemainingFireTicks(20);
        }
    }
}

