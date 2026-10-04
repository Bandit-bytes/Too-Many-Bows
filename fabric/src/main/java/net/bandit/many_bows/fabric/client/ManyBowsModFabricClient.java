package net.bandit.many_bows.fabric.client;

import net.bandit.many_bows.ManyBowsMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.bandit.many_bows.client.particle.EventideStarParticle;
import net.bandit.many_bows.client.particle.RelicCombatParticle;
import net.bandit.many_bows.registry.RelicParticleRegistry;

public final class ManyBowsModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ManyBowsMod.initClient();
        ParticleFactoryRegistry.getInstance().register(
                RelicParticleRegistry.FALLING_STAR.get(), EventideStarParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.RITUAL_STAR.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.RITUAL));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.ANGEL_TRAIL.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.ANGEL));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.STAR_BEACON.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.BEACON));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.GOLD_SPARK.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.SPARK));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.PRECISION_HALO.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.HALO));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.SEVERANCE_FLASH.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.FRACTURE));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.WYRM_MAW.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.MAW));
        ParticleFactoryRegistry.getInstance().register(RelicParticleRegistry.WYRM_SHOCKWAVE.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.SHOCKWAVE));
        ManyBowsFabricPredicates.init();
    }
}
