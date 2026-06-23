package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.ManyBowsMod;
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

    private static final String MOD_ID = ManyBowsMod.MOD_ID;

    private static final LootTier EASY = new LootTier(
            0.50F,
            List.of("minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft"),
            List.of(
                    mod("ancient_sage_bow"), mod("aethers_call"), mod("burnt_relic"),
                    mod("arcane_bow"), mod("sharpshot_ring"), mod("cyroheart_bow"),
                    mod("power_crystal"), mod("emerald_sage_bow"), mod("torchbearer"),
                    mod("demons_grasp"), mod("dead_eyes_pendant"), mod("fletchers_talisman")
            )
    );

    private static final LootTier MEDIUM = new LootTier(
            0.40F,
            List.of(
                    "minecraft:chests/jungle_temple", "minecraft:chests/pillager_outpost",
                    "minecraft:chests/abandoned_mineshaft", "minecraft:chests/simple_dungeon"
            ),
            List.of(
                    mod("arcane_bow"), mod("cyroheart_bow"), mod("power_crystal"),
                    mod("torchbearer"), mod("sharpshot_ring"), mod("emerald_sage_bow"),
                    mod("demons_grasp"), mod("stormbound_signet"), mod("dead_eyes_pendant"),
                    mod("fletchers_talisman")
            )
    );

    private static final LootTier HARD = new LootTier(
            0.30F,
            List.of(
                    "minecraft:chests/stronghold_corridor", "minecraft:chests/nether_bridge",
                    "minecraft:chests/bastion_treasure"
            ),
            List.of(
                    mod("sentinels_wrath"), mod("cursed_stone"), mod("solar_bow"),
                    mod("arc_heavens"), mod("stormbound_signet"), mod("scatter_bow"),
                    mod("sharpshot_ring"), mod("wind_glove"), mod("vitality_weaver"),
                    mod("spectral_whisper"), mod("webstring"), mod("dead_eyes_pendant"),
                    mod("fletchers_talisman")
            )
    );

    private static final LootTier ENDGAME = new LootTier(
            0.20F,
            List.of(
                    "minecraft:chests/end_city_treasure", "minecraft:chests/nether_bridge",
                    "minecraft:chests/bastion_treasure"
            ),
            List.of(
                    mod("flame_bow"), mod("dark_bow"), mod("dragons_breath"), mod("wind_bow"),
                    mod("stormbound_signet"), mod("wind_glove"), mod("sharpshot_ring"),
                    mod("shulker_blast"), mod("astral_bound"), mod("auroras_grace"),
                    mod("dead_eyes_pendant"), mod("fletchers_talisman")
            )
    );

    private ModLootModifiers() {
    }

    public static void registerLootModifiers() {
        List<ResolvedLootTier> tiers = List.of(
                EASY.resolve(), MEDIUM.resolve(), HARD.resolve(), ENDGAME.resolve()
        );

        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin) {
                return;
            }

            BowLootConfig config = ManyBowsConfigHolder.get();
            if (!Boolean.TRUE.equals(config.enableChestLoot)) {
                return;
            }

            float multiplier = config.lootChanceMultiplier;
            for (ResolvedLootTier tier : tiers) {
                if (!tier.tables().contains(key)) {
                    continue;
                }

                LootPool.Builder pool = createPool(tier.baseChance() * multiplier, tier.itemIds());
                if (pool != null) {
                    context.addPool(pool);
                }
            }
        });
    }

    private static LootPool.Builder createPool(float chance, List<String> itemIds) {
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        if (chance <= 0.0F || itemIds.isEmpty()) {
            return null;
        }

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        boolean addedAny = false;
        for (String id : itemIds) {
            Identifier identifier = Identifier.tryParse(id);
            if (identifier == null) {
                continue;
            }

            Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.get(identifier);
            if (item.isPresent()) {
                pool.add(LootItem.lootTableItem(item.get().value()));
                addedAny = true;
            }
        }
        return addedAny ? pool : null;
    }

    private static Set<ResourceKey<LootTable>> parseTables(List<String> ids) {
        Set<ResourceKey<LootTable>> tables = new HashSet<>();
        for (String id : ids) {
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null) {
                tables.add(ResourceKey.create(Registries.LOOT_TABLE, identifier));
            }
        }
        return Set.copyOf(tables);
    }

    private static String mod(String path) {
        return MOD_ID + ":" + path;
    }

    private record LootTier(float baseChance, List<String> tables, List<String> itemIds) {
        private ResolvedLootTier resolve() {
            return new ResolvedLootTier(baseChance, parseTables(tables), itemIds);
        }
    }

    private record ResolvedLootTier(
            float baseChance,
            Set<ResourceKey<LootTable>> tables,
            List<String> itemIds
    ) {
    }
}
