package net.bandit.many_bows.fabric.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BowLootInjectorPlatformImpl {

    public static void registerLootTables() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (isEasyLootTable(id)) {
                tableBuilder.pool(createCommonBowPool().build());
            } else if (isMediumLootTable(id)) {
                tableBuilder.pool(createUncommonBowPool().build());
            } else if (isHardLootTable(id)) {
                tableBuilder.pool(createRareBowPool().build());
            } else if (isEndGameLootTable(id)) {
                tableBuilder.pool(createEpicBowPool().build());
            }
        });
    }

    private static boolean isEasyLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.SIMPLE_DUNGEON)
                || id.equals(BuiltInLootTables.ABANDONED_MINESHAFT);
    }

    private static boolean isMediumLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.JUNGLE_TEMPLE)
                || id.equals(BuiltInLootTables.PILLAGER_OUTPOST)
                || id.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
                || id.equals(BuiltInLootTables.SIMPLE_DUNGEON);
    }

    private static boolean isHardLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR)
                || id.equals(BuiltInLootTables.NETHER_BRIDGE)
                || id.equals(BuiltInLootTables.BASTION_TREASURE);
    }

    private static boolean isEndGameLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.END_CITY_TREASURE)
                || id.equals(BuiltInLootTables.NETHER_BRIDGE)
                || id.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
                || id.equals(BuiltInLootTables.BASTION_TREASURE);
    }

    private static LootPool.Builder createCommonBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/common_bows")));
    }

    private static LootPool.Builder createUncommonBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/uncommon_bows")));
    }

    private static LootPool.Builder createRareBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/rare_bows")));
    }

    private static LootPool.Builder createEpicBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/epic_bows")));
    }
}
