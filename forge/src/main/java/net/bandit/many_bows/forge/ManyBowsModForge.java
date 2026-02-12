package net.bandit.many_bows.forge;

import dev.architectury.platform.forge.EventBuses;
import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(ManyBowsMod.MOD_ID)
@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ManyBowsModForge {

    public ManyBowsModForge() {
        ManyBowsConfigHolder.CONFIG = BowLootConfig.loadConfig();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(ManyBowsMod.MOD_ID, modEventBus);
        modEventBus.addListener(this::onClientSetup);
        ManyBowsMod.init();

    }


    private void onClientSetup(final FMLClientSetupEvent event) {
        // This is where the client-specific code should be registered
        ManyBowsMod.initClient();
    }

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        // Register any models or textures here
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
        event.registerEntityRenderer(EntityRegistry.RADIANT_ARROW.get(), RadianceArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DUSK_REAPER_ARROW.get(), DuskArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEBSTRING_ARROW.get(), WebstringArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.IRONCLAD_ARROW.get(), IroncladArrowRenderer::new);
    }
}
