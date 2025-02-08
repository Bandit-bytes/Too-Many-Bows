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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class AuroraBow extends BowItem {

    public AuroraBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                boolean hasInfinity = player.getAbilities().instabuild ||
                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

                if (!arrowStack.isEmpty() || hasInfinity) {
                    if (consumeRiftShard(player, 1)) {
                        fireRiftArrow(level, player, arrowStack, hasInfinity, stack, power);
                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    } else {
                        player.displayClientMessage(Component.translatable("item.many_bows.aurora_bow.no_rift_shard")
                                .withStyle(ChatFormatting.RED), true);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("item.many_bows.aurora_bow.no_arrows")
                            .withStyle(ChatFormatting.RED), true);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private void fireRiftArrow(Level level, Player player, ItemStack arrowStack, boolean hasInfinity, ItemStack bowStack, float power) {
        // Create and configure AuroraArrowEntity
        AuroraArrowEntity riftArrow = new AuroraArrowEntity(level, player);
        riftArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

        // Apply enchantments to the arrow
        applyEnchantments(bowStack, riftArrow);

        // Handle Infinity enchantment and arrow consumption
        if (hasInfinity) {
            riftArrow.pickup = AuroraArrowEntity.Pickup.CREATIVE_ONLY;
        } else {
            arrowStack.shrink(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeItem(arrowStack);
            }
        }

        // Add arrow entity to the world
        level.addFreshEntity(riftArrow);

        // Play sound and update stats
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void applyEnchantments(ItemStack stack, AuroraArrowEntity arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 1.0);
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
            return true; // Creative mode bypass
        }

        int shardsRemoved = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ItemRegistry.RIFT_SHARD.get()) {
                int removeAmount = Math.min(stack.getCount(), count - shardsRemoved);
                stack.shrink(removeAmount);
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
        return stack -> stack.getItem() instanceof ArrowItem;
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
