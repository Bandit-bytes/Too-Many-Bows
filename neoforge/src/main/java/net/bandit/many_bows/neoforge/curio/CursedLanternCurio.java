package net.bandit.many_bows.neoforge.curio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.common.LanternLightHelper;
import net.bandit.many_bows.neoforge.config.NeoForgeCompatConfigHolder;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CursedLanternCurio implements ICurioItem {

    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "necro_bow_damage");

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            Identifier id,
            ItemStack stack
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();

        Holder<Attribute> holder =
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.NECRO_BOW_DAMAGE.get());

        Identifier uniqueId = Identifier.fromNamespaceAndPath(
                MODIFIER_ID.getNamespace(),
                MODIFIER_ID.getPath() + "/" + id.toString().replace(':', '_')
        );

        map.put(holder, new AttributeModifier(
                uniqueId,
                NeoForgeCompatConfigHolder.get().cursedLanternNecroDamageBonus,
                AttributeModifier.Operation.ADD_VALUE
        ));

        return map;
    }
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            LanternLightHelper.ensureLanternLight(
                    player,
                    NeoForgeCompatConfigHolder.get().cursedLanternLightLevel
            );
        }
    }
}