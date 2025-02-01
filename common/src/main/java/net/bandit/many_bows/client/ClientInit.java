package net.bandit.many_bows.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

public class ClientInit {

    public static void registerClientProperties() {
        // Bows
        List<Item> bows = List.of(
                ItemRegistry.ARCANE_BOW.get(),
                ItemRegistry.SOLAR_BOW.get(),
                ItemRegistry.FROSTBITE.get(),
                ItemRegistry.ARC_HEAVENS.get(),
                ItemRegistry.DARK_BOW.get(),
                ItemRegistry.DRAGONS_BREATH.get(),
                ItemRegistry.VERDANT_VIPER.get(),
                ItemRegistry.FLAME_BOW.get(),
                ItemRegistry.TIDAL_BOW.get(),
                ItemRegistry.NECRO_FLAME_BOW.get(),
                ItemRegistry.SCATTER_BOW.get(),
                ItemRegistry.ANCIENT_SAGE_BOW.get(),
                ItemRegistry.WIND_BOW.get(),
                ItemRegistry.DEMONS_GRASP.get(),
                ItemRegistry.AETHERS_CALL.get(),
                ItemRegistry.CYROHEART_BOW.get(),
                ItemRegistry.BURNT_RELIC.get(),
                ItemRegistry.IRONCLAD_BOW.get(),
                ItemRegistry.HUNTER_BOW.get(),
                ItemRegistry.SENTINELS_WRAITH.get(),
                ItemRegistry.EMERALD_SAGE_BOW.get(),
                ItemRegistry.SHULKER_BLAST.get(),
                ItemRegistry.VITALITY_WEAVER.get(),
                ItemRegistry.ASTRAL_BOUND.get(),
                ItemRegistry.SPECTRAL_WHISPER.get(),
                ItemRegistry.AURORAS_GRACE.get(),
                ItemRegistry.TWIN_SHADOWS.get(),
                ItemRegistry.VERDANT_VIGOR.get(),
                ItemRegistry.CRIMSON_NEXUS.get(),
                ItemRegistry.RADIANCE.get(),
                ItemRegistry.DUSK_REAPER.get(),
                ItemRegistry.ETHEREAL_HUNTER.get(),
                ItemRegistry.WEBSTRING.get()
        );

        // Register properties for each bow
        bows.forEach(ClientInit::registerBowProperties);

        // Entity renderers
        registerEntityRenderers();
    }

    private static void registerBowProperties(Item item) {
        ItemPropertiesRegistry.register(item, ResourceLocation.parse(ResourceLocation.fromNamespaceAndPath("minecraft","pull").toString()), (itemStack, level, entity, seed) -> {
            if (entity == null) return 0.0F;
            return entity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
        });

        ItemPropertiesRegistry.register(item, ResourceLocation.parse(ResourceLocation.fromNamespaceAndPath("minecraft","pulling").toString()), (itemStack, level, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
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
        EntityRendererRegistry.register(() -> EntityRegistry.RIFT_ENTITY.get(), NoopRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.IRONCLAD_ARROW.get(), IroncladArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.WEBSTRING_ARROW.get(), WebstringArrowRenderer::new);
    }
}
