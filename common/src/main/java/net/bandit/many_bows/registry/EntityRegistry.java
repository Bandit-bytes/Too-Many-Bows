package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.entity.*;
import net.bandit.many_bows.item.AncientSageBow;
import net.bandit.many_bows.item.SentinelsWrathBow;
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
                    .build(new ResourceLocation(MOD_ID, "dragons_breath_arrow").toString()));

    public static final RegistrySupplier<EntityType<SonicBoomProjectile>> SONIC_BOOM_PROJECTILE = ENTITY_TYPES.register("sonic_boom_projectile",
            () -> EntityType.Builder.<SonicBoomProjectile>of(SonicBoomProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "sonic_boom_projectile").toString()));

    public static final RegistrySupplier<EntityType<VenomArrow>> VENOM_ARROW = ENTITY_TYPES.register("venom_arrow",
            () -> EntityType.Builder.<VenomArrow>of(VenomArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("venom_arrow"));
    public static final RegistrySupplier<EntityType<FlameArrow>> FLAME_ARROW = ENTITY_TYPES.register("flame_arrow",
            () -> EntityType.Builder.<FlameArrow>of(FlameArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("flame_arrow"));
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
                    .build(new ResourceLocation(MOD_ID, "lightning_arrow").toString()));

    public static final RegistrySupplier<EntityType<WindProjectile>> WIND_PROJECTILE = ENTITY_TYPES.register("wind_projectile",
            () -> EntityType.Builder.<WindProjectile>of(WindProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "wind_projectile").toString()));

    public static final RegistrySupplier<EntityType<IcicleJavelin>> ICICLE_JAVELIN = ENTITY_TYPES.register("icicle_javelin",
            () -> EntityType.Builder.<IcicleJavelin>of(IcicleJavelin::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "icicle_javelin").toString()));

    public static final RegistrySupplier<EntityType<HunterArrow>> HUNTER_ARROW = ENTITY_TYPES.register("hunter_arrow",
            () -> EntityType.Builder.<HunterArrow>of(HunterArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "hunter_arrow").toString()));

    public static final RegistrySupplier<EntityType<HunterXPArrow>> HUNTER_XP_ARROW = ENTITY_TYPES.register("hunter_xp_arrow",
            () -> EntityType.Builder.<HunterXPArrow>of(HunterXPArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "hunter_arrow").toString()));

    public static final RegistrySupplier<EntityType<AncientSageArrow>> ANCIENT_SAGE_ARROW = ENTITY_TYPES.register("ancient_sage_arrow",
            () -> EntityType.Builder.<AncientSageArrow>of(AncientSageArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "ancient_sage_arrow").toString()));

    public static final RegistrySupplier<EntityType<SentinelArrow>> SENTINEL_ARROW = ENTITY_TYPES.register("sentinel_arrow",
            () -> EntityType.Builder.<SentinelArrow>of(SentinelArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "sentinel_arrow").toString()));

    public static final RegistrySupplier<EntityType<ShulkerBlastProjectile>> SHULKER_BLAST_PROJECTILE = ENTITY_TYPES.register("shulker_blast_projectile",
            () -> EntityType.Builder.<ShulkerBlastProjectile>of(ShulkerBlastProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "shulker_blast_projectile").toString()));




    public static void register() {
        ENTITY_TYPES.register();
    }
}
