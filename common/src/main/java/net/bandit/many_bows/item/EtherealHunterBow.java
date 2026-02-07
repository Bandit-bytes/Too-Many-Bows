package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.EtherealArrow;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EtherealHunterBow extends ModBowItem {
    private static final int HUNGER_COST = 1;

    public EtherealHunterBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try {if (!(entity instanceof Player player)) return false;

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        float power = getPowerForTime(scaledCharge);
        if (power < 0.1F) return false;


        if (!player.hasInfiniteMaterials()) {
            if (!level.isClientSide()) {
                if (!consumeHungerServer(player)) return false;
            } else {

                if (player.getFoodData().getFoodLevel() < HUNGER_COST) return false;
            }
        }

        List<ItemStack> ammo = List.of(new ItemStack(Items.ARROW));

        if (level instanceof ServerLevel serverLevel) {
            this.shoot(
                    serverLevel,
                    player,
                    player.getUsedItemHand(),
                    bowStack,
                    ammo,
                    power * 2.5F,
                    1.0F,
                    power == 1.0F,
                    null
            );

            if (!player.hasInfiniteMaterials()) {
                damageBow(bowStack, player, player.getUsedItemHand());
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F + (power * 0.1F));

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }finally {
        if (level.isClientSide()) {
            this.manybows$resetPullVisual(bowStack);
        }
    }
}

    @Override
    protected Projectile createProjectile(Level level,
                                          LivingEntity shooter,
                                          ItemStack weaponStack,
                                          ItemStack ammoStack,
                                          boolean crit) {

        AbstractArrow arrow = new EtherealArrow(level, shooter, weaponStack, ammoStack);

        if (crit) arrow.setCritArrow(true);

        if (level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weaponStack, arrow, item -> {});
        }

        if (shooter instanceof Player player) {
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);
        }

        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter,
                                   Projectile projectile,
                                   int index,
                                   float velocity,
                                   float inaccuracy,
                                   float angle,
                                   @Nullable LivingEntity target) {

        projectile.shootFromRotation(
                shooter,
                shooter.getXRot(),
                shooter.getYRot() + angle,
                0.0F,
                velocity,
                inaccuracy
        );
    }

    private boolean consumeHungerServer(Player player) {
        int current = player.getFoodData().getFoodLevel();
        if (current >= HUNGER_COST) {
            player.getFoodData().setFoodLevel(current - HUNGER_COST);
            return true;
        }

        player.displayClientMessage(
                Component.translatable("item.many_bows.ethereal_hunter.no_hunger")
                        .withStyle(ChatFormatting.RED),
                true
        );
        return false;
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);

        if (player.hasInfiniteMaterials() || player.getFoodData().getFoodLevel() >= HUNGER_COST) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide()) {
            player.displayClientMessage(
                    Component.translatable("item.many_bows.ethereal_hunter.no_hunger")
                            .withStyle(ChatFormatting.RED),
                    true
            );
        }
        return InteractionResult.FAIL;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        // doesn’t require arrows
        return stack -> false;
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

        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("item.many_bows.ethereal_hunter.tooltip.info")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.accept(Component.translatable("item.many_bows.ethereal_hunter.tooltip.hunger", HUNGER_COST)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.many_bows.ethereal_hunter.tooltip.legend")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
