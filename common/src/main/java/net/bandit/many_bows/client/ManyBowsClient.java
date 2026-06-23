package net.bandit.many_bows.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

/** Client-only registration kept out of the shared server initializer. */
public final class ManyBowsClient {
    private ManyBowsClient() {
    }

    public static void init() {
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
        EntityRendererRegistry.register(() -> EntityRegistry.IRONCLAD_ARROW.get(), IroncladArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.WEBSTRING_ARROW.get(), WebstringArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.TORCHBEARER_ARROW.get(), TorchbearerArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.ETHEREAL_ARROW.get(), EtherealArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.LIGHT_ORB.get(), NoopRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.RIFT_ENTITY.get(), NoopRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SOLAR_ARROW.get(), SolarArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.AETHERS_CALL_ARROW.get(), AethersCallArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.BEACON_BEAM_ARROW.get(), BeaconBeamArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.GRAVEWIRE_ARROW.get(), GravewireArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.GRAVEWIRE_MARK.get(), GravewireMarkRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.VAULTPIERCER_ARROW.get(), VaultpiercerArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.VAULT_PORTAL.get(), VaultPortalRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.SOULHOARD_ARROW.get(), SoulhoardArrowRenderer::new);
        EntityRendererRegistry.register(() -> EntityRegistry.HOARDED_SKULL.get(), HoardedSkullRenderer::new);
    }
}
