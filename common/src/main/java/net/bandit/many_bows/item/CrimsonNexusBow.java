package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class CrimsonNexusBow extends BowItem {

    public CrimsonNexusBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                // Play custom sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.5F);

                // Calculate health cost
                float healthCost = player.getHealth() <= 4.0F ? 0 : 2.0F; // Prevent killing the player
                player.hurt(player.damageSources().magic(), healthCost);

                // Fire energy arrow
                Arrow arrow = new Arrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                // Apply enchantments
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 1.5);
                }

                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    arrow.setKnockback(punchLevel);
                }

                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                    arrow.setSecondsOnFire(100);
                }

                // Special effects for full health
                if (player.getHealth() == player.getMaxHealth()) {
                    arrow.setCritArrow(true);
                    arrow.setSecondsOnFire(200);
                }

                level.addFreshEntity(arrow);

                // Damage the bow after firing
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player && selected && !level.isClientSide) {
            // Apply life drain aura
            if (level.getGameTime() % 20 == 0) {
                AABB area = new AABB(player.getX() - 10, player.getY() - 10, player.getZ() - 10,
                        player.getX() + 10, player.getY() + 10, player.getZ() + 10);
                level.getEntities(player, area, e -> e instanceof LivingEntity && e != player)
                        .forEach(target -> {
                            if (target instanceof LivingEntity livingEntity) {
                                livingEntity.hurt(player.damageSources().magic(), 1.0F);
                                player.heal(0.5F); // Heal player slightly
                            }
                        });
            }
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
        return repair.is(ItemRegistry.REPAIR_CRYSTAL.get());
    }
    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        // Allow the bow to function without requiring arrows
        return stack -> true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            // Detailed information when holding Shift
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.info").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.health_cost", "2.0").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            // Base message prompting to hold Shift
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }

}
