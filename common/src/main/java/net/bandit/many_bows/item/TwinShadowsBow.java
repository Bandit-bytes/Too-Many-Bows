package net.bandit.many_bows.item;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class TwinShadowsBow extends ModBowItem {

    private static final Identifier RANGED_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");

    public TwinShadowsBow(Properties properties) {
        super(properties);
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
                fireTwinArrows(serverLevel, player, bowStack, projectiles, power);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
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

    private void fireTwinArrows(ServerLevel serverLevel, Player player, ItemStack bowStack, List<ItemStack> projectileStacks, float power) {
        if (projectileStacks.isEmpty()) return;

        ItemStack projectileStack = projectileStacks.get(0);

        Item ammoItem = projectileStack.getItem();
        ArrowItem arrowItem = (ammoItem instanceof ArrowItem ai) ? ai : (ArrowItem) Items.ARROW;

        float rangedDamage = getRangedWeaponDamage(serverLevel, player);

        // ---------- Light Arrow ----------
        AbstractArrow lightArrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);
        lightArrow.setBaseDamage(rangedDamage / 3.0F);
        applyBowDamageAttribute(lightArrow, player);
        tryApplyBowCrit(lightArrow, player, 1.5D);

        // Prefer translatable if you want localization later; literal is fine too
        lightArrow.setCustomName(Component.literal("Light Arrow").withStyle(ChatFormatting.WHITE));
        lightArrow.addTag("light");
        lightArrow.pickup = AbstractArrow.Pickup.ALLOWED;
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
        serverLevel.addFreshEntity(lightArrow);

        // ---------- Dark Arrow ----------
        AbstractArrow darkArrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);
        darkArrow.setBaseDamage(rangedDamage / 2.0F);
        applyBowDamageAttribute(darkArrow, player);
        tryApplyBowCrit(darkArrow, player, 1.5D);

        darkArrow.setCustomName(Component.literal("Dark Arrow").withStyle(ChatFormatting.DARK_GRAY));
        darkArrow.addTag("dark");
        darkArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + 5.0F, 0.0F, power * 2.5F, 1.0F);
        serverLevel.addFreshEntity(darkArrow);

        // consume exactly one ammo (since you spawn 2 arrows from it)
        if (!player.hasInfiniteMaterials()) {
            projectileStack.shrink(1);
            if (projectileStack.isEmpty()) {
                projectileStacks.remove(0);
            }
        }
    }

    private static float getRangedWeaponDamage(Level level, LivingEntity shooter) {
        var lookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var holderOpt = lookup.get(RANGED_DAMAGE_ID);
        if (holderOpt.isEmpty()) return 6.0F;

        Holder<Attribute> holder = holderOpt.get();
        AttributeInstance inst = shooter.getAttribute(holder);
        if (inst == null) return 6.0F;

        return (float) inst.getValue();
    }

    // keep your existing power curve
    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
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
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.many_bows.twin_shadows.tooltip")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.translatable("item.many_bows.twin_shadows.tooltip.ability")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.many_bows.twin_shadows.tooltip.legend")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
