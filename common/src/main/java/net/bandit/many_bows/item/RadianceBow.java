package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.RadiantArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class RadianceBow extends ModBowItem {

    private static final int EXPERIENCE_COST = 5;
    private static final double CRIT_MULTIPLIER = 1.5D;

    public RadianceBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge);

        if (!level.isClientSide) {
            if (power >= 0.1F && consumeExperience(player)) {
                RadiantArrow arrow = new RadiantArrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                arrow.setCritArrow(charge >= 20);

                applyEnchantments(stack, arrow);

                applyBowDamageAttribute(arrow, player);
                tryApplyBowCrit(arrow, player, CRIT_MULTIPLIER);

                level.addFreshEntity(arrow);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.5F);

                damageBow(stack, player, player.getUsedItemHand());
                player.awardStat(Stats.ITEM_USED.get(this));
            } else {
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private boolean consumeExperience(Player player) {
        int totalExperience = player.totalExperience;
        if (totalExperience >= EXPERIENCE_COST) {
            player.giveExperiencePoints(-EXPERIENCE_COST);
            return true;
        }
        player.displayClientMessage(
                Component.translatable("item.many_bows.radiance.no_experience").withStyle(ChatFormatting.RED),
                true
        );
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getAbilities().instabuild || player.getProjectile(stack).isEmpty() || this.getAllSupportedProjectiles().test(player.getProjectile(stack))) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    private void applyEnchantments(ItemStack stack, RadiantArrow arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5D) + 1.5D);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.radiance.tooltip.info").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.many_bows.radiance.tooltip.xp", EXPERIENCE_COST).withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.many_bows.radiance.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }
}
