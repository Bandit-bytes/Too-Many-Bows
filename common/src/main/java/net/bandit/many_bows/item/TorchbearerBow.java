package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.TorchbearerArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TorchbearerBow extends BowItem {

    public TorchbearerBow(Properties properties) {
        super(properties);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player && !world.isClientSide) {
            boolean isHoldingBow = player.getMainHandItem() == stack || player.getOffhandItem() == stack;

            if (isHoldingBow) {
                BlockPos playerPos = player.blockPosition();
                if (world.isEmptyBlock(playerPos)) {
                    world.setBlockAndUpdate(playerPos, Blocks.LIGHT.defaultBlockState());
                }
            } else {
                BlockPos playerPos = player.blockPosition();
                if (world.getBlockState(playerPos).is(Blocks.LIGHT)) {
                    world.setBlockAndUpdate(playerPos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);
            if (power >= 0.1F) {
                ItemStack arrowStack = player.getProjectile(stack);
                boolean isCreative = player.getAbilities().instabuild;
                boolean hasInfinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                boolean hasArrows = !arrowStack.isEmpty() || isCreative || hasInfinity;

                if (hasArrows) {
                    if (arrowStack.isEmpty() && hasInfinity) {
                        arrowStack = new ItemStack(Items.ARROW);
                    }

                    TorchbearerArrow torchbearerArrow = new TorchbearerArrow(level, player);
                    torchbearerArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                    applyEnchantments(stack, torchbearerArrow);

                    if (!isCreative && !hasInfinity) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }

                    level.addFreshEntity(torchbearerArrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private void applyEnchantments(ItemStack stack, TorchbearerArrow torchbearerArrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            torchbearerArrow.setBaseDamage(torchbearerArrow.getBaseDamage() + (powerLevel * 0.5) + 1.0);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            torchbearerArrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            torchbearerArrow.setSecondsOnFire(100);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.torchbearer_bow").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.torchbearer_bow.tooltip").withStyle(ChatFormatting.GREEN));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.torchbearer_bow.shift").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.too_many_bows.torchbearer_bow.effect").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.GRAY));
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
}
