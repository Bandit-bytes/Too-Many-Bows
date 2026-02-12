package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.config.BowLootConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModLootModifiers {

    private static final BowLootConfig CONFIG = BowLootConfig.loadConfig();

    public static void registerLootModifiers() {
        final Set<ResourceLocation> easyTables = parseLootTables(CONFIG.easyLootTables);
        final Set<ResourceLocation> mediumTables = parseLootTables(CONFIG.mediumLootTables);
        final Set<ResourceLocation> hardTables = parseLootTables(CONFIG.hardLootTables);
        final Set<ResourceLocation> endgameTables = parseLootTables(CONFIG.endgameLootTables);

        LootEvent.MODIFY_LOOT_TABLE.register((lootDataManager, id, context, builtin) -> {
            if (!builtin) return;

            if (Boolean.TRUE.equals(CONFIG.easyLootEnabled) && easyTables.contains(id)) {
                context.addPool(createConfigDrivenPool(CONFIG.easyLootDropChance, CONFIG.easyLootItems));
            }
            if (Boolean.TRUE.equals(CONFIG.mediumLootEnabled) && mediumTables.contains(id)) {
                context.addPool(createConfigDrivenPool(CONFIG.mediumLootDropChance, CONFIG.mediumLootItems));
            }
            if (Boolean.TRUE.equals(CONFIG.hardLootEnabled) && hardTables.contains(id)) {
                context.addPool(createConfigDrivenPool(CONFIG.hardLootDropChance, CONFIG.hardLootItems));
            }
            if (Boolean.TRUE.equals(CONFIG.endgameLootEnabled) && endgameTables.contains(id)) {
                context.addPool(createConfigDrivenPool(CONFIG.endgameLootDropChance, CONFIG.endgameLootItems));
            }
        });
    }

    private static Set<ResourceLocation> parseLootTables(List<String> ids) {
        Set<ResourceLocation> out = new HashSet<>();
        if (ids == null) return out;

        for (String s : ids) {
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl != null) out.add(rl);
            else {
                System.err.println("[too_many_bows] Invalid loot table id in config: " + s);
            }
        }
        return out;
    }

    private static LootPool.Builder createConfigDrivenPool(float chance, List<String> itemIds) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(UniformGenerator.between(0.0F, 1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        if (itemIds == null || itemIds.isEmpty()) {
            return pool;
        }

        for (String id : itemIds) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) {
                System.err.println("[too_many_bows] Invalid items id in loot config: " + id);
                continue;
            }

            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item == Items.AIR) {
                System.err.println("[too_many_bows] Unknown items in loot config (not registered): " + id);
                continue;
            }

            pool.add(LootItem.lootTableItem(item));
        }

        return pool;
    }

}
