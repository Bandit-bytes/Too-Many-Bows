package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.entity.*;
import net.bandit.many_bows.entity.AstralArrow;
import net.bandit.many_bows.item.TorchbearerBow;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static net.bandit.many_bows.ManyBowsMod.MOD_ID;

public class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<FrostbiteArrow>> FROSTBITE_ARROW = ENTITY_TYPES.register("frostbite_arrow",
            () -> EntityType.Builder.<FrostbiteArrow>of(FrostbiteArrow::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .build("frostbite_arrow"));
    public static final RegistrySupplier<EntityType<DragonsBreathArrow>> DRAGONS_BREATH_ARROW = ENTITY_TYPES.register("dragons_breath_arrow",
            () -> EntityType.Builder.<DragonsBreathArrow>of(DragonsBreathArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "dragons_breath_arrow").toString()));

    public static final RegistrySupplier<EntityType<SonicBoomProjectile>> SONIC_BOOM_PROJECTILE = ENTITY_TYPES.register("sonic_boom_projectile",
            () -> EntityType.Builder.<SonicBoomProjectile>of(SonicBoomProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "sonic_boom_projectile").toString()));

    public static final RegistrySupplier<EntityType<VenomArrow>> VENOM_ARROW = ENTITY_TYPES.register("venom_arrow",
            () -> EntityType.Builder.<VenomArrow>of(VenomArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("venom_arrow"));
    public static final RegistrySupplier<EntityType<FlameArrow>> FLAME_ARROW = ENTITY_TYPES.register("flame_arrow",
            () -> EntityType.Builder.<FlameArrow>of(FlameArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("flame_arrow"));
    public static final RegistrySupplier<EntityType<SolarArrow>> SOLAR_ARROW = ENTITY_TYPES.register("solar_arrow",
            () -> EntityType.Builder.<SolarArrow>of(SolarArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("solar_arrow"));
    public static final RegistrySupplier<EntityType<TidalArrow>> TIDAL_ARROW = ENTITY_TYPES.register("tidal_arrow",
            () -> EntityType.Builder.<TidalArrow>of(TidalArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("tidal_arrow"));
    public static final RegistrySupplier<EntityType<CursedFlameArrow>> CURSED_FLAME_ARROW = ENTITY_TYPES.register("cursed_flame_arrow",
            () -> EntityType.Builder.<CursedFlameArrow>of(CursedFlameArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("cursed_flame_arrow"));
    public static final RegistrySupplier<EntityType<LightningArrow>> LIGHTNING_ARROW = ENTITY_TYPES.register("lightning_arrow",
            () -> EntityType.Builder.<LightningArrow>of(LightningArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "lightning_arrow").toString()));

    public static final RegistrySupplier<EntityType<WindProjectile>> WIND_PROJECTILE = ENTITY_TYPES.register("wind_projectile",
            () -> EntityType.Builder.<WindProjectile>of(WindProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "wind_projectile").toString()));

    public static final RegistrySupplier<EntityType<IcicleJavelin>> ICICLE_JAVELIN = ENTITY_TYPES.register("icicle_javelin",
            () -> EntityType.Builder.<IcicleJavelin>of(IcicleJavelin::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "icicle_javelin").toString()));

    public static final RegistrySupplier<EntityType<HunterArrow>> HUNTER_ARROW = ENTITY_TYPES.register("hunter_arrow",
            () -> EntityType.Builder.<HunterArrow>of(HunterArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "hunter_arrow").toString()));

    public static final RegistrySupplier<EntityType<AstralArrow>> ASTRAL_ARROW = ENTITY_TYPES.register("astral_arrow",
            () -> EntityType.Builder.<AstralArrow>of(AstralArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "astral_arrow").toString()));

    public static final RegistrySupplier<EntityType<HunterXPArrow>> HUNTER_XP_ARROW = ENTITY_TYPES.register("hunter_xp_arrow",
            () -> EntityType.Builder.<HunterXPArrow>of(HunterXPArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "hunter_xp_arrow").toString()));

    public static final RegistrySupplier<EntityType<AncientSageArrow>> ANCIENT_SAGE_ARROW = ENTITY_TYPES.register("ancient_sage_arrow",
            () -> EntityType.Builder.<AncientSageArrow>of(AncientSageArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "ancient_sage_arrow").toString()));

    public static final RegistrySupplier<EntityType<SentinelWrathArrow>> SENTINEL_ARROW = ENTITY_TYPES.register("sentinel_arrow",
            () -> EntityType.Builder.<SentinelWrathArrow>of(SentinelWrathArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "sentinel_arrow").toString()));

    public static final RegistrySupplier<EntityType<VitalityArrow>> VITALITY_ARROW = ENTITY_TYPES.register("vitality_arrow",
            () -> EntityType.Builder.<VitalityArrow>of(VitalityArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "vitality_arrow").toString()));

    public static final RegistrySupplier<EntityType<EtherealArrow>> ETHEREAL_ARROW = ENTITY_TYPES.register("ethereal_arrow",
            () -> EntityType.Builder.<EtherealArrow>of(EtherealArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "ethereal_arrow").toString()));

    public static final RegistrySupplier<EntityType<ShulkerBlastProjectile>> SHULKER_BLAST_PROJECTILE = ENTITY_TYPES.register("shulker_blast_projectile",
            () -> EntityType.Builder.<ShulkerBlastProjectile>of(ShulkerBlastProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "shulker_blast_projectile").toString()));

    public static final RegistrySupplier<EntityType<SpectralArrow>> SPECTRAL_ARROW = ENTITY_TYPES.register("spectral_arrow",
            () -> EntityType.Builder.<SpectralArrow>of(SpectralArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "spectral_arrow").toString()));

    public static final RegistrySupplier<EntityType<AuroraArrowEntity>> AURORA_ARROW = ENTITY_TYPES.register("aurora_arrow",
            () -> EntityType.Builder.<AuroraArrowEntity>of(AuroraArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "aurora_arrow").toString()));

    public static final RegistrySupplier<EntityType<RiftEntity>> RIFT_ENTITY = ENTITY_TYPES.register("rift_entity",
            () -> EntityType.Builder.<RiftEntity>of(RiftEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "aurora_arrow").toString()));

    public static final RegistrySupplier<EntityType<RadiantArrow>> RADIANT_ARROW = ENTITY_TYPES.register("radiant_arrow",
            () -> EntityType.Builder.<RadiantArrow>of(RadiantArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "radiant_arrow").toString()));

    public static final RegistrySupplier<EntityType<DuskReaperArrow>> DUSK_REAPER_ARROW = ENTITY_TYPES.register("dusk_reaper_arrow",
            () -> EntityType.Builder.<DuskReaperArrow>of(DuskReaperArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "dusk_reaper_arrow").toString()));
    public static final RegistrySupplier<EntityType<IronCladArrow>> IRONCLAD_ARROW = ENTITY_TYPES.register("ironclad_arrow",
            () -> EntityType.Builder.<IronCladArrow>of(IronCladArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "ironclad_arrow").toString()));

    public static final RegistrySupplier<EntityType<WebstringArrow>> WEBSTRING_ARROW = ENTITY_TYPES.register("webstring_arrow",
            () -> EntityType.Builder.<WebstringArrow>of(WebstringArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "webstring_arrow").toString()));

    public static final RegistrySupplier<EntityType<TorchbearerArrow>> TORCHBEARER_ARROW = ENTITY_TYPES.register("torchbearer_arrow",
            () -> EntityType.Builder.<TorchbearerArrow>of(TorchbearerArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "torchbearer_arrow").toString()));

    public static final RegistrySupplier<EntityType<LightOrbEntity>> LIGHT_ORB = ENTITY_TYPES.register("light_orb",
            () -> EntityType.Builder.<LightOrbEntity>of(LightOrbEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(32)
                    .updateInterval(20)
                    .build(ResourceLocation.fromNamespaceAndPath(MOD_ID, "light_orb").toString()));


    public static void register() {
        ENTITY_TYPES.register();
    }
}
