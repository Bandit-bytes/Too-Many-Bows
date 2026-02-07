package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.RadiantArrow;
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

import java.util.function.Predicate;

public class RadianceBow extends ModBowItem {

    private static final int EXPERIENCE_COST = 5; // XP

    public RadianceBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try {
            if (!(entity instanceof Player player)) return false;

            int charge = this.getUseDuration(bowStack, entity) - chargeTime;

            float mult = this.manybows$getChargeMultiplier(bowStack, entity);
            int scaledCharge = Math.max(0, (int) (charge * mult));

            float power = getPowerForTime(scaledCharge);
            if (power < 0.1F) return false;


            if (!level.isClientSide()) {
                boolean canShoot = player.hasInfiniteMaterials() || consumeExperienceServer(player);

                if (!canShoot) {

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.7F, 1.2F);
                    return false;
                }

                if (level instanceof ServerLevel serverLevel) {

                    Projectile proj = createProjectile(serverLevel, player, bowStack, ItemStack.EMPTY, power == 1.0F);

                    if (proj instanceof RadiantArrow radiantArrow) {
                        radiantArrow.setPowerMultiplier(power);
                    }

                    shootProjectile(player, proj, 0, power * 3.0F, 1.0F, 0.0F, null);

                    serverLevel.addFreshEntity(proj);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);

                if (!player.hasInfiniteMaterials()) {
                    damageBow(bowStack, player, InteractionHand.MAIN_HAND);
                }

                player.awardStat(Stats.ITEM_USED.get(this));
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
        AbstractArrow arrow = new RadiantArrow(level, shooter, weaponStack, new ItemStack(Items.ARROW));

        if (shooter instanceof Player player) {
            // Optional: base damage from ranged_weapon:damage attribute (div 2)
            Holder<Attribute> rangedDamageAttr = level.registryAccess()
                    .lookupOrThrow(Registries.ATTRIBUTE)
                    .get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"))
                    .orElse(null);

            if (rangedDamageAttr != null) {
                AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                if (attrInstance != null) {
                    float damage = (float) attrInstance.getValue();
                    arrow.setBaseDamage(damage / 2.0F);
                }
            }
        }

        if (crit) arrow.setCritArrow(true);

        if (level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weaponStack, arrow, (item) -> {});
        }

        if (shooter instanceof Player player) {
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);
        }

        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    private boolean consumeExperienceServer(Player player) {
        if (player.totalExperience >= EXPERIENCE_COST) {
            player.giveExperiencePoints(-EXPERIENCE_COST);
            return true;
        }

        player.displayClientMessage(
                Component.translatable("item.many_bows.radiance.no_experience").withStyle(ChatFormatting.RED),
                true
        );
        return false;
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) f = 1.0F;
        return f;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("item.many_bows.radiance.tooltip.info")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.many_bows.radiance.tooltip.xp", EXPERIENCE_COST)
                    .withStyle(ChatFormatting.RED));
        } else {
            tooltip.accept(Component.translatable("item.many_bows.radiance.tooltip.legend")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
