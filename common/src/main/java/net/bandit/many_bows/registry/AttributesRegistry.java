package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AttributesRegistry {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.ATTRIBUTE);

    public static final RegistrySupplier<Attribute> BOW_DRAW_SPEED =
            ATTRIBUTES.register("bow_draw_speed",
                    () -> new RangedAttribute(
                            "attribute.too_many_bows.bow_draw_speed",
                            1.0D, 0.05D, 10.0D
                    ).setSyncable(true));

    public static final RegistrySupplier<Attribute> BOW_DAMAGE =
            ATTRIBUTES.register("bow_damage",
                    () -> new RangedAttribute(
                            "attribute.too_many_bows.bow_damage",
                            1.0D, 0.0D, 10.0D
                    ).setSyncable(true));

    public static final RegistrySupplier<Attribute> BOW_CRIT_CHANCE =
            ATTRIBUTES.register("bow_crit_chance",
                    () -> new RangedAttribute(
                            "attribute.too_many_bows.bow_crit_chance",
                            0.0D,
                            0.0D,
                            1.0D
                    ).setSyncable(true));


    public static void register() {
        ATTRIBUTES.register();
    }
}
