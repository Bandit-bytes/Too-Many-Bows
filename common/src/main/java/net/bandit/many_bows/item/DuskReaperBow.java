package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.DuskReaperArrow;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DuskReaperBow extends ModBowItem {

    public DuskReaperBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try {
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

        if (!consumeSoulFragment(player, 1)) {
            if (level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.many_bows.dusk_reaper.no_soul_fragments")
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
            return false;
        }

        List<ItemStack> projectiles = ProjectileWeaponItem.draw(bowStack, ammoInInv, player);
        if (projectiles.isEmpty()) return false;

        if (level instanceof ServerLevel serverLevel) {
            this.shoot(
                    serverLevel,
                    player,
                    player.getUsedItemHand(),
                    bowStack,
                    projectiles,
                    power * 3.0F,
                    1.0F,
                    power == 1.0F,
                    null
            );
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS,
                0.3F, 0.5F + (power * 0.1F));

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

        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem
                && (ammoStack.is(Items.SPECTRAL_ARROW) || ammoStack.is(Items.TIPPED_ARROW))) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {
            arrow = new DuskReaperArrow(level, shooter, weaponStack, ammoStack);
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

    @Override
    protected void shootProjectile(LivingEntity shooter,
                                   Projectile projectile,
                                   int index,
                                   float velocity,
                                   float inaccuracy,
                                   float angle,
                                   @Nullable LivingEntity target) {

        if (projectile instanceof DuskReaperArrow dusk) {
            float power = Math.min(1.0F, velocity / 3.0F);
            dusk.setPowerMultiplier(power);
        }

        projectile.shootFromRotation(
                shooter,
                shooter.getXRot(),
                shooter.getYRot() + angle,
                0.0F,
                velocity,
                inaccuracy
        );
    }

    private boolean consumeSoulFragment(Player player, int count) {
        if (player.hasInfiniteMaterials()) return true;

        int remaining = count;
        var inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.is(ItemRegistry.SOUL_FRAGMENT.get())) {
                int remove = Math.min(stack.getCount(), remaining);

                stack.shrink(remove);
                remaining -= remove;

                inventory.setChanged();

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public static float getPowerForTime(int i) {
        float f = (float) i / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
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
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.info")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.soul_fragments")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.mark")
                    .withStyle(ChatFormatting.RED));
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.spectral")
                    .withStyle(ChatFormatting.DARK_AQUA));
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.effect")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.debuff")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.accept(Component.translatable("item.many_bows.dusk_reaper.tooltip.glow")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
