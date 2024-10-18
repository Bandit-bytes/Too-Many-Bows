package net.bandit.many_bows.forge;

import dev.architectury.platform.forge.EventBuses;
import net.bandit.many_bows.client.renderer.FrostbiteArrowRenderer;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.renderer.VenomArrowRenderer;
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
    }
}
