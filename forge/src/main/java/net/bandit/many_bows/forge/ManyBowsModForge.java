package net.bandit.many_bows.forge;

import dev.architectury.platform.forge.EventBuses;
import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.forge.loot.BowLootInjectorPlatformImpl;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ManyBowsMod.MOD_ID)
@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ManyBowsModForge {

    public ManyBowsModForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(ManyBowsMod.MOD_ID, modEventBus);
        modEventBus.addListener(this::onClientSetup);
        ManyBowsMod.init();

        // Register loot table handling on the Forge event bus
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(BowLootInjectorPlatformImpl.class);
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
    }
}
