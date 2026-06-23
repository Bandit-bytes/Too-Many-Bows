package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ModLootModifiers {
    private ModLootModifiers() {
    }

    public static void registerLootModifiers() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin) return;

            BowLootConfig config = ManyBowsConfigHolder.get();

            if (Boolean.TRUE.equals(config.easyLootEnabled)
                    && parseLootTables(config.easyLootTables).contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(config.easyLootDropChance, config.easyLootItems);
                if (pool != null) context.addPool(pool);
            }
            if (Boolean.TRUE.equals(config.mediumLootEnabled)
                    && parseLootTables(config.mediumLootTables).contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(config.mediumLootDropChance, config.mediumLootItems);
                if (pool != null) context.addPool(pool);
            }
            if (Boolean.TRUE.equals(config.hardLootEnabled)
                    && parseLootTables(config.hardLootTables).contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(config.hardLootDropChance, config.hardLootItems);
                if (pool != null) context.addPool(pool);
            }
            if (Boolean.TRUE.equals(config.endgameLootEnabled)
                    && parseLootTables(config.endgameLootTables).contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(config.endgameLootDropChance, config.endgameLootItems);
                if (pool != null) context.addPool(pool);
            }
        });
    }

    private static Set<ResourceKey<LootTable>> parseLootTables(List<String> ids) {
        Set<ResourceKey<LootTable>> out = new HashSet<>();
        if (ids == null) return out;
        for (String value : ids) {
            Identifier id = Identifier.tryParse(value);
            if (id != null) out.add(ResourceKey.create(Registries.LOOT_TABLE, id));
        }
        return out;
    }

    private static LootPool.Builder createConfigDrivenPool(float chance, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) return null;
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        if (chance <= 0.0F) return null;

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        boolean added = false;
        for (String value : itemIds) {
            Identifier id = Identifier.tryParse(value);
            if (id == null) continue;
            Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.get(id);
            if (item.isPresent()) {
                pool.add(LootItem.lootTableItem(item.get().value()));
                added = true;
            }
        }
        return added ? pool : null;
    }
}
