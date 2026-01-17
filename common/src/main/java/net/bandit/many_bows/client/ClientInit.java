package net.bandit.many_bows.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.renderer.*;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
                ItemRegistry.SENTINELS_WRATH.get(),
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
                ItemRegistry.TORCHBEARER.get(),
                ItemRegistry.WEBSTRING.get(),
                ItemRegistry.BEACON_BEAM_BOW.get()
        );

        bows.forEach(ClientInit::registerBowProperties);
        registerEntityRenderers();
    }

    private static void registerBowProperties(Item item) {
        ItemPropertiesRegistry.register(item,
                ResourceLocation.fromNamespaceAndPath("minecraft", "pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;

                    float basePullTicks = ManyBowsConfigHolder.CONFIG.globalBowPullSpeed;

                    if (item instanceof PullSpeedItem psi) {
                        basePullTicks = psi.getPullTicks(stack, entity);
                    }

                    float drawSpeed = 1.0F;
                    if (entity instanceof LivingEntity living) {
                        AttributeInstance inst = living.getAttribute(BOW_DRAW_SPEED_HOLDER);
                        if (inst != null) {
                            drawSpeed = (float) inst.getValue();
                        }
                    }

                    float pullTicks = basePullTicks / Math.max(0.05F, drawSpeed);

                    int used = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
                    return (float) used / pullTicks;
                });

        ItemPropertiesRegistry.register(item,
                ResourceLocation.fromNamespaceAndPath("minecraft", "pulling"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    private static final Holder<Attribute> BOW_DRAW_SPEED_HOLDER =
            BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(
                    ResourceKey.create(
                            Registries.ATTRIBUTE,
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_draw_speed")
                    )
            );


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
        EntityRendererRegistry.register(() -> EntityRegistry.LIGHT_ORB.get(), NoopRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.IRONCLAD_ARROW.get(), IroncladArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.WEBSTRING_ARROW.get(), WebstringArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.ETHEREAL_ARROW.get(), EtherealArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.TORCHBEARER_ARROW.get(), TorchbearerArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SOLAR_ARROW.get(), SolarArrowRenderer::new);
        EntityRendererRegistry.register(()  ->EntityRegistry.AETHERS_CALL_ARROW.get(),AethersCallArrowRenderer::new);
        EntityRendererRegistry.register(()  ->EntityRegistry.BEACON_BEAM_ARROW.get(), BeaconBeamArrowRenderer::new);

    }
}
