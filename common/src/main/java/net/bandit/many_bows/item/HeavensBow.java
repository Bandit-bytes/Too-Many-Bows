package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeavensBow extends BowItem {

    public HeavensBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : findArrowInInventory(player);

            if (power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                LightningArrow arrow = new LightningArrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 4.0F, 1.0F);

                // Apply enchantment-based modifications
                arrow.setBaseDamage(arrow.getBaseDamage() + 2.0);
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5 + 0.5);
                }
                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    arrow.setKnockback(punchLevel);
                }
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                    arrow.setSecondsOnFire(100);
                }

                // Prevent pickup if Infinity is enabled
                arrow.pickup = hasInfinity ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED;

                level.addFreshEntity(arrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Consume arrow if not in Creative mode or with Infinity
                if (!hasInfinity) {
                    arrowStack.shrink(1);
                }
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static class LightningArrow extends AbstractArrow {
        private int delayTicks = 10;

        public LightningArrow(Level level, LivingEntity shooter) {
            super(EntityType.ARROW, shooter, level);
        }

        @Override
        protected void onHitEntity(EntityHitResult result) {
            super.onHitEntity(result);
            if (result.getEntity() instanceof LivingEntity target && !level().isClientSide()) {
                this.delayTicks = 10;
            }
        }

        @Override
        public void tick() {
            super.tick();

            if (delayTicks > 0) {
                delayTicks--;
            } else if (delayTicks == 0 && !this.level().isClientSide()) {
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level());
                if (lightningBolt != null) {
                    lightningBolt.moveTo(this.getX(), this.getY(), this.getZ());
                    level().addFreshEntity(lightningBolt);
                }
                delayTicks = -1;
            }
        }

        @Override
        protected ItemStack getPickupItem() {
            return new ItemStack(Items.ARROW);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.arc_heavens.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.arc_heavens.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
    }
}
