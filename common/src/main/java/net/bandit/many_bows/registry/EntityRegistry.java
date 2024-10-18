package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.entity.DragonsBreathArrow;
import net.bandit.many_bows.entity.FrostbiteArrow;
import net.bandit.many_bows.entity.SonicBoomProjectile;
import net.bandit.many_bows.entity.VenomArrow;
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
                    .build("frostbite_arrow")
    );
    public static final RegistrySupplier<EntityType<DragonsBreathArrow>> DRAGONS_BREATH_ARROW = ENTITY_TYPES.register("dragons_breath_arrow",
            () -> EntityType.Builder.<DragonsBreathArrow>of(DragonsBreathArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "dragons_breath_arrow").toString())
    );

    public static final RegistrySupplier<EntityType<SonicBoomProjectile>> SONIC_BOOM_PROJECTILE = ENTITY_TYPES.register("sonic_boom_projectile",
            () -> EntityType.Builder.<SonicBoomProjectile>of(SonicBoomProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MOD_ID, "sonic_boom_projectile").toString()));

    public static final RegistrySupplier<EntityType<VenomArrow>> VENOM_ARROW = ENTITY_TYPES.register("venom_arrow",
            () -> EntityType.Builder.<VenomArrow>of(VenomArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("venom_arrow"));

    public static void register() {
        ENTITY_TYPES.register();
    }
}
