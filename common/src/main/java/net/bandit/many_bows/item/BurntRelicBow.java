package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.BurntRelicBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BurntRelicBow extends ModBowItem {

    public BurntRelicBow(Properties properties) {
        super(properties);
    }

    private static BurntRelicBowConfig config() {
        return BowJsonConfigHelper.getConfig("burnt_relic", BurntRelicBowConfig.class, BurntRelicBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        ItemStack arrowStack = player.getProjectile(stack);
        if (arrowStack.isEmpty() && !player.getAbilities().instabuild) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge);

        if (power >= 0.1F) {
            BurntRelicBowConfig config = config();
            AbstractArrow arrow;
            if (arrowStack.getItem() instanceof ArrowItem arrowItem && !arrowStack.is(Items.ARROW)) {
                // Spectral, tipped and modded arrows keep their own projectile.
                arrow = arrowItem.createArrow(level, arrowStack, player);
            } else {
                arrow = ((ArrowItem) Items.ARROW).createArrow(level, new ItemStack(Items.ARROW), player);
                arrow.setBaseDamage(resolveConfiguredDamage(player, config.base_damage,
                        config.use_ranged_damage_attribute, config.ranged_damage_attribute_divisor, config.ranged_damage_flat_bonus));
            }
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * config.arrow_velocity, config.arrow_inaccuracy);

            applyStandardEnchantments(arrow, stack, 1.5D);
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, config.crit_damage_multiplier);

            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

            level.addFreshEntity(arrow);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);

            damageBow(stack, player, player.getUsedItemHand());
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack s = new ItemStack(this);
        s.enchant(Enchantments.INFINITY_ARROWS, 1);
        return s;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.burnt_relic.tooltip").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.too_many_bows.burnt_relic.tooltip1").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("item.too_many_bows.burnt_relic.tooltip2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
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
