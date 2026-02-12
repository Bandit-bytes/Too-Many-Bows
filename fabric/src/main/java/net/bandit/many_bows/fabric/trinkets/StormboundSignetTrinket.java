package net.bandit.many_bows.fabric.trinkets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class StormboundSignetTrinket implements Trinket {

    private static final double BONUS = 0.30D;

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(AttributesRegistry.BOW_DAMAGE.get(), new AttributeModifier(uuid, "stormbound_signet_bow_damage", BONUS, AttributeModifier.Operation.ADDITION));
        return map;
    }
}
