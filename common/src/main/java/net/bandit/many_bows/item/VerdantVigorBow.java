package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VerdantVigorBow extends BowItem {

    private static final int HEALTH_BOOST_DURATION = 200; // 10 seconds (ticks)
    private static final int REGENERATION_DURATION = 40; // 2 seconds (ticks)
    private static final int HEALTH_BOOST_LEVEL = 1; // +2 hearts
    private static final int REGEN_COOLDOWN = 100; // 5 seconds (ticks)

    public VerdantVigorBow(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player && !level.isClientSide) {
            if (selected) {
                // Apply constant health boost with hidden icon while holding the bow
                MobEffectInstance currentEffect = player.getEffect(MobEffects.HEALTH_BOOST);
                if (currentEffect == null || currentEffect.getAmplifier() != HEALTH_BOOST_LEVEL || currentEffect.getDuration() < Integer.MAX_VALUE) {
                    player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, Integer.MAX_VALUE, HEALTH_BOOST_LEVEL, true, false));

                    // Ensure the player's health doesn't drop below the new max health
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
                }

                // Periodically heal the player to match the extra hearts
                if (player.getHealth() < player.getMaxHealth() && level.getGameTime() % 20 == 0) {
                    player.heal(1.0F); // Heal 0.5 hearts per second
                }

                // Provide regeneration to nearby allies
                if (level.getGameTime() % 40 == 0) { // Every 2 seconds
                    AABB area = new AABB(player.getX() - 5, player.getY() - 5, player.getZ() - 5,
                            player.getX() + 5, player.getY() + 5, player.getZ() + 5);
                    level.getEntities(player, area, e -> e instanceof LivingEntity && e.isAlive())
                            .forEach(entityNearby -> {
                                if (entityNearby instanceof LivingEntity livingEntity) {
                                    livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_DURATION, 0));
                                }
                            });
                }
            } else {
                // Remove the health boost effect when not holding the bow
                if (player.hasEffect(MobEffects.HEALTH_BOOST)) {
                    player.removeEffect(MobEffects.HEALTH_BOOST);

                    // Adjust the player's health to avoid invalid states
                    if (player.getHealth() > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeCharged;

            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            if (charge >= 20 && (hasInfinity || consumeArrows(player, 1))) {
                // Use Arrow class for the projectile
                Arrow arrow = new Arrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);

                // Custom behavior for Infinity enchantment
                if (hasInfinity) {
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }

                level.addFreshEntity(arrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FLOWERING_AZALEA_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);

                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private boolean consumeArrows(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int arrowsRemoved = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                int removeAmount = Math.min(stack.getCount(), count - arrowsRemoved);
                stack.shrink(removeAmount);
                arrowsRemoved += removeAmount;
                if (arrowsRemoved >= count) {
                    return true;
                }
            }
        }
        return false;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.many_bows.verdant_vigor.tooltip").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.many_bows.verdant_vigor.tooltip.ability").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.many_bows.verdant_vigor.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
