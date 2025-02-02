package net.bandit.many_bows.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CursedFlameEffect extends MobEffect {

    public CursedFlameEffect() {
        super(MobEffectCategory.HARMFUL, 0x660066);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.hurt(entity.damageSources().magic(), 1.0F + amplifier);
        }
        return false;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

}
