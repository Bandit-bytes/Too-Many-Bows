package net.bandit.many_bows.fabric.trinkets;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.fabric.config.FabricCompatConfigHolder;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class StormboundSignetTrinket implements TrinketCallback {

    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "stormbound_signet_bow_damage");


    @Override
    public void forEachTrinketModifier(
            ItemStack stack,
            TrinketSlotAccess slot,
            LivingEntity entity,
            Identifier slotIdentifier,
            java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier> consumer
    ) {
        Holder<Attribute> holder =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DAMAGE.get());

        Identifier uniqueId = Identifier.fromNamespaceAndPath(
                MODIFIER_ID.getNamespace(),
                MODIFIER_ID.getPath() + "/" + slotIdentifier.toString().replace(':', '_')
        );

        consumer.accept(holder, new AttributeModifier(uniqueId, FabricCompatConfigHolder.get().stormboundSignetBonus, AttributeModifier.Operation.ADD_VALUE));
    }
}
