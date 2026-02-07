package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.WindProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WindBow extends ModBowItem {

    private static final int MOVEMENT_SPEED_DURATION = 5;
    private static final int MOVEMENT_SPEED_LEVEL = 0;

    private static final Identifier RANGED_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");

    public WindBow(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!level.isClientSide() && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED,
                    MOVEMENT_SPEED_DURATION * 20,
                    MOVEMENT_SPEED_LEVEL,
                    true,
                    false,
                    false
            ));
        }
        super.onUseTick(level, entity, stack, count);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try {
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
                    SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS,
                    1.0F, 1.0F);

            player.awardStat(Stats.ITEM_USED.get(this));

            if (!player.getAbilities().instabuild) {
                damageBow(bowStack, player, player.getUsedItemHand());
            }

            return true;
        } finally {
            if (level.isClientSide()) {
                this.manybows$resetPullVisual(bowStack);
            }
        }
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem
                && (ammoStack.is(Items.SPECTRAL_ARROW) || ammoStack.is(Items.TIPPED_ARROW))) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {
            arrow = new WindProjectile(level, shooter, weaponStack, ammoStack);
            float base = getRangedWeaponDamage(level, shooter);
            arrow.setBaseDamage(base / 2.25F);
        }

        if (crit) arrow.setCritArrow(true);
        if (level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weaponStack, arrow, (item) -> {});
        }

        if (shooter instanceof Player player) {
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);
        }

        return arrow;
    }

    private static float getRangedWeaponDamage(Level level, LivingEntity shooter) {
        var lookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var holderOpt = lookup.get(RANGED_DAMAGE_ID);
        if (holderOpt.isEmpty()) return 6.0F;

        Holder<Attribute> holder = holderOpt.get();
        var inst = shooter.getAttribute(holder);
        if (inst == null) return 6.0F;

        return (float) inst.getValue();
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
                                Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.too_many_bows.wind_bow.tooltip")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.translatable("item.too_many_bows.wind_bow.tooltip.ability")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.accept(Component.translatable("item.too_many_bows.wind_bow.tooltip.effect")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.too_many_bows.wind_bow.tooltip.legend")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
