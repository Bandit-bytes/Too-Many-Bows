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
            if (isDungeonLootTable(id)) {
                LootPool.Builder customBowPool = createBowLootPool();
                tableBuilder.pool(customBowPool.build());
            }
        });
    }

    private static boolean isDungeonLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.SIMPLE_DUNGEON)
                || id.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
                || id.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR)
                || id.equals(BuiltInLootTables.JUNGLE_TEMPLE)
                || id.equals(BuiltInLootTables.NETHER_BRIDGE);
    }

    private static LootPool.Builder createBowLootPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/dungeon_bows")));
    }
}
