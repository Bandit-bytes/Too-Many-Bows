package net.bandit.many_bows.relic;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.function.Consumer;

public final class RelicData {
    private RelicData() {}
    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void edit(ItemStack stack, Consumer<CompoundTag> edit) {
        CompoundTag tag = read(stack); edit.accept(tag); stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
