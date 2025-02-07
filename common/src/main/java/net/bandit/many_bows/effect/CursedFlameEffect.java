package net.bandit.many_bows.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CursedFlameEffect extends MobEffect {

    public CursedFlameEffect() {
        super(MobEffectCategory.HARMFUL, 0x660066);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int i) {
        return super.applyEffectTick(livingEntity, i);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
