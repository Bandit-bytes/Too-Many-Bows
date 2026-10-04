package net.bandit.many_bows.relic;

import dev.architectury.event.events.common.LootEvent;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class RelicQuests {
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("too_many_bows", "relic_bosses"));
    public static void init() {
        RelicConfig.get();
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin) return;
            String table = key.location().toString();
            Item item = switch(table) {
                case "minecraft:chests/end_city_treasure" -> ItemRegistry.CELESTIAL_LIMB.get();
                case "minecraft:chests/ancient_city" -> ItemRegistry.CELESTIAL_STRING.get();
                case "minecraft:chests/stronghold_library" -> ItemRegistry.CELESTIAL_GRIP.get();
                default -> null;
            };
            if (item != null) context.addPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(Math.max(0, Math.min(1, RelicConfig.get().fragmentChance)))).add(LootItem.lootTableItem(item)));
            if (table.equals("minecraft:chests/ancient_city") || table.equals("minecraft:chests/trial_chambers/reward")) context.addPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(Math.max(0, Math.min(1, RelicConfig.get().bluntedEdgeChance)))).add(LootItem.lootTableItem(ItemRegistry.BLUNTED_EDGE.get())));
        });
    }

    public static void award(ServerPlayer player, String id) {
        if (!id.equals("root")) award(player, "root");
        var advancement = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("too_many_bows", "relic/" + id));
        if (advancement != null) {
            var progress = player.getAdvancements().getOrStartProgress(advancement);
            for (String criterion : progress.getRemainingCriteria()) player.getAdvancements().award(advancement, criterion); }
    }

    public static void give(Player player, ItemStack stack) { if (!player.getInventory().add(stack)) player.drop(stack, false); }
    public static void trialMiss(ItemStack bow, int shot) {
        if (bow.isEmpty()) return;
        if (RelicData.read(bow).getInt("Trials") == 1) RelicData.edit(bow, t -> {
            if (shot >= t.getInt("LastTrialKill")) { t.putInt("Streak", 0); t.putInt("LastTrialKill", shot); } }
        );
    }

    public static void trialKill(ServerPlayer player, ItemStack bow, LivingEntity target, Vec3 origin, boolean airborne, int shot) {
        if (!(target instanceof Enemy)) { trialMiss(bow, shot); return;
        }
        var data = RelicData.read(bow);
        int stage = data.getInt("Trials");
        boolean success = false;
        if (stage == 0) success = origin.distanceTo(target.position()) >= 100;
        else if (stage == 1) {
            if (shot <= data.getInt("LastTrialKill")) return;
            int count = shot == data.getInt("LastTrialKill") + 1 ? data.getInt("Streak") + 1 : 1;
            boolean duplicate = false;
            for (int i = 0; i < 3; i++) if (data.hasUUID("TrialVictim" + i) && target.getUUID().equals(data.getUUID("TrialVictim" + i))) duplicate = true;
            if (duplicate) count = 0;
            final int streak = count;
            RelicData.edit(bow, t -> { t.putInt("Streak", streak); t.putInt("LastTrialKill", shot); if (streak > 0) t.putUUID("TrialVictim" + (streak - 1), target.getUUID()); });
            success = count >= 3;
        }
        else if (stage == 2) success = airborne && !player.onGround() && !target.onGround();
        if (!success) return;
        int completed = stage + 1;
        RelicData.edit(bow, t -> { t.putInt("Trials", completed); t.putInt("Streak", 0); t.putInt("LastTrialKill", shot); });
        award(player, "trial_" + completed);
        player.displayClientMessage(Component.literal("The Blunted Edge: trial " + completed + "/3 complete").withStyle(ChatFormatting.GOLD), false);
        if (completed == 3) { bow.shrink(1); give(player,
                new ItemStack(ItemRegistry.GODSPLITTER.get())); award(player, "godsplitter");
                ((ServerLevel)player.level()).sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 80, 1, 1, 1, 0.15); }
    }

    public static void playerTick(ServerPlayer player) {
        if (player.tickCount % 10 != 0 || player.level().dimension() != Level.OVERWORLD)
            return;

        for (ItemStack bow : player.getInventory().items) {
            if (!bow.is(ItemRegistry.DORMANT_CELESTIAL_BOW.get())) continue;
            var data = RelicData.read(bow); if (!data.getBoolean("StarCalled")) continue;

            BlockPos pos = new BlockPos(data.getInt("StarX"), data.getInt("StarY"), data.getInt("StarZ"));

            if (player.distanceToSqr(Vec3.atCenterOf(pos)) < 128 * 128) {
                ServerLevel level = (ServerLevel)player.level();
                level.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 8, 0.3, 1, 0.3, 0.04);
                for (int y = 4; y < 28; y += 4) level.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + y, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            }
        }
    }
}
