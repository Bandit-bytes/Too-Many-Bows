package net.bandit.many_bows.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;


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
        registerBowProperties(ItemRegistry.ANCIENT_SAGE_BOW.get());
        registerBowProperties(ItemRegistry.WIND_BOW.get());
        registerBowProperties(ItemRegistry.DEMONS_GRASP.get());
        registerBowProperties(ItemRegistry.AETHERS_CALL.get());
        registerBowProperties(ItemRegistry.CYROHEART_BOW.get());
        registerBowProperties(ItemRegistry.BURNT_RELIC.get());
        registerBowProperties(ItemRegistry.IRONCLAD_BOW.get());
        registerBowProperties(ItemRegistry.HUNTER_BOW.get());
        registerBowProperties(ItemRegistry.SENTINELS_WRAITH.get());
        registerBowProperties(ItemRegistry.EMERALD_SAGE_BOW.get());
        registerBowProperties(ItemRegistry.SHULKER_BLAST.get());
        registerBowProperties(ItemRegistry.VITALITY_WEAVER.get());
        registerBowProperties(ItemRegistry.ASTRAL_BOUND.get());
        registerBowProperties(ItemRegistry.SPECTRAL_WHISPER.get());
        registerBowProperties(ItemRegistry.AURORAS_GRACE.get());
        registerBowProperties(ItemRegistry.TWIN_SHADOWS.get());
        registerBowProperties(ItemRegistry.VERDANT_VIGOR.get());
        registerBowProperties(ItemRegistry.CRIMSON_NEXUS.get());
        registerBowProperties(ItemRegistry.RADIANCE.get());
        registerBowProperties(ItemRegistry.DUSK_REAPER.get());
        registerBowProperties(ItemRegistry.ETHEREAL_HUNTER.get());

        //crowsbows
        registerCrossbowProperties(ItemRegistry.ARCFORGE.get());

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
    private static void registerCrossbowProperties(Item item) {
        ItemPropertiesRegistry.register(item, new ResourceLocation("charged"), (stack, level, entity, seed) -> {
            return CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
        });

        ItemPropertiesRegistry.register(item, new ResourceLocation("firework"), (stack, level, entity, seed) -> {
            return CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
        });

        ItemPropertiesRegistry.register(item, new ResourceLocation("pull"), (stack, level, entity, seed) -> {
            if (entity == null || CrossbowItem.isCharged(stack)) return 0.0F;
            return (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / CrossbowItem.getChargeDuration(stack);
        });

        ItemPropertiesRegistry.register(item, new ResourceLocation("pulling"), (stack, level, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
        });
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(() -> EntityRegistry.FROSTBITE_ARROW.get(), FrostbiteArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SONIC_BOOM_PROJECTILE.get(), SonicBoomProjectileRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.DRAGONS_BREATH_ARROW.get(), DragonsBreathArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.ICICLE_JAVELIN.get(), IcicleJavelinRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.VENOM_ARROW.get(), VenomArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.FLAME_ARROW.get(), FlameArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.TIDAL_ARROW.get(), TidalArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.WIND_PROJECTILE.get(), WindArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.LIGHTNING_ARROW.get(), LightningArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.CURSED_FLAME_ARROW.get(), CursedFlameArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.HUNTER_ARROW.get(), HunterArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SENTINEL_ARROW.get(), SentinelArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), ShulkerBlastArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.HUNTER_XP_ARROW.get(), HunterXPArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.ANCIENT_SAGE_ARROW.get(), AncientSageArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.VITALITY_ARROW.get(), VitalityArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.ASTRAL_ARROW.get(), AstralArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SPECTRAL_ARROW.get(), SpectralArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.AURORA_ARROW.get(), AuroraArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.RADIANT_ARROW.get(), RadianceArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.DUSK_REAPER_ARROW.get(), DuskArrowRenderer::new);

    }
}
