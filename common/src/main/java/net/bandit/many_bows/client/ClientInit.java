package net.bandit.many_bows.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


public class ClientInit {

    public static void registerClientProperties() {
        registerBowProperties(ItemRegistry.ARCANE_BOW.get());
        registerBowProperties(ItemRegistry.SOLAR_BOW.get());
        registerBowProperties(ItemRegistry.FROSTBITE.get());
        registerBowProperties(ItemRegistry.ARC_HEAVENS.get());
        registerBowProperties(ItemRegistry.DARK_BOW.get());
        registerBowProperties(ItemRegistry.DRAGONS_BREATH.get());
        registerBowProperties(ItemRegistry.VERDANT_VIPER.get());
        registerBowProperties(ItemRegistry.FLAME_BOW.get());
        registerBowProperties(ItemRegistry.TIDAL_BOW.get());
        registerBowProperties(ItemRegistry.NECRO_FLAME_BOW.get());
        registerBowProperties(ItemRegistry.SCATTER_BOW.get());

        registerEntityRenderers();
    }

    private static void registerBowProperties(Item item) {
        ItemPropertiesRegistry.register(item, new ResourceLocation("pull"), (itemStack, level, entity, seed) -> {
            if (entity == null) return 0.0F;
            return entity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
        });

        ItemPropertiesRegistry.register(item, new ResourceLocation("pulling"), (itemStack, level, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
        });
    }
    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(() -> EntityRegistry.FROSTBITE_ARROW.get(), FrostbiteArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SONIC_BOOM_PROJECTILE.get(), SonicBoomProjectileRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.DRAGONS_BREATH_ARROW.get(), DragonsBreathArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.VENOM_ARROW.get(), VenomArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.FLAME_ARROW.get(), FlameArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.TIDAL_ARROW.get(), TidalArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.CURSED_FLAME_ARROW.get(), CursedFlameArrowRenderer::new);

    }
}
