//package net.bandit.many_bows.fabric.trinkets;
//
//import com.google.common.collect.HashMultimap;
//import com.google.common.collect.Multimap;
//import dev.emi.trinkets.api.SlotReference;
//import dev.emi.trinkets.api.Trinket;
//import net.bandit.many_bows.ManyBowsMod;
//import net.bandit.many_bows.registry.AttributesRegistry;
//import net.minecraft.core.Holder;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.ai.attributes.Attribute;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
//import net.minecraft.world.item.ItemStack;
//
//public class StormboundSignetTrinket implements Trinket {
//
//    private static final ResourceLocation MODIFIER_ID =
//            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "stormbound_signet_bow_damage");
//
//    private static final double BONUS = 0.30D;
//
//    @Override
//    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(
//            ItemStack stack,
//            dev.emi.trinkets.api.SlotReference slot,
//            LivingEntity entity,
//            ResourceLocation slotIdentifier
//    ) {
//        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
//
//        Holder<Attribute> holder =
//                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DAMAGE.get());
//
//        ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(
//                MODIFIER_ID.getNamespace(),
//                MODIFIER_ID.getPath() + "/" + slotIdentifier.toString().replace(':', '_')
//        );
//
//        map.put(holder, new AttributeModifier(uniqueId, BONUS, AttributeModifier.Operation.ADD_VALUE));
//        return map;
//    }
//}
