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
import net.minecraft.world.item.enchantment.Enchantments;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.ITEM);


    //BOWS
    public static final RegistrySupplier<Item> ANCIENT_SAGE_BOW = ITEMS.register("ancient_sage_bow",
            () -> new AncientSageBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> ARCANE_BOW = ITEMS.register("arcane_bow",
            () -> new ArcaneBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> DARK_BOW = ITEMS.register("dark_bow",
            () -> new SonicBoomBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> FROSTBITE = ITEMS.register("frostbite",
            () -> new FrostbiteBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> ARC_HEAVENS = ITEMS.register("arc_heavens",
            () -> new HeavensBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> DRAGONS_BREATH = ITEMS.register("dragons_breath",
            () -> new DragonsBreathBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> VERDANT_VIPER = ITEMS.register("verdant_viper",
            () -> new VerdantViperBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));

//    //Flash Art Bows
    public static final RegistrySupplier<Item> SOLAR_BOW = ITEMS.register("solar_bow",
            () -> new SolarBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> SHULKER_BLAST = ITEMS.register("shulker_blast",
            () -> new ShulkerBlastBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> EMERALD_SAGE_BOW = ITEMS.register("emerald_sage_bow",
            () -> new EmeraldSageBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> DEMONS_GRASP = ITEMS.register("demons_grasp",
            () -> new BowItem(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> AETHERS_CALL = ITEMS.register("aethers_call",
            () -> new BowItem(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> IRONCLAD_BOW = ITEMS.register("ironclad_bow",
            () -> new IroncladBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> HUNTER_BOW = ITEMS.register("hunter_bow",
            () -> new HunterBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> BURNT_RELIC = ITEMS.register("burnt_relic",
            () -> new BurntRelicBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> CYROHEART_BOW = ITEMS.register("cyroheart_bow",
            () -> new IcicleJavelinBow(new Properties().durability(850).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> SENTINELS_WRAITH = ITEMS.register("sentinels_wrath",
            () -> new SentinelsWrathBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> VITALITY_WEAVER = ITEMS.register("vitality_weaver",
            () -> new VitalityWeaverBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> ASTRAL_BOUND = ITEMS.register("astral_bound",
            () -> new AstralBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> SPECTRAL_WHISPER = ITEMS.register("spectral_whisper",
            () -> new SpectralWhisperBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> AURORAS_GRACE = ITEMS.register("auroras_grace",
            () -> new AuroraBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));

    //Crossbows
//    public static final RegistrySupplier<Item> ARCFORGE = ITEMS.register("arcforge",
//            () -> new CrossbowItem(new Properties().durability(700).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));

//    //Bows that need to be updated - they are done now
    public static final RegistrySupplier<Item> TWIN_SHADOWS = ITEMS.register("twin_shadows",
            () -> new TwinShadowsBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> DUSK_REAPER = ITEMS.register("dusk_reaper",
            () -> new DuskReaperBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> VERDANT_VIGOR = ITEMS.register("verdant_vigor",
            () -> new VerdantVigorBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> ETHEREAL_HUNTER = ITEMS.register("ethereal_hunter",
            () -> new EtherealHunterBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> CRIMSON_NEXUS = ITEMS.register("crimson_nexus",
            () -> new CrimsonNexusBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> RADIANCE = ITEMS.register("radiance",
            () -> new RadianceBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> WEBSTRING = ITEMS.register("webstring",
            () -> new WebstringVolleyBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> TORCHBEARER = ITEMS.register("torchbearer",
            () -> new TorchbearerBow(new Properties().durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));

//    //bandit bows
    public static final RegistrySupplier<Item> FLAME_BOW = ITEMS.register("flame_bow",
            () -> new FlameBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> TIDAL_BOW = ITEMS.register("tidal_bow",
            () -> new TidalBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> NECRO_FLAME_BOW = ITEMS.register("necro_flame_bow",
            () -> new CursedFlameBow(new Properties().durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> SCATTER_BOW = ITEMS.register("scatter_bow",
            () -> new ScatterBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> WIND_BOW = ITEMS.register("wind_bow",
            () -> new WindBow(new Properties().durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON)));
    //item
    public static final RegistrySupplier<Item> CURSED_STONE = ITEMS.register("cursed_stone",
            () -> new CursedStone(new Properties().rarity(Rarity.EPIC).stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> SOUL_FRAGMENT = ITEMS.register("soul_fragment",
            () -> new SoulFragment(new Properties().stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> RIFT_SHARD = ITEMS.register("rift_shard",
            () -> new RiftShardItem(new Properties().rarity(Rarity.EPIC).stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> POWER_CRYSTAL = ITEMS.register("power_crystal",
            () -> new RepairCrystalItem(new Properties().rarity(Rarity.EPIC).stacksTo(16).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));


    public static void register() {
        ITEMS.register();
    }
}
