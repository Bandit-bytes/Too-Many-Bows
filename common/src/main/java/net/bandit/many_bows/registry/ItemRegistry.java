package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.ITEM);

    private static Item.Properties props(String path) {
        return new Item.Properties().setId(
                ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, path))
        );
    }

    public static final RegistrySupplier<Item> POWER_CRYSTAL = ITEMS.register("power_crystal",
            () -> new RepairCrystalItem(props("power_crystal").stacksTo(16).rarity(Rarity.EPIC).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
    public static final RegistrySupplier<Item> RIFT_SHARD = ITEMS.register("rift_shard",
            () -> new RiftShardItem(props("rift_shard").rarity(Rarity.EPIC).stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));

    public static final RegistrySupplier<Item> WIND_GLOVE = ITEMS.register("wind_glove",
            () -> new DrawSpeedGloveItem(props("wind_glove").stacksTo(1).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> SHARPSHOT_RING = ITEMS.register("sharpshot_ring",
            () -> new BowDamageRingItem( BowDamageRingItem.Tier.SHARPSHOT,props("sharpshot_ring")
                    .stacksTo(1).rarity(Rarity.UNCOMMON).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
    public static final RegistrySupplier<Item> STORMBOUND_SIGNET = ITEMS.register("stormbound_signet",
            () -> new BowDamageRingItem( BowDamageRingItem.Tier.STORMBOUND,props("stormbound_signet")
                    .stacksTo(1).rarity(Rarity.RARE).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
    public static final RegistrySupplier<Item> FLETCHERS_TALISMAN = ITEMS.register("fletchers_talisman",
            () -> new BowNecklaceItem(BowNecklaceItem.Tier.FLETCHER, props("fletchers_talisman")
                    .stacksTo(1).rarity(Rarity.UNCOMMON).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
    public static final RegistrySupplier<Item> DEAD_EYES_PENDANT = ITEMS.register("dead_eyes_pendant",
            () -> new BowNecklaceItem(BowNecklaceItem.Tier.DEAD_EYE, props("dead_eyes_pendant")
                    .stacksTo(1).rarity(Rarity.RARE).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB)));
    public static final RegistrySupplier<Item> CURSED_STONE = ITEMS.register("cursed_stone",
            () -> new CursedStone(props("cursed_stone").rarity(Rarity.EPIC).stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> SOUL_FRAGMENT = ITEMS.register("soul_fragment",
            () -> new SoulFragment(props("soul_fragment").stacksTo(64).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC)));

    //BOWS
    public static final RegistrySupplier<Item> AETHERS_CALL = ITEMS.register("aethers_call",
            () -> new AethersCall(props("aethers_call").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get()).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> ANCIENT_SAGE_BOW = ITEMS.register("ancient_sage_bow",
            () -> new AncientSageBow(props("ancient_sage_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> ARC_HEAVENS = ITEMS.register("arc_heavens",
            () -> new HeavensBow(props("arc_heavens").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> ARCANE_BOW = ITEMS.register("arcane_bow",
            () -> new ArcaneBow(props("arcane_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> ASTRAL_BOUND = ITEMS.register("astral_bound",
            () -> new AstralBow(props("astral_bound").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> AURORAS_GRACE = ITEMS.register("auroras_grace",
            () -> new AuroraBow(props("auroras_grace").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> BEACON_BEAM_BOW = ITEMS.register("beacon_beam_bow",
            () -> new BeaconBeamBow(props("beacon_beam_bow").durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> BURNT_RELIC = ITEMS.register("burnt_relic",
            () -> new BurntRelicBow(props("burnt_relic").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> CRIMSON_NEXUS = ITEMS.register("crimson_nexus",
            () -> new CrimsonNexusBow(props("crimson_nexus").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> CYROHEART_BOW = ITEMS.register("cyroheart_bow",
            () -> new IcicleJavelinBow(props("cyroheart_bow").durability(850).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> DRAGONS_BREATH = ITEMS.register("dragons_breath",
            () -> new DragonsBreathBow(props("dragons_breath").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> NECRO_FLAME_BOW = ITEMS.register("necro_flame_bow",
            () -> new CursedFlameBow(props("necro_flame_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> DARK_BOW = ITEMS.register("dark_bow",
            () -> new SonicBoomBow(props("dark_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> DEMONS_GRASP = ITEMS.register("demons_grasp",
        () -> new DemonsGrasp(props("demons_grasp").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> DUSK_REAPER = ITEMS.register("dusk_reaper",
        () -> new DuskReaperBow(props("dusk_reaper").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> EMERALD_SAGE_BOW = ITEMS.register("emerald_sage_bow",
            () -> new EmeraldSageBow(props("emerald_sage_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> ETHEREAL_HUNTER = ITEMS.register("ethereal_hunter",
            () -> new EtherealHunterBow(props("ethereal_hunter").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> FLAME_BOW = ITEMS.register("flame_bow",
            () -> new FlameBow(props("flame_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> FROSTBITE = ITEMS.register("frostbite",
            () -> new FrostbiteBow(props("frostbite").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> HUNTER_BOW = ITEMS.register("hunter_bow",
            () -> new HunterBow(props("hunter_bow").durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> IRONCLAD_BOW = ITEMS.register("ironclad_bow",
            () -> new IroncladBow(props("ironclad_bow").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> RADIANCE = ITEMS.register("radiance",
            () -> new RadianceBow(props("radiance").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> SCATTER_BOW = ITEMS.register("scatter_bow",
        () -> new ScatterBow(props("scatter_bow").durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> SENTINELS_WRATH= ITEMS.register("sentinels_wrath",
        () -> new SentinelBow(props("sentinels_wrath").durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> SHULKER_BLAST = ITEMS.register("shulker_blast",
            () -> new ShulkerBlastBow(props("shulker_blast").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> SOLAR_BOW = ITEMS.register("solar_bow",
            () -> new SolarBow(props("solar_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> SPECTRAL_WHISPER = ITEMS.register("spectral_whisper",
            () -> new SpectralWhisperBow(props("spectral_whisper").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> TIDAL_BOW = ITEMS.register("tidal_bow",
            () -> new TidalBow(props("tidal_bow").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> TORCHBEARER = ITEMS.register("torchbearer",
            () -> new TorchbearerBow(props("torchbearer").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> TWIN_SHADOWS = ITEMS.register("twin_shadows",
            () -> new TwinShadowsBow(props("twin_shadows").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> VERDANT_VIGOR = ITEMS.register("verdant_vigor",
            () -> new VerdantVigorBow(props("verdant_vigor").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> VERDANT_VIPER = ITEMS.register("verdant_viper",
            () -> new VerdantViperBow(props("verdant_viper").durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.EPIC).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> VITALITY_WEAVER = ITEMS.register("vitality_weaver",
            () -> new VitalityWeaverBow(props("vitality_weaver").durability(500).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
        public static final RegistrySupplier<Item> WEBSTRING = ITEMS.register("webstring",
            () -> new WebstringVolleyBow(props("webstring").durability(950).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.RARE).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));
    public static final RegistrySupplier<Item> WIND_BOW = ITEMS.register("wind_bow",
            () -> new WindBow(props("wind_bow").durability(750).arch$tab(TabRegistry.TOO_MANY_BOWS_TAB).rarity(Rarity.UNCOMMON).enchantable(16).repairable(ItemRegistry.POWER_CRYSTAL.get())));

    public static void register() {
        ITEMS.register();
    }
}
