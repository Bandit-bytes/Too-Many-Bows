package net.bandit.many_bows.neoforge.curio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class DrawSpeedGloveCurio implements ICurioItem {

    private static final ResourceLocation DRAW_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "glove_draw_speed");

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            ResourceLocation id,
            ItemStack stack
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();

        Holder<Attribute> holder =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DRAW_SPEED.get());

        ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(
                DRAW_SPEED_MODIFIER_ID.getNamespace(),
                DRAW_SPEED_MODIFIER_ID.getPath() + "/" + id.toString().replace(':', '_')
        );

        map.put(holder, new AttributeModifier(
                uniqueId,
                0.75D,
                AttributeModifier.Operation.ADD_VALUE
        ));

        return map;
    }
}
