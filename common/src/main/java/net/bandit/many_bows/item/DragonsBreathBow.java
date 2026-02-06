package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.DragonsBreathArrow;
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
import java.util.function.Predicate;

public class DragonsBreathBow extends ModBowItem {

    public DragonsBreathBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) return false;

        ItemStack ammoInInv = player.getProjectile(bowStack);
        if (ammoInInv.isEmpty() && !player.hasInfiniteMaterials()) return false;

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        float power = getPowerForTime(scaledCharge);
        if (power < 0.1F) return false;

        List<ItemStack> projectiles = ProjectileWeaponItem.draw(bowStack, ammoInInv, player);
        if (projectiles.isEmpty()) return false;

        if (level instanceof ServerLevel serverLevel) {
            this.shoot(
                    serverLevel,
                    player,
                    player.getUsedItemHand(),
                    bowStack,
                    projectiles,
                    power * 2.5F,
                    1.0F,
                    power == 1.0F,
                    null
            );
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.1F + (power * 0.25F));

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem
                && (ammoStack.is(Items.SPECTRAL_ARROW) || ammoStack.is(Items.TIPPED_ARROW))) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {

            arrow = new DragonsBreathArrow(level, shooter, weaponStack, ammoStack);
        }

        if (crit) arrow.setCritArrow(true);

        if (level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weaponStack, arrow, (item) -> {});
        }

        if (shooter instanceof Player player) {
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);
        }

        if (arrow instanceof DragonsBreathArrow db) {
            int useDuration = this.getUseDuration(weaponStack, shooter);
            int useLeft = shooter.getUseItemRemainingTicks();
            int used = useDuration - useLeft;

            float mult = this.manybows$getChargeMultiplier(weaponStack, shooter);
            int scaled = Math.max(0, (int) (used * mult));
            float power = getPowerForTime(scaled);

            db.setPowerMultiplier(power);
        }

        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int i) {
        float f = (float) i / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) f = 1.0F;
        return f;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();

        if (!player.hasInfiniteMaterials() && !hasArrows) return InteractionResult.FAIL;

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

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.many_bows.dragons_breath_bow.tooltip.ability")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.accept(Component.translatable("item.many_bows.dragons_breath_bow.tooltip")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
