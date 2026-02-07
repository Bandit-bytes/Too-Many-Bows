package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.WebstringArrow;
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

public class WebstringVolleyBow extends ModBowItem {

    private static final int ARROWS_TO_FIRE = 5;

    private static final Identifier RANGED_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");

    public WebstringVolleyBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try {
            if (!(entity instanceof Player player)) return false;

            ItemStack ammoInInv = player.getProjectile(bowStack);
            boolean hasAmmo = !ammoInInv.isEmpty();

            boolean hasInfinity = hasInfinityEnchantmentLikeVanilla(bowStack, level);
            if (!hasAmmo && !player.hasInfiniteMaterials() && !hasInfinity) return false;

            int charge = this.getUseDuration(bowStack, entity) - chargeTime;

            float mult = this.manybows$getChargeMultiplier(bowStack, entity);
            int scaledCharge = Math.max(0, (int) (charge * mult));

            float power = getPowerForTime(scaledCharge);
            if (power < 0.1F) return false;
            List<ItemStack> projectiles = hasAmmo
                    ? ProjectileWeaponItem.draw(bowStack, ammoInInv, player)
                    : List.of(new ItemStack(Items.ARROW));

            if (projectiles.isEmpty()) return false;

            if (level instanceof ServerLevel serverLevel) {
                fireVolley(serverLevel, player, bowStack, projectiles, power);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

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

    private void fireVolley(ServerLevel level, Player player, ItemStack bowStack, List<ItemStack> projectileStacks, float power) {
        if (projectileStacks.isEmpty()) return;

        float basePitch = player.getXRot();
        float baseYaw = player.getYRot();
        float spreadAngle = 8.0F;

        ItemStack projectileStack = projectileStacks.get(0);

        boolean hasInfinity = hasInfinityEnchantmentLikeVanilla(bowStack, level);

        for (int i = -2; i <= 2; i++) {

            WebstringArrow arrow = new WebstringArrow(level, player, bowStack, projectileStack);
            float ranged = getRangedWeaponDamage(level, player);
            arrow.setBaseDamage(ranged / 6.0F);

            EnchantmentHelper.onProjectileSpawned(level, bowStack, arrow, (item) -> {});

            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);

            float spreadOffset = i * spreadAngle;
            arrow.shootFromRotation(player, basePitch, baseYaw + spreadOffset, 0.0F, power * 2.5F, 1.0F);

            // pickup behavior
            if (hasInfinity || i == 0) {
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            } else {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(arrow);
        }

        if (!player.hasInfiniteMaterials() && !hasInfinity && !projectileStack.isEmpty()) {
            projectileStack.shrink(1);
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


    private static boolean hasInfinityEnchantmentLikeVanilla(ItemStack bowStack, Level level) {
        return EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.INFINITY), bowStack) > 0;
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
        boolean hasInfinity = hasInfinityEnchantmentLikeVanilla(bowStack, level);

        if (!hasArrows && !hasInfinity && !player.hasInfiniteMaterials()) {
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
            tooltip.accept(Component.translatable("item.too_many_bows.webstring_volley.details")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.webstring_volley")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.too_many_bows.webstring_volley.tooltip")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
