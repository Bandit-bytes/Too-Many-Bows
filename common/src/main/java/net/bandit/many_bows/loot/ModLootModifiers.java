package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.core.registries.Registries;

import java.util.Set;

public class ModLootModifiers {

    private static final BowLootConfig CONFIG = BowLootConfig.loadConfig();

    private static ResourceKey<LootTable> createKey(String namespace, String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static final Set<ResourceKey<LootTable>> EASY_LOOT_TABLES = Set.of(
            createKey("minecraft", "chests/simple_dungeon"),
            createKey("minecraft", "chests/abandoned_mineshaft")
    );

    private static final Set<ResourceKey<LootTable>> MEDIUM_LOOT_TABLES = Set.of(
            createKey("minecraft", "chests/jungle_temple"),
            createKey("minecraft", "chests/pillager_outpost"),
            createKey("minecraft", "chests/abandoned_mineshaft"),
            createKey("minecraft", "chests/simple_dungeon")
    );

    private static final Set<ResourceKey<LootTable>> HARD_LOOT_TABLES = Set.of(
            createKey("minecraft", "chests/stronghold_corridor"),
            createKey("minecraft", "chests/nether_bridge"),
            createKey("minecraft", "chests/bastion_treasure")
    );

    private static final Set<ResourceKey<LootTable>> ENDGAME_LOOT_TABLES = Set.of(
            createKey("minecraft", "chests/end_city_treasure"),
            createKey("minecraft", "chests/nether_bridge"),
            createKey("minecraft", "chests/bastion_treasure")
    );

    public static void registerLootModifiers() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin) return;

            if (CONFIG.easyLootEnabled && EASY_LOOT_TABLES.contains(key)) {
                context.addPool(createCommonBowPool(CONFIG.easyLootDropChance));
            }
            if (CONFIG.mediumLootEnabled && MEDIUM_LOOT_TABLES.contains(key)) {
                context.addPool(createUncommonBowPool(CONFIG.mediumLootDropChance));
            }
            if (CONFIG.hardLootEnabled && HARD_LOOT_TABLES.contains(key)) {
                context.addPool(createRareBowPool(CONFIG.hardLootDropChance));
            }
            if (CONFIG.endgameLootEnabled && ENDGAME_LOOT_TABLES.contains(key)) {
                context.addPool(createEpicBowPool(CONFIG.endgameLootDropChance));
            }
        });
    }

    private static LootPool.Builder createCommonBowPool(float chance) {
        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(0.0F, 1.0F))
                .add(LootItem.lootTableItem(ItemRegistry.ANCIENT_SAGE_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.AETHERS_CALL.get()))
                .add(LootItem.lootTableItem(ItemRegistry.BURNT_RELIC.get()))
                .add(LootItem.lootTableItem(ItemRegistry.ARCANE_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SHARPSHOT_RING.get()))
                .add(LootItem.lootTableItem(ItemRegistry.CYROHEART_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.POWER_CRYSTAL.get()))
                .add(LootItem.lootTableItem(ItemRegistry.EMERALD_SAGE_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.TORCHBEARER.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DEMONS_GRASP.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DEAD_EYES_PENDANT.get()))
                .add(LootItem.lootTableItem(ItemRegistry.FLETCHERS_TALISMAN.get()))
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static LootPool.Builder createUncommonBowPool(float chance) {
        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(0.0F, 1.0F))
                .add(LootItem.lootTableItem(ItemRegistry.ARCANE_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.CYROHEART_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.POWER_CRYSTAL.get()))
                .add(LootItem.lootTableItem(ItemRegistry.TORCHBEARER.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SHARPSHOT_RING.get()))
                .add(LootItem.lootTableItem(ItemRegistry.EMERALD_SAGE_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DEMONS_GRASP.get()))
                .add(LootItem.lootTableItem(ItemRegistry.STORMBOUND_SIGNET.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DEAD_EYES_PENDANT.get()))
                .add(LootItem.lootTableItem(ItemRegistry.FLETCHERS_TALISMAN.get()))
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static LootPool.Builder createRareBowPool(float chance) {
        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(0.0F, 1.0F))
                .add(LootItem.lootTableItem(ItemRegistry.SENTINELS_WRATH.get()))
                .add(LootItem.lootTableItem(ItemRegistry.CURSED_STONE.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SOLAR_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.ARC_HEAVENS.get()))
                .add(LootItem.lootTableItem(ItemRegistry.STORMBOUND_SIGNET.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SCATTER_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SHARPSHOT_RING.get()))
                .add(LootItem.lootTableItem(ItemRegistry.WIND_GLOVE.get()))
                .add(LootItem.lootTableItem(ItemRegistry.VITALITY_WEAVER.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SPECTRAL_WHISPER.get()))
                .add(LootItem.lootTableItem(ItemRegistry.WEBSTRING.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DEAD_EYES_PENDANT.get()))
                .add(LootItem.lootTableItem(ItemRegistry.FLETCHERS_TALISMAN.get()))
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static LootPool.Builder createEpicBowPool(float chance) {
        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(0.0F, 1.0F))
                .add(LootItem.lootTableItem(ItemRegistry.FLAME_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DARK_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DRAGONS_BREATH.get()))
                .add(LootItem.lootTableItem(ItemRegistry.WIND_BOW.get()))
                .add(LootItem.lootTableItem(ItemRegistry.STORMBOUND_SIGNET.get()))
                .add(LootItem.lootTableItem(ItemRegistry.WIND_GLOVE.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SHARPSHOT_RING.get()))
                .add(LootItem.lootTableItem(ItemRegistry.SHULKER_BLAST.get()))
                .add(LootItem.lootTableItem(ItemRegistry.ASTRAL_BOUND.get()))
                .add(LootItem.lootTableItem(ItemRegistry.AURORAS_GRACE.get()))
                .add(LootItem.lootTableItem(ItemRegistry.DEAD_EYES_PENDANT.get()))
                .add(LootItem.lootTableItem(ItemRegistry.FLETCHERS_TALISMAN.get()))
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }
}
