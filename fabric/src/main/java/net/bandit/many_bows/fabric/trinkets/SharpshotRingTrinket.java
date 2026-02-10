package net.bandit.many_bows.fabric.trinkets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class SharpshotRingTrinket implements Trinket {

    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "sharpshot_ring_bow_damage");

    // +0.15 => base 1.0 becomes 1.15 (15% more)
    private static final double BONUS = 0.15D;

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(
            ItemStack stack,
            SlotReference slot,
            LivingEntity entity,
            Identifier slotIdentifier
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();

        Holder<Attribute> holder =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DAMAGE.get());

        Identifier uniqueId = Identifier.fromNamespaceAndPath(
                MODIFIER_ID.getNamespace(),
                MODIFIER_ID.getPath() + "/" + slotIdentifier.toString().replace(':', '_')
        );

        map.put(holder, new AttributeModifier(uniqueId, BONUS, AttributeModifier.Operation.ADD_VALUE));
        return map;
    }
}
