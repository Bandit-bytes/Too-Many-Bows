package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.effect.CursedFlameEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import static net.bandit.many_bows.ManyBowsMod.MOD_ID;

public class EffectRegistry {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> CURSED_FLAME = MOB_EFFECTS.register("cursed_flame", () -> new CursedFlameEffect(MobEffectCategory.HARMFUL, 0xdfff2b));

    public static void register() {
        MOB_EFFECTS.register();
    }
}
