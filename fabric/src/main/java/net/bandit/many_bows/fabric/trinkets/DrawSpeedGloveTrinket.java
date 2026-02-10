package net.bandit.many_bows.fabric.trinkets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class DrawSpeedGloveTrinket implements Trinket {

    private static final Identifier DRAW_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "glove_draw_speed");

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(
            ItemStack stack,
            SlotReference slot,
            LivingEntity entity,
            Identifier slotIdentifier
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();

        Holder<Attribute> holder = entity.level().registryAccess()
                .lookupOrThrow(Registries.ATTRIBUTE)
                .get(AttributesRegistry.BOW_DRAW_SPEED.getKey())
                .orElse(null);

        if (holder == null) return map;

        Identifier uniqueId = Identifier.fromNamespaceAndPath(
                DRAW_SPEED_MODIFIER_ID.getNamespace(),
                DRAW_SPEED_MODIFIER_ID.getPath() + "/" + slotIdentifier.toString().replace(':', '_')
        );

        map.put(holder, new AttributeModifier(
                uniqueId,
                0.75D,
                AttributeModifier.Operation.ADD_VALUE
        ));

        return map;
    }
}
