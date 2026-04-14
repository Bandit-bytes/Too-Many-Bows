package net.bandit.many_bows.fabric.trinkets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.common.LanternLightHelper;
import net.bandit.many_bows.fabric.config.FabricCompatConfigHolder;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SoulLanternTrinket implements Trinket {

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "soul_lantern_gravewire_damage");

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(
            ItemStack stack,
            SlotReference slot,
            LivingEntity entity,
            ResourceLocation slotIdentifier
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();

        Holder<Attribute> holder =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.GRAVEWIRE_BOW_DAMAGE.get());

        ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(
                MODIFIER_ID.getNamespace(),
                MODIFIER_ID.getPath() + "/" + slotIdentifier.toString().replace(':', '_')
        );

        map.put(holder, new AttributeModifier(
                uniqueId,
                FabricCompatConfigHolder.get().soulLanternGravewireDamageBonus,
                AttributeModifier.Operation.ADD_VALUE
        ));

        return map;
    }
    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof Player player) {
            LanternLightHelper.ensureLanternLight(
                    player,
                    FabricCompatConfigHolder.get().soulLanternLightLevel
            );
        }
    }
}