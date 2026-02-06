package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.config.BowLootConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModLootModifiers {

    private static final BowLootConfig CONFIG = BowLootConfig.loadConfig();

    public static void registerLootModifiers() {
        final Set<ResourceKey<LootTable>> easyTables = parseLootTables(CONFIG.easyLootTables);
        final Set<ResourceKey<LootTable>> mediumTables = parseLootTables(CONFIG.mediumLootTables);
        final Set<ResourceKey<LootTable>> hardTables = parseLootTables(CONFIG.hardLootTables);
        final Set<ResourceKey<LootTable>> endgameTables = parseLootTables(CONFIG.endgameLootTables);

        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin) return;

            if (Boolean.TRUE.equals(CONFIG.easyLootEnabled) && easyTables.contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(CONFIG.easyLootDropChance, CONFIG.easyLootItems);
                if (pool != null) context.addPool(pool);
            }

            if (Boolean.TRUE.equals(CONFIG.mediumLootEnabled) && mediumTables.contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(CONFIG.mediumLootDropChance, CONFIG.mediumLootItems);
                if (pool != null) context.addPool(pool);
            }

            if (Boolean.TRUE.equals(CONFIG.hardLootEnabled) && hardTables.contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(CONFIG.hardLootDropChance, CONFIG.hardLootItems);
                if (pool != null) context.addPool(pool);
            }

            if (Boolean.TRUE.equals(CONFIG.endgameLootEnabled) && endgameTables.contains(key)) {
                LootPool.Builder pool = createConfigDrivenPool(CONFIG.endgameLootDropChance, CONFIG.endgameLootItems);
                if (pool != null) context.addPool(pool);
            }
        });
    }

    private static Set<ResourceKey<LootTable>> parseLootTables(List<String> ids) {
        Set<ResourceKey<LootTable>> out = new HashSet<>();
        if (ids == null) return out;

        for (String s : ids) {
            Identifier rl = Identifier.tryParse(s);
            if (rl == null) {
                System.err.println("[too_many_bows] Invalid loot table id in config: " + s);
                continue;
            }
            out.add(ResourceKey.create(Registries.LOOT_TABLE, rl));
        }
        return out;
    }

    private static LootPool.Builder createConfigDrivenPool(float chance, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) return null;

        chance = Math.max(0.0F, Math.min(1.0F, chance));

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(UniformGenerator.between(0.0F, 1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        boolean addedAny = false;

        for (String id : itemIds) {
            Identifier rl = Identifier.tryParse(id);
            if (rl == null) {
                System.err.println("[too_many_bows] Invalid item id in loot config: " + id);
                continue;
            }

            Optional<Holder.Reference<Item>> ref = BuiltInRegistries.ITEM.get(rl);
            if (ref.isEmpty()) {
                System.err.println("[too_many_bows] Unknown item in loot config (not registered): " + id);
                continue;
            }

            Item item = ref.get().value();
            pool.add(LootItem.lootTableItem(item));
            addedAny = true;
        }

        return addedAny ? pool : null;
    }

}
