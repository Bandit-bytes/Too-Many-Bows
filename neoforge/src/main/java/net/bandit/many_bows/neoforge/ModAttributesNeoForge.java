package net.bandit.many_bows.neoforge;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModAttributesNeoForge {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        Holder<Attribute> bowDrawSpeed = BuiltInRegistries.ATTRIBUTE
                .getHolderOrThrow(ResourceKey.create(
                        Registries.ATTRIBUTE,
                        ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_draw_speed")
                ));
        Holder<Attribute> bowDamage = BuiltInRegistries.ATTRIBUTE
                .getHolderOrThrow(ResourceKey.create(
                        Registries.ATTRIBUTE,
                        ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_damage")
                ));
        Holder<Attribute> bowCritChance = BuiltInRegistries.ATTRIBUTE
                .getHolderOrThrow(ResourceKey.create(
                        Registries.ATTRIBUTE,
                        ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_crit_chance")
                ));

        event.add(EntityType.PLAYER, bowDamage);
        event.add(EntityType.PLAYER, bowDrawSpeed);
        event.add(EntityType.PLAYER, bowCritChance);
    }
}
