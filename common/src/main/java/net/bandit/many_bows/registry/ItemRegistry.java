package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> ARCANE_BOW = ITEMS.register("arcane_bow",
            () -> new ArcaneBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static final RegistrySupplier<Item> SOLAR_BOW = ITEMS.register("solar_bow",
            () -> new SolarBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static final RegistrySupplier<Item> FROSTBITE = ITEMS.register("frostbite",
            () -> new FrostbiteBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static final RegistrySupplier<Item> ARC_HEAVENS = ITEMS.register("arc_heavens",
            () -> new HeavensBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static final RegistrySupplier<Item> DRAGONS_BREATH = ITEMS.register("dragons_breath",
            () -> new DragonsBreathBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static final RegistrySupplier<Item> VERDANT_VIPER = ITEMS.register("verdant_viper",
            () -> new VerdantViperBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
//
//    public static final RegistrySupplier<Item> LIGHT_BOW = ITEMS.register("light_bow",
//            () -> new BowItem(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
//
    public static final RegistrySupplier<Item> DARK_BOW = ITEMS.register("dark_bow",
            () -> new SonicBoomBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
//
//    public static final RegistrySupplier<Item> POISON_BOW = ITEMS.register("poison_bow",
//            () -> new BowItem(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
//
//    public static final RegistrySupplier<Item> SHADOW_BOW = ITEMS.register("shadow_bow",
//            () -> new BowItem(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static void register() {
        ITEMS.register();
    }
}
