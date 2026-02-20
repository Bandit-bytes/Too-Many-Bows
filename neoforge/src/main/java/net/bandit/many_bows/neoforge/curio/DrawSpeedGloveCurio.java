package net.bandit.many_bows.neoforge.curio;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class DrawSpeedGloveCurio implements ICurioItem {

    private static final Identifier DRAW_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "glove_draw_speed");

    private static final double BONUS = 0.75D;

    @Override
    public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack) {
        Holder<Attribute> drawSpeed =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DRAW_SPEED.get());

        AttributeModifier modifier =
                new AttributeModifier(
                        DRAW_SPEED_MODIFIER_ID,
                        BONUS,
                        AttributeModifier.Operation.ADD_VALUE
                );

        return CurioAttributeModifiers.builder()
                .addModifier(drawSpeed, modifier, "glove")
                .build();
    }
}