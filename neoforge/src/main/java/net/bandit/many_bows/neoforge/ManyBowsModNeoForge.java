package net.bandit.many_bows.neoforge;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.neoforge.curio.CursedLanternCurio;
import net.bandit.many_bows.neoforge.curio.DeadEyesPendantCurio;
import net.bandit.many_bows.neoforge.curio.DrawSpeedGloveCurio;
import net.bandit.many_bows.neoforge.curio.FletchersTalismanCurio;
import net.bandit.many_bows.neoforge.curio.SharpshotRingCurio;
import net.bandit.many_bows.neoforge.curio.SoulLanternCurio;
import net.bandit.many_bows.neoforge.curio.StormboundSignetCurio;
import net.bandit.many_bows.registry.ItemRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(ManyBowsMod.MOD_ID)
public final class ManyBowsModNeoForge {

    public ManyBowsModNeoForge(IEventBus modEventBus) {
        ManyBowsMod.init();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModAttributesNeoForge::onEntityAttributeModification);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosApi.registerCurio(ItemRegistry.WIND_GLOVE.get(), new DrawSpeedGloveCurio());
            CuriosApi.registerCurio(ItemRegistry.SHARPSHOT_RING.get(), new SharpshotRingCurio());
            CuriosApi.registerCurio(ItemRegistry.STORMBOUND_SIGNET.get(), new StormboundSignetCurio());
            CuriosApi.registerCurio(ItemRegistry.FLETCHERS_TALISMAN.get(), new FletchersTalismanCurio());
            CuriosApi.registerCurio(ItemRegistry.DEAD_EYES_PENDANT.get(), new DeadEyesPendantCurio());
            CuriosApi.registerCurio(ItemRegistry.SOUL_LANTERN.get(), new SoulLanternCurio());
            CuriosApi.registerCurio(ItemRegistry.CURSED_LANTERN.get(), new CursedLanternCurio());
        });
    }
}