package net.bandit.many_bows.forge.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

@Mod.EventBusSubscriber
public class BowLootInjectorPlatformImpl {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (isDungeonLootTable(id)) {
            LootPool customBowPool = createBowLootPool();
            event.getTable().addPool(customBowPool);
        }
    }

    private static boolean isDungeonLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.SIMPLE_DUNGEON)
                || id.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
                || id.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR)
                || id.equals(BuiltInLootTables.JUNGLE_TEMPLE)
                || id.equals(BuiltInLootTables.NETHER_BRIDGE);
    }

    private static LootPool createBowLootPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/dungeon_bows")))
                .build();
    }
}
