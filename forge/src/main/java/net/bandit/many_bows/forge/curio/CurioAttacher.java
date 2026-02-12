package net.bandit.many_bows.forge.curio;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CurioAttacher {

    private static final ResourceLocation ID = new ResourceLocation(ManyBowsMod.MOD_ID, "curio");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();

        if (stack.is(ItemRegistry.WIND_GLOVE.get())) {
            event.addCapability(ID, new CurioCapabilityProvider(new DrawSpeedGloveCurio(), stack));
            return;
        }

        if (stack.is(ItemRegistry.SHARPSHOT_RING.get())) {
            event.addCapability(ID, new CurioCapabilityProvider(new SharpshotRingCurio(), stack));
            return;
        }

        if (stack.is(ItemRegistry.STORMBOUND_SIGNET.get())) {
            event.addCapability(ID, new CurioCapabilityProvider(new StormboundSignetCurio(), stack));
            return;
        }

        if (stack.is(ItemRegistry.FLETCHERS_TALISMAN.get())) {
            event.addCapability(ID, new CurioCapabilityProvider(new FletchersTalismanCurio(), stack));
            return;
        }

        if (stack.is(ItemRegistry.DEAD_EYES_PENDANT.get())) {
            event.addCapability(ID, new CurioCapabilityProvider(new DeadEyesPendantCurio(), stack));
        }
    }
}
