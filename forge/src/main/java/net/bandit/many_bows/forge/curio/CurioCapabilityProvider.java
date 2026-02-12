package net.bandit.many_bows.forge.curio;

import com.google.common.collect.Multimap;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class CurioCapabilityProvider implements ICapabilityProvider {

    private final LazyOptional<ICurio> opt;

    public CurioCapabilityProvider(ICurioItem curioItem, ItemStack stack) {
        this.opt = LazyOptional.of(() -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                curioItem.curioTick(slotContext, stack);
            }

            @Override
            public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                curioItem.onEquip(slotContext, prevStack, stack);
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                curioItem.onUnequip(slotContext, newStack, stack);
            }

            @Override
            public boolean canEquip(SlotContext slotContext) {
                return curioItem.canEquip(slotContext, stack);
            }

            @Override
            public boolean canUnequip(SlotContext slotContext) {
                return curioItem.canUnequip(slotContext, stack);
            }

            @Override
            public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
                return curioItem.getAttributeModifiers(slotContext, uuid, stack);
            }
        });
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CuriosCapability.ITEM ? opt.cast() : LazyOptional.empty();
    }
}
