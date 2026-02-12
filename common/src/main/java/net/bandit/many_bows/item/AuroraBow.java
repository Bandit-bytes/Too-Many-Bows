package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.AuroraArrowEntity;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class AuroraBow extends ModBowItem {

    private static final double CRIT_MULTIPLIER = 1.5D;

    public AuroraBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge);

        if (power < 0.1F) return;

        boolean hasInfinity = canFireWithoutArrows(stack, player);
        ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

        if (arrowStack.isEmpty() && !hasInfinity) {
            player.displayClientMessage(Component.translatable("items.many_bows.aurora_bow.no_arrows")
                    .withStyle(ChatFormatting.RED), true);
            player.awardStat(Stats.ITEM_USED.get(this));
            return;
        }

        if (!consumeRiftShard(player, 1)) {
            player.displayClientMessage(Component.translatable("item.many_bows.aurora_bow.no_rift_shard")
                    .withStyle(ChatFormatting.RED), true);
            player.awardStat(Stats.ITEM_USED.get(this));
            return;
        }

        fireRiftArrow(level, player, arrowStack, hasInfinity, stack, power);

        damageBow(stack, player, player.getUsedItemHand());

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void fireRiftArrow(Level level, Player player, ItemStack arrowStack, boolean hasInfinity, ItemStack bowStack, float power) {
        AuroraArrowEntity riftArrow = new AuroraArrowEntity(level, player);
        riftArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

        applyEnchantments(bowStack, riftArrow);

        applyBowDamageAttribute(riftArrow, player);
        tryApplyBowCrit(riftArrow, player, CRIT_MULTIPLIER);

        if (hasInfinity) {
            riftArrow.pickup = AuroraArrowEntity.Pickup.CREATIVE_ONLY;
        } else {
            if (!arrowStack.isEmpty()) {
                arrowStack.shrink(1);
                if (arrowStack.isEmpty()) {
                    player.getInventory().removeItem(arrowStack);
                }
            }
        }

        level.addFreshEntity(riftArrow);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }

    private void applyEnchantments(ItemStack stack, AuroraArrowEntity arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5D) + 1.0D);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

    private boolean consumeRiftShard(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int shardsRemoved = 0;
        for (ItemStack invStack : player.getInventory().items) {
            if (invStack.getItem() == ItemRegistry.RIFT_SHARD.get()) {
                int removeAmount = Math.min(invStack.getCount(), count - shardsRemoved);
                invStack.shrink(removeAmount);
                shardsRemoved += removeAmount;
                if (shardsRemoved >= count) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> s.getItem() instanceof ArrowItem;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.aurora_bow.tooltip.extended").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.many_bows.aurora_bow.tooltip").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
