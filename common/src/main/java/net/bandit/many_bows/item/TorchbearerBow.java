package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.LightOrbEntity;
import net.bandit.many_bows.entity.TorchbearerArrow;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TorchbearerBow extends ModBowItem {

    private static final double DEFAULT_CRIT_MULTIPLIER = 1.5D;

    public TorchbearerBow(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        boolean isHoldingBow = player.getMainHandItem() == stack || player.getOffhandItem() == stack;

        if (isHoldingBow) {
            boolean hasOrb = !level.getEntitiesOfClass(LightOrbEntity.class, player.getBoundingBox().inflate(3)).isEmpty();

            if (!hasOrb) {
                var orb = new LightOrbEntity(EntityRegistry.LIGHT_ORB.get(), level);
                orb.moveTo(player.getX(), player.getY() + 1.5, player.getZ());
                level.addFreshEntity(orb);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player) || level.isClientSide()) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge);
        if (power < 0.1F) return;

        ItemStack projectile = player.getProjectile(stack);
        boolean isCreative = player.getAbilities().instabuild;
        boolean hasInfinity = hasInfinity(stack, player);
        boolean canFireNoArrows = canFireWithoutArrows(stack, player);
        boolean hasArrows = !projectile.isEmpty() || isCreative || canFireNoArrows;

        if (!hasArrows) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(this));
            return;
        }

        if (projectile.isEmpty() && canFireNoArrows) {
            projectile = new ItemStack(Items.ARROW);
        }

        TorchbearerArrow arrow = new TorchbearerArrow(level, player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

        applyEnchantments(stack, arrow);
        applyBowDamageAttribute(arrow, player);
        tryApplyBowCrit(arrow, player, DEFAULT_CRIT_MULTIPLIER);

        if (!isCreative && !hasInfinity && !projectile.isEmpty()) {
            projectile.shrink(1);
            if (projectile.isEmpty()) {
                player.getInventory().removeItem(projectile);
            }
        }

        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

        InteractionHand hand = player.getUsedItemHand();
        damageBow(stack, player, hand);

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void applyEnchantments(ItemStack stack, TorchbearerArrow arrow) {
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.torchbearer_bow").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("items.too_many_bows.torchbearer_bow.tooltip").withStyle(ChatFormatting.GREEN));

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
