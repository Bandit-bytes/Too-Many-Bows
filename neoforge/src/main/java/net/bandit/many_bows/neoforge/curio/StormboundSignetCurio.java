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

public class StormboundSignetCurio implements ICurioItem {

    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "stormbound_signet_bow_damage");

    private static final double BONUS = 0.30D;

    @Override
    public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack) {
        Holder<Attribute> bowDamage =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DAMAGE.get());

        AttributeModifier modifier =
                new AttributeModifier(MODIFIER_ID,
                        BONUS,
                        AttributeModifier.Operation.ADD_VALUE
                );

        return CurioAttributeModifiers.builder()
                .addModifier(bowDamage, modifier)
                .build();
    }
}
