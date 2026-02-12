package net.bandit.many_bows.forge.curio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class DrawSpeedGloveCurio implements ICurioItem {

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        Attribute attr = AttributesRegistry.BOW_DRAW_SPEED.get();
        map.put(attr, new AttributeModifier(uuid, "glove_draw_speed", 0.75D, AttributeModifier.Operation.ADDITION));
        return map;
    }
}
