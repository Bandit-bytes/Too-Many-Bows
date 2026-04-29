package net.bandit.many_bows.neoforge.client;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.renderer.AethersCallArrowRenderer;
import net.bandit.many_bows.client.renderer.AncientSageArrowRenderer;
import net.bandit.many_bows.client.renderer.AstralArrowRenderer;
import net.bandit.many_bows.client.renderer.AuroraArrowRenderer;
import net.bandit.many_bows.client.renderer.BeaconBeamArrowRenderer;
import net.bandit.many_bows.client.renderer.CursedFlameArrowRenderer;
import net.bandit.many_bows.client.renderer.DragonsBreathArrowRenderer;
import net.bandit.many_bows.client.renderer.DuskArrowRenderer;
import net.bandit.many_bows.client.renderer.EtherealArrowRenderer;
import net.bandit.many_bows.client.renderer.FlameArrowRenderer;
import net.bandit.many_bows.client.renderer.FrostbiteArrowRenderer;
import net.bandit.many_bows.client.renderer.GravewireArrowRenderer;
import net.bandit.many_bows.client.renderer.GravewireMarkRenderer;
import net.bandit.many_bows.client.renderer.HoardedSkullRenderer;
import net.bandit.many_bows.client.renderer.HunterArrowRenderer;
import net.bandit.many_bows.client.renderer.HunterXPArrowRenderer;
import net.bandit.many_bows.client.renderer.IcicleJavelinRenderer;
import net.bandit.many_bows.client.renderer.IroncladArrowRenderer;
import net.bandit.many_bows.client.renderer.LightningArrowRenderer;
import net.bandit.many_bows.client.renderer.RadianceArrowRenderer;
import net.bandit.many_bows.client.renderer.SentinelArrowRenderer;
import net.bandit.many_bows.client.renderer.ShulkerBlastArrowRenderer;
import net.bandit.many_bows.client.renderer.SolarArrowRenderer;
import net.bandit.many_bows.client.renderer.SonicBoomProjectileRenderer;
import net.bandit.many_bows.client.renderer.SoulhoardArrowRenderer;
import net.bandit.many_bows.client.renderer.SpectralArrowRenderer;
import net.bandit.many_bows.client.renderer.TidalArrowRenderer;
import net.bandit.many_bows.client.renderer.TorchbearerArrowRenderer;
import net.bandit.many_bows.client.renderer.VaultPortalRenderer;
import net.bandit.many_bows.client.renderer.VaultpiercerArrowRenderer;
import net.bandit.many_bows.client.renderer.VenomArrowRenderer;
import net.bandit.many_bows.client.renderer.VitalityArrowRenderer;
import net.bandit.many_bows.client.renderer.WebstringArrowRenderer;
import net.bandit.many_bows.client.renderer.WindArrowRenderer;
import net.bandit.many_bows.neoforge.client.curio.LanternCurioRenderer;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, value = Dist.CLIENT)
public final class ManyBowsNeoForgeClient {

    private ManyBowsNeoForgeClient() {
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ManyBowsMod.initClient();

        event.enqueueWork(() -> {
            ICurioRenderer.register(ItemRegistry.SOUL_LANTERN.get(), LanternCurioRenderer::new);
            ICurioRenderer.register(ItemRegistry.CURSED_LANTERN.get(), LanternCurioRenderer::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.FROSTBITE_ARROW.get(), FrostbiteArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VENOM_ARROW.get(), VenomArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FLAME_ARROW.get(), FlameArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CURSED_FLAME_ARROW.get(), CursedFlameArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TIDAL_ARROW.get(), TidalArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WIND_PROJECTILE.get(), WindArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LIGHTNING_ARROW.get(), LightningArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DRAGONS_BREATH_ARROW.get(), DragonsBreathArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), SonicBoomProjectileRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICICLE_JAVELIN.get(), IcicleJavelinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HUNTER_ARROW.get(), HunterArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SENTINEL_ARROW.get(), SentinelArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HUNTER_XP_ARROW.get(), HunterXPArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ANCIENT_SAGE_ARROW.get(), AncientSageArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), ShulkerBlastArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VITALITY_ARROW.get(), VitalityArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ASTRAL_ARROW.get(), AstralArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SPECTRAL_ARROW.get(), SpectralArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AURORA_ARROW.get(), AuroraArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RIFT_ENTITY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LIGHT_ORB.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VAULT_PORTAL.get(), VaultPortalRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RADIANT_ARROW.get(), RadianceArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DUSK_REAPER_ARROW.get(), DuskArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEBSTRING_ARROW.get(), WebstringArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.IRONCLAD_ARROW.get(), IroncladArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TORCHBEARER_ARROW.get(), TorchbearerArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ETHEREAL_ARROW.get(), EtherealArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SOLAR_ARROW.get(), SolarArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AETHERS_CALL_ARROW.get(), AethersCallArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BEACON_BEAM_ARROW.get(), BeaconBeamArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GRAVEWIRE_ARROW.get(), GravewireArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GRAVEWIRE_MARK.get(), GravewireMarkRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VAULTPIERCER_ARROW.get(), VaultpiercerArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SOULHOARD_ARROW.get(), SoulhoardArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HOARDED_SKULL.get(), HoardedSkullRenderer::new);
    }
}