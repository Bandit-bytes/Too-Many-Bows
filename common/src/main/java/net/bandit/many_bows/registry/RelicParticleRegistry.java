package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public final class RelicParticleRegistry {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> FALLING_STAR =
            PARTICLES.register("eventide_star", StarType::new);

    public static final RegistrySupplier<SimpleParticleType> RITUAL_STAR =
            PARTICLES.register("eventide_ritual", StarType::new);
    public static final RegistrySupplier<SimpleParticleType> ANGEL_TRAIL =
            PARTICLES.register("eventide_angel", StarType::new);
    public static final RegistrySupplier<SimpleParticleType> STAR_BEACON =
            PARTICLES.register("eventide_beacon", StarType::new);

    public static final RegistrySupplier<SimpleParticleType> GOLD_SPARK =
            PARTICLES.register("godsplitter_spark", StarType::new);
    public static final RegistrySupplier<SimpleParticleType> PRECISION_HALO =
            PARTICLES.register("godsplitter_halo", StarType::new);
    public static final RegistrySupplier<SimpleParticleType> SEVERANCE_FLASH =
            PARTICLES.register("godsplitter_fracture", StarType::new);
    public static final RegistrySupplier<SimpleParticleType> WYRM_MAW =
            PARTICLES.register("worldeater_maw", StarType::new);
    public static final RegistrySupplier<SimpleParticleType> WYRM_SHOCKWAVE =
            PARTICLES.register("worldeater_shockwave", StarType::new);

    private RelicParticleRegistry() {}

    private static final class StarType extends SimpleParticleType {
        private StarType() {
            super(false);
        }
    }

    public static void register() {
        PARTICLES.register();
    }
}
