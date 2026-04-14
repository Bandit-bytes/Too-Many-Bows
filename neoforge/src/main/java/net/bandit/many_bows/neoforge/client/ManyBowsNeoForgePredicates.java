package net.bandit.many_bows.neoforge.client;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.renderer.LanternRenderState;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ManyBowsNeoForgePredicates {

    private static final ResourceLocation WORN_PREDICATE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "worn");

    @SubscribeEvent
    public static void registerItemProperties(RegisterClientExtensionsEvent event) {
        ItemProperties.register(
                ItemRegistry.SOUL_LANTERN.get(),
                WORN_PREDICATE,
                (stack, level, entity, seed) -> LanternRenderState.isEquippedRender() ? 1.0F : 0.0F
        );

        ItemProperties.register(
                ItemRegistry.CURSED_LANTERN.get(),
                WORN_PREDICATE,
                (stack, level, entity, seed) -> LanternRenderState.isEquippedRender() ? 1.0F : 0.0F
        );
    }
}