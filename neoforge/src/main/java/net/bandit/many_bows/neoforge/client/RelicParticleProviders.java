package net.bandit.many_bows.neoforge.client;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.particle.EventideStarParticle;
import net.bandit.many_bows.client.particle.RelicCombatParticle;
import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = ManyBowsMod.MOD_ID,
        value = Dist.CLIENT)
public final class RelicParticleProviders {
    private RelicParticleProviders() {}

    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(RelicParticleRegistry.FALLING_STAR.get(),
                EventideStarParticle.Provider::new);
        event.registerSpriteSet(RelicParticleRegistry.RITUAL_STAR.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.RITUAL));
        event.registerSpriteSet(RelicParticleRegistry.ANGEL_TRAIL.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.ANGEL));
        event.registerSpriteSet(RelicParticleRegistry.STAR_BEACON.get(),
                sprites -> new EventideStarParticle.Provider(sprites, EventideStarParticle.Mode.BEACON));
        event.registerSpriteSet(RelicParticleRegistry.GOLD_SPARK.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.SPARK));
        event.registerSpriteSet(RelicParticleRegistry.PRECISION_HALO.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.HALO));
        event.registerSpriteSet(RelicParticleRegistry.SEVERANCE_FLASH.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.FRACTURE));
        event.registerSpriteSet(RelicParticleRegistry.WYRM_MAW.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.MAW));
        event.registerSpriteSet(RelicParticleRegistry.WYRM_SHOCKWAVE.get(),
                sprites -> new RelicCombatParticle.Provider(sprites, RelicCombatParticle.Mode.SHOCKWAVE));
    }
}
