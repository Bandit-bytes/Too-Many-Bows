package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModLootModifiers {

    private static boolean registered;

    private ModLootModifiers() {
    }

    /*
     * No-argument signature intentionally matches the mod's original build.
     */
    public static void registerLootModifiers() {
        if (registered) {
            return;
        }

        registered = true;

        BowLootConfig config =
                ManyBowsConfigHolder.CONFIG;

        final Set<ResourceLocation> easyTables =
                parseLootTables(config.easyLootTables);
        final Set<ResourceLocation> mediumTables =
                parseLootTables(config.mediumLootTables);
        final Set<ResourceLocation> hardTables =
                parseLootTables(config.hardLootTables);
        final Set<ResourceLocation> endgameTables =
                parseLootTables(config.endgameLootTables);
        final Set<ResourceLocation> accessoryTables =
                parseLootTables(config.accessoryLootTables);

        LootEvent.MODIFY_LOOT_TABLE.register(
                (lootDataManager, id, context, builtin) -> {
                    if (!builtin) {
                        return;
                    }

                    if (Boolean.TRUE.equals(config.easyLootEnabled)
                            && easyTables.contains(id)) {
                        LootPool.Builder pool = createConfigDrivenPool(
                                config.easyLootDropChance,
                                config.easyLootItems,
                                "easy"
                        );
                        if (pool != null) context.addPool(pool);
                    }

                    if (Boolean.TRUE.equals(config.mediumLootEnabled)
                            && mediumTables.contains(id)) {
                        LootPool.Builder pool = createConfigDrivenPool(
                                config.mediumLootDropChance,
                                config.mediumLootItems,
                                "medium"
                        );
                        if (pool != null) context.addPool(pool);
                    }

                    if (Boolean.TRUE.equals(config.hardLootEnabled)
                            && hardTables.contains(id)) {
                        LootPool.Builder pool = createConfigDrivenPool(
                                config.hardLootDropChance,
                                config.hardLootItems,
                                "hard"
                        );
                        if (pool != null) context.addPool(pool);
                    }

                    if (Boolean.TRUE.equals(config.endgameLootEnabled)
                            && endgameTables.contains(id)) {
                        LootPool.Builder pool = createConfigDrivenPool(
                                config.endgameLootDropChance,
                                config.endgameLootItems,
                                "endgame"
                        );
                        if (pool != null) context.addPool(pool);
                    }

                    if (Boolean.TRUE.equals(config.accessoryLootEnabled)
                            && accessoryTables.contains(id)) {
                        LootPool.Builder pool = createConfigDrivenPool(
                                config.accessoryLootDropChance,
                                config.accessoryLootItems,
                                "accessory"
                        );
                        if (pool != null) context.addPool(pool);
                    }
                }
        );
    }

    private static Set<ResourceLocation> parseLootTables(
            List<String> ids
    ) {
        Set<ResourceLocation> parsed = new HashSet<>();

        if (ids == null) {
            return parsed;
        }

        for (String id : ids) {
            ResourceLocation resourceLocation =
                    ResourceLocation.tryParse(id);

            if (resourceLocation != null) {
                parsed.add(resourceLocation);
            } else {
                System.err.println(
                        "[too_many_bows] Invalid loot table id: "
                                + id
                );
            }
        }

        return parsed;
    }

    private static LootPool.Builder createConfigDrivenPool(
            float chance,
            List<String> itemIds,
            String poolName
    ) {
        List<Item> validItems =
                resolveItems(itemIds, poolName);

        if (validItems.isEmpty()) {
            System.err.println(
                    "[too_many_bows] Skipping empty "
                            + poolName
                            + " loot pool."
            );
            return null;
        }

        LootPool.Builder pool =
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(
                                LootItemRandomChanceCondition
                                        .randomChance(chance)
                        );

        for (Item item : validItems) {
            pool.add(LootItem.lootTableItem(item));
        }

        return pool;
    }

    private static List<Item> resolveItems(
            List<String> itemIds,
            String poolName
    ) {
        List<Item> items = new ArrayList<>();

        if (itemIds == null) {
            return items;
        }

        ResourceLocation airId =
                BuiltInRegistries.ITEM.getKey(Items.AIR);

        for (String id : itemIds) {
            ResourceLocation resourceLocation =
                    ResourceLocation.tryParse(id);

            if (resourceLocation == null) {
                System.err.println(
                        "[too_many_bows] Invalid item id in "
                                + poolName
                                + " loot config: "
                                + id
                );
                continue;
            }

            Item item =
                    BuiltInRegistries.ITEM.get(resourceLocation);

            if (item == Items.AIR
                    && !resourceLocation.equals(airId)) {
                System.err.println(
                        "[too_many_bows] Unknown item in "
                                + poolName
                                + " loot config: "
                                + id
                );
                continue;
            }

            items.add(item);
        }

        return items;
    }
}
