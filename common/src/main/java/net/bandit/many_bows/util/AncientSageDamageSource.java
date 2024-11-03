package net.bandit.many_bows.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class AncientSageDamageSource {

    public static DamageSource create(Level level, Entity arrow, Entity owner) {
        Holder<DamageType> damageType = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ARROW);
        DamageSource source = new DamageSource(damageType, owner);

        return source;
    }
}
