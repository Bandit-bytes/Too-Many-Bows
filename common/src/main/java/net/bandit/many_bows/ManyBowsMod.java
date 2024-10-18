package net.bandit.many_bows;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.bandit.many_bows.client.ClientInit;
import net.bandit.many_bows.client.renderer.FrostbiteArrowRenderer;
import net.bandit.many_bows.client.renderer.SonicBoomProjectileRenderer;
import net.bandit.many_bows.client.renderer.VenomArrowRenderer;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.registry.TabRegistry;

public final class ManyBowsMod {
    public static final String MOD_ID = "too_many_bows";

    public static void init() {
        ItemRegistry.register();
        TabRegistry.init();
        EntityRegistry.register();
    }

    public static void initClient() {
        ClientInit.registerClientProperties();

        EntityRendererRegistry.register(() -> EntityRegistry.FROSTBITE_ARROW.get(), FrostbiteArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.VENOM_ARROW.get(), VenomArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SONIC_BOOM_PROJECTILE.get(), SonicBoomProjectileRenderer::new);
    }
}
