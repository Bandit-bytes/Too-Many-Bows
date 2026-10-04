package net.bandit.many_bows.relic;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class RelicData {
    private RelicData() {}
    public static Data read(ItemStack stack) {
        return new Data(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }
    public static void edit(ItemStack stack, Consumer<Data> edit) {
        Data data = read(stack);
        edit.accept(data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data.tag));
    }
    public static List<ItemStack> inventory(Player player) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) items.add(player.getInventory().getItem(i));
        return items;
    }
    public static final class Data {
        private final CompoundTag tag;
        private Data(CompoundTag tag) { this.tag = tag; }
        public int getInt(String key) { return tag.getIntOr(key, 0); }
        public long getLong(String key) { return tag.getLongOr(key, 0); }
        public double getDouble(String key) { return tag.getDoubleOr(key, 0); }
        public boolean getBoolean(String key) { return tag.getBooleanOr(key, false); }
        public boolean contains(String key) { return tag.contains(key); }
        public boolean hasUUID(String key) { return tag.read(key, UUIDUtil.CODEC).isPresent(); }
        public UUID getUUID(String key) { return tag.read(key, UUIDUtil.CODEC).orElse(new UUID(0, 0)); }
        public void putInt(String key, int value) { tag.putInt(key, value); }
        public void putLong(String key, long value) { tag.putLong(key, value); }
        public void putDouble(String key, double value) { tag.putDouble(key, value); }
        public void putBoolean(String key, boolean value) { tag.putBoolean(key, value); }
        public void putUUID(String key, UUID value) { tag.store(key, UUIDUtil.CODEC, value); }
        public void remove(String key) { tag.remove(key); }
    }
}
