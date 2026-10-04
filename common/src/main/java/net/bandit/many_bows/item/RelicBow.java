package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.RelicArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.relic.RelicConfig;
import net.bandit.many_bows.relic.RelicCombatEffects;
import net.bandit.many_bows.relic.RelicData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public class RelicBow extends ModBowItem {

    public enum Kind {
        EVENTIDE,
        WORLDEATER,
        GODSPLITTER,
        BLUNTED
    }

    public final Kind kind;

    private final Map<Player, AimState> aimingPlayers = new WeakHashMap<>();

    private static final class AimState {
        private final ItemStack bow;
        private Vec3 referenceAim;
        private int lastElapsed = -1;
        private int steadyTicks;

        private AimState(ItemStack bow, Vec3 referenceAim) {
            this.bow = bow;
            this.referenceAim = referenceAim;
        }
    }

    public RelicBow(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void onUseTick(
            Level level,
            LivingEntity entity,
            ItemStack stack,
            int remaining
    ) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel server)) return;
        int elapsed = getUseDuration(stack, entity) - remaining;
        if (kind == Kind.WORLDEATER) {
            double dominance = Math.min(1, RelicData.read(stack).getInt("Dominance")
                    / (double) Math.max(1, RelicConfig.get().maxDominance));
            RelicCombatEffects.worldCharge(server, player, elapsed, dominance);
            return;
        }
        if (kind != Kind.GODSPLITTER) return;
        Vec3 aim = entity.getLookAngle();
        AimState state = aimingPlayers.get(player);

        if (state == null
                || state.bow != stack
                || elapsed != state.lastElapsed + 1) {
            state = new AimState(stack, aim);
            aimingPlayers.put(player, state);
        }

        state.lastElapsed = elapsed;

        if (elapsed < 20 || aim.dot(state.referenceAim) <= 0.99985) {
            state.steadyTicks = 0;
            state.referenceAim = aim;
            RelicCombatEffects.precisionCharge(server, player, elapsed, 0);
            return;
        }

        state.steadyTicks++;
        RelicCombatEffects.precisionCharge(server, player, elapsed, state.steadyTicks);

        if (state.steadyTicks == 30) {
            player.displayClientMessage(
                    Component.literal("PERFECT SHOT")
                            .withStyle(ChatFormatting.AQUA),
                    true
            );

            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS,
                    1,
                    1.5f
            );
        }
    }

    @Override
    public void releaseUsing(
            ItemStack bow,
            Level level,
            LivingEntity entity,
            int remaining
    ) {
        if (!(entity instanceof Player player)
                || !(level instanceof ServerLevel server)) {
            return;
        }

        int elapsed = getUseDuration(bow, entity) - remaining;
        AimState aimState = aimingPlayers.remove(player);
        float power = BowItem.getPowerForTime(elapsed);

        if (power < 0.1f) {
            return;
        }

        ItemStack ammo = player.getProjectile(bow);

        if (ammo.isEmpty() && !player.getAbilities().instabuild) {
            return;
        }

        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }

        // Preserve vanilla ammo consumption and enchantment handling.
        List<ItemStack> shots = draw(bow, ammo, player);

        if (shots.isEmpty()) {
            return;
        }

        var tag = RelicData.read(bow);

        boolean perfect = kind == Kind.GODSPLITTER
                && aimState != null
                && aimState.bow == bow
                && elapsed >= 50
                && elapsed >= aimState.lastElapsed
                && elapsed - aimState.lastElapsed <= 1
                && aimState.steadyTicks >= 30
                && player.getLookAngle().dot(aimState.referenceAim) > 0.99985;

        if (kind == Kind.GODSPLITTER && !perfect) {
            RelicData.edit(bow, data -> {
                data.remove("SeverTarget");
                data.putInt("Severance", 0);
            });
        }

        boolean ready = power == 1
                && !player.getCooldowns().isOnCooldown(this);

        boolean empowered = ready
                && kind == Kind.WORLDEATER
                && tag.getInt("Dominance")
                >= Math.max(1, RelicConfig.get().maxDominance);

        if (kind == Kind.BLUNTED) {
            RelicData.edit(
                    bow,
                    data -> data.putInt(
                            "TrialShot",
                            data.getInt("TrialShot") + 1
                    )
            );
        }

        for (ItemStack shot : shots) {
            RelicArrow arrow = new RelicArrow(server, player, shot, bow);

            arrow.configure(
                    kind,
                    ready,
                    perfect,
                    empowered,
                    player.position(),
                    !player.onGround(),
                    RelicData.read(bow).getInt("TrialShot")
            );

            double damage = switch (kind) {
                case EVENTIDE -> RelicConfig.get().eventideDamage;
                case WORLDEATER -> RelicConfig.get().worldeaterDamage;
                case GODSPLITTER -> RelicConfig.get().godsplitterDamage;
                case BLUNTED -> 5;
            };

            float speed = kind == Kind.GODSPLITTER ? 6 : 3;

            arrow.setBaseDamage(damage / speed);

            var powerHolder = level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.POWER);

            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    powerHolder,
                    bow
            );

            arrow.setBaseDamage(
                    arrow.getBaseDamage() + powerLevel * 0.5
            );

            var flameHolder = level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.FLAME);

            if (EnchantmentHelper.getItemEnchantmentLevel(
                    flameHolder,
                    bow
            ) > 0) {
                arrow.igniteForSeconds(5);
            }

            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5);
            arrow.setCritArrow(power == 1);

            if (kind == Kind.GODSPLITTER) {
                arrow.setNoGravity(true);
                arrow.setRelicPiercing((byte) (perfect ? 5 : 0));
            }

            if (player.getAbilities().instabuild
                    || shot.has(DataComponents.INTANGIBLE_PROJECTILE)) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            arrow.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0,
                    speed * power,
                    kind == Kind.GODSPLITTER ? 0 : 0.5f
            );

            server.addFreshEntity(arrow);
        }

        if (ready && (kind == Kind.EVENTIDE || kind == Kind.WORLDEATER)) {
            player.getCooldowns().addCooldown(
                    this,
                    Math.max(1, RelicConfig.get().abilityCooldownTicks)
            );
        }

        if (empowered) {
            RelicData.edit(bow, data -> data.putInt("Dominance", 0));
        }

        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1,
                kind == Kind.GODSPLITTER ? 0.5f : 0.8f
        );

        damageBow(bow, player, player.getUsedItemHand());
    }

    @Override
    public boolean isValidRepairItem(ItemStack bow, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> lines,
            TooltipFlag flag
    ) {
        String translationPrefix =
                "relic.too_many_bows." + kind.name().toLowerCase(Locale.ROOT);

        lines.add(
                Component.translatable(translationPrefix + ".lore")
                        .withStyle(ChatFormatting.GRAY)
        );

        for (int i = 1; i <= 3; i++) {
            lines.add(
                    Component.translatable(
                            translationPrefix + ".ability" + i
                    ).withStyle(ChatFormatting.AQUA)
            );
        }

        var tag = RelicData.read(stack);

        if (kind == Kind.WORLDEATER) {
            lines.add(
                    Component.literal(
                            "Dominance: " + tag.getInt("Dominance")
                                    + "/" + RelicConfig.get().maxDominance
                    ).withStyle(ChatFormatting.GOLD)
            );
        }

        if (kind == Kind.BLUNTED) {
            lines.add(
                    Component.literal(
                            "Trials: " + tag.getInt("Trials") + "/3"
                    ).withStyle(ChatFormatting.GOLD)
            );
        }

        lines.add(
                Component.translatable(translationPrefix + ".obtain")
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}