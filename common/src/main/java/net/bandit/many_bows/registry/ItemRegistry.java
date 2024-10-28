package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.ITEM);

    //BOWS
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
            () -> new VerdantViperBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> FLAME_BOW = ITEMS.register("flame_bow",
            () -> new FlameBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> DARK_BOW = ITEMS.register("dark_bow",
            () -> new SonicBoomBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> TIDAL_BOW = ITEMS.register("tidal_bow",
            () -> new TidalBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> NECRO_FLAME_BOW = ITEMS.register("necro_flame_bow",
            () -> new CursedFlameBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> SCATTER_BOW = ITEMS.register("scatter_bow",
            () -> new ScatterBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));

//items
    public static final RegistrySupplier<Item> SKULL = ITEMS.register("skull",
            () -> new Item(new Properties().rarity(Rarity.EPIC).stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));

    public static void register() {
        ITEMS.register();
    }
}
