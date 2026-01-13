package net.bandit.many_bows.mixin;

import net.bandit.many_bows.registry.EffectRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityWaterExtinguishMixin {

    @Unique
    private static boolean tmb$hasCursed(Entity e) {
        return (e instanceof LivingEntity le) && le.hasEffect(EffectRegistry.CURSED_FLAME.get());
    }

    @Inject(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At("TAIL"))
    private void tmb$afterWater(CallbackInfo ci) {
        Entity e = (Entity)(Object)this;
        if (!tmb$hasCursed(e)) return;

        LivingEntity le = (LivingEntity)e;
        if (le.getRemainingFireTicks() < 20) le.setRemainingFireTicks(20);
    }
}
