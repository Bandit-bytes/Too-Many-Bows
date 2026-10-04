package net.bandit.many_bows.fabric.client;

import net.bandit.many_bows.client.ManyBowsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.bandit.many_bows.client.particle.EventideStarParticle;
import net.bandit.many_bows.client.particle.RelicCombatParticle;

public final class ManyBowsModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ManyBowsClient.init();
        ParticleProviderRegistry.getInstance().register(
                RelicParticleRegistry.FALLING_STAR.get(), EventideStarParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.RITUAL_STAR.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.RITUAL));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.ANGEL_TRAIL.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.ANGEL));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.STAR_BEACON.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.BEACON));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.GOLD_SPARK.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.SPARK));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.PRECISION_HALO.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.HALO));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.SEVERANCE_FLASH.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.FRACTURE));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.WYRM_MAW.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.MAW));
        ParticleProviderRegistry.getInstance().register(RelicParticleRegistry.WYRM_SHOCKWAVE.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.SHOCKWAVE));

        ManyBowsFabricPredicates.init();
        ManyBowsFabricRenderers.init();
    }
}
