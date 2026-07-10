package net.bandit.many_bows.fabric.trinkets;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.fabric.config.FabricCompatConfigHolder;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class DrawSpeedGloveTrinket implements TrinketCallback {

    private static final Identifier DRAW_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "glove_draw_speed");

    @Override
    public void forEachTrinketModifier(
            ItemStack stack,
            TrinketSlotAccess slot,
            LivingEntity entity,
            Identifier slotIdentifier,
            java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier> consumer
    ) {
        Holder<Attribute> holder = entity.level().registryAccess()
                .lookupOrThrow(Registries.ATTRIBUTE)
                .get(AttributesRegistry.BOW_DRAW_SPEED.getKey())
                .orElse(null);

        if (holder == null) return;

        Identifier uniqueId = Identifier.fromNamespaceAndPath(
                DRAW_SPEED_MODIFIER_ID.getNamespace(),
                DRAW_SPEED_MODIFIER_ID.getPath() + "/" + slotIdentifier.toString().replace(':', '_')
        );

        consumer.accept(holder, new AttributeModifier(
                uniqueId,
                FabricCompatConfigHolder.get().drawSpeedGloveBonus,
                AttributeModifier.Operation.ADD_VALUE
        ));

    }
}
