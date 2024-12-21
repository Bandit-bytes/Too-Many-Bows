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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
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
        if (entity instanceof Player player && !level.isClientSide) {
            int charge = this.getUseDuration(stack) - timeCharged;
            if (charge >= 20 && consumeRiftShard(player, 1)) { // Consume Rift Shard
                // Play custom sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLOW_ITEM_FRAME_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Fire Aurora Arrow
                fireRiftArrow(level, player, stack);

                // Damage the bow
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                player.displayClientMessage(Component.translatable("item.many_bows.aurora_bow.no_rift_shard").withStyle(ChatFormatting.RED), true);
            }
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

    private void fireRiftArrow(Level level, Player player, ItemStack stack) {
        AuroraArrowEntity riftArrow = new AuroraArrowEntity(level, player);
        riftArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);

        // Apply Power enchantment
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            riftArrow.setBaseDamage(riftArrow.getBaseDamage() + (double) powerLevel * 0.5 + 2.0);
        } else {
            riftArrow.setBaseDamage(riftArrow.getBaseDamage() + 2.0);
        }

        // Apply Punch enchantment (knockback)
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            riftArrow.setKnockback(punchLevel);
        }

        riftArrow.pickup = AuroraArrowEntity.Pickup.DISALLOWED;

        level.addFreshEntity(riftArrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true; // No arrows required
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

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.REPAIR_CRYSTAL.get());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
