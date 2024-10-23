package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
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

            if (power >= 0.1F) {
                LightningArrow arrow = new LightningArrow(level, player);

                // Adjust velocity by increasing the multiplier (4.0F instead of 3.0F)
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 4.0F, 1.0F);

                // Set custom base damage (e.g., increase base damage by 2.0)
                arrow.setBaseDamage(arrow.getBaseDamage() + 2.0); // Increase base damage by 2

                level.addFreshEntity(arrow);

                // Play the arrow shooting sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.arc_heavens.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.arc_heavens.tooltip.ability").withStyle(ChatFormatting.YELLOW));
    }

    public static class LightningArrow extends AbstractArrow {
        public LightningArrow(Level level, LivingEntity shooter) {
            super(EntityType.ARROW, shooter, level);
        }

        @Override
        protected void onHitEntity(EntityHitResult result) {
            super.onHitEntity(result);
            if (result.getEntity() instanceof LivingEntity target) {
                // Strike the target with lightning
                Level level = target.level();
                if (!level.isClientSide()) {
                    LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightningBolt != null) {
                        lightningBolt.moveTo(target.getX(), target.getY(), target.getZ());
                        level.addFreshEntity(lightningBolt);
                    }
                }
            }
        }

        @Override
        protected ItemStack getPickupItem() {
            return new ItemStack(Items.ARROW);
        }
    }
}
