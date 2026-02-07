package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcaneBow extends ModBowItem {

    private static final float SPREAD_ANGLE = 5.0F;

    public ArcaneBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try{
            if (!(entity instanceof Player player)) return false;

        ItemStack ammoInInv = player.getProjectile(bowStack);
        if (ammoInInv.isEmpty() && !player.hasInfiniteMaterials()) {
            return false;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        float power = getPowerForTime(scaledCharge);
        if (power < 0.1F) return false;

        List<ItemStack> projectiles = ProjectileWeaponItem.draw(bowStack, ammoInInv, player);
        if (projectiles.isEmpty()) return false;

        if (level instanceof ServerLevel serverLevel) {
            ItemStack usedAmmo = projectiles.get(0);

            fireTriple(serverLevel, player, bowStack, usedAmmo, power);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F);

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }finally {
        if (level.isClientSide()) {
            this.manybows$resetPullVisual(bowStack);
        }
    }
}

    private void fireTriple(ServerLevel level, Player player, ItemStack bowStack, ItemStack ammoStack, float power) {
        for (int i = -1; i <= 1; i++) {
            boolean center = (i == 0);

            Projectile proj = this.createProjectile(level, player, bowStack, ammoStack, power == 1.0F);
            if (!(proj instanceof AbstractArrow arrow)) continue;

            EnchantmentHelper.onProjectileSpawned(level, bowStack, arrow, (item) -> {});


            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);

            arrow.pickup = center ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.CREATIVE_ONLY;


            float yaw = player.getYRot() + (i * SPREAD_ANGLE);
            arrow.shootFromRotation(player, player.getXRot(), yaw, 0.0F, power * 2.5F, 1.0F);

            level.addFreshEntity(arrow);
        }

        if (!player.hasInfiniteMaterials()) {
            damageBow(bowStack, player, player.getUsedItemHand());
        }
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {
            arrow = ((ArrowItem) Items.ARROW).createArrow(level, new ItemStack(Items.ARROW), shooter, weaponStack);
        }

        if (crit) arrow.setCritArrow(true);
        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();

        if (!player.hasInfiniteMaterials() && !hasArrows) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }


    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.accept(Component.translatable("item.many_bows.arcane_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.many_bows.arcane_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
