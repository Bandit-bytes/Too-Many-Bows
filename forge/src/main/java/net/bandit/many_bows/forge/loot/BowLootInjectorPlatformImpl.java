package net.bandit.many_bows.forge.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
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

        if (isEasyLootTable(id)) {
            event.getTable().addPool(createCommonBowPool());
        } else if (isMediumLootTable(id)) {
            event.getTable().addPool(createUncommonBowPool());
        } else if (isHardLootTable(id)) {
            event.getTable().addPool(createRareBowPool());
        } else if (isEndGameLootTable(id)) {
            event.getTable().addPool(createEpicBowPool());
        }
    }

    private static boolean isEasyLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.SIMPLE_DUNGEON)
                || id.equals(BuiltInLootTables.ABANDONED_MINESHAFT);
    }

    private static boolean isMediumLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.JUNGLE_TEMPLE)
                || id.equals(BuiltInLootTables.PILLAGER_OUTPOST);
    }

    private static boolean isHardLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR)
                || id.equals(BuiltInLootTables.NETHER_BRIDGE);
    }

    private static boolean isEndGameLootTable(ResourceLocation id) {
        return id.equals(BuiltInLootTables.END_CITY_TREASURE)
                || id.equals(BuiltInLootTables.BASTION_TREASURE);
    }

    private static LootPool createCommonBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1)) // Always 1 roll
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/common_bows")))
                .build();
    }

    private static LootPool createUncommonBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/uncommon_bows")))
                .build();
    }

    private static LootPool createRareBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/rare_bows")))
                .build();
    }

    private static LootPool createEpicBowPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(new ResourceLocation("too_many_bows", "chests/epic_bows")))
                .build();
    }
}
