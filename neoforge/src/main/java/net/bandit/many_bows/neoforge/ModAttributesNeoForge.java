package net.bandit.many_bows.neoforge;

import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;


public class ModAttributesNeoForge {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        Holder<Attribute> bowDrawSpeed = BuiltInRegistries.ATTRIBUTE
                .getOrThrow(ResourceKey.create(
                        Registries.ATTRIBUTE,
                        Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_draw_speed")
                ));
        Holder<Attribute> bowDamage = BuiltInRegistries.ATTRIBUTE
                .getOrThrow(ResourceKey.create(
                        Registries.ATTRIBUTE,
                        Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_damage")
                ));
        Holder<Attribute> bowCritChance = BuiltInRegistries.ATTRIBUTE
                .getOrThrow(ResourceKey.create(
                        Registries.ATTRIBUTE,
                        Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_crit_chance")
                ));

        event.add(EntityType.PLAYER, bowDamage);
        event.add(EntityType.PLAYER, bowDrawSpeed);
        event.add(EntityType.PLAYER, bowCritChance);
    }
}
