package net.bandit.many_bows.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TabRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> TOO_MANY_BOWS_TAB = TABS.register(
            "too_many_bows_tab",
            () -> CreativeTabRegistry.create(
                    Component.translatable("category.many_bows"),
                    () -> new ItemStack(ItemRegistry.ARCANE_BOW.get())
            )
    );
    public static void init() {
        TABS.register();
    }
}
