package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.FrostbiteArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class FrostbiteBow extends ModBowItem {

    private boolean hasPlayedPullSound = false;

    public FrostbiteBow(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (livingEntity instanceof Player player) {
            applyFrostWalkerEffect(player, level);

            if (level.isClientSide()) {
                if (player.getRandom().nextFloat() < 0.2F) {
                    createPullingSnowParticles(level, livingEntity);
                }
                if (!hasPlayedPullSound) {
                    level.playSound(null,
                            livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                            SoundEvents.SNOW_BREAK, SoundSource.PLAYERS,
                            0.5F, 1.0F
                    );
                    hasPlayedPullSound = true;
                }
            }
        }

        super.onUseTick(level, livingEntity, stack, count);
    }

    private void applyFrostWalkerEffect(Player player, Level level) {
        BlockPos playerPos = player.blockPosition().below();

        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-1, 0, -1), playerPos.offset(1, 0, 1))) {
            BlockState state = level.getBlockState(pos);

            if (state.is(Blocks.WATER) && level.getBlockState(pos.above()).isAir()) {
                level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                level.scheduleTick(pos, Blocks.FROSTED_ICE, 60);
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
       try { if (!(entity instanceof Player player)) return false;

        ItemStack ammoInInv = player.getProjectile(bowStack);
        if (ammoInInv.isEmpty() && !player.hasInfiniteMaterials()) {
            hasPlayedPullSound = false;
            return false;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        float power = getPowerForTime(scaledCharge);
        if (power < 0.1F) {
            hasPlayedPullSound = false;
            return false;
        }

        List<ItemStack> projectiles = ProjectileWeaponItem.draw(bowStack, ammoInInv, player);
        if (projectiles.isEmpty()) {
            hasPlayedPullSound = false;
            return false;
        }

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
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS,
                1.0F, 1.2F
        );

        player.awardStat(Stats.ITEM_USED.get(this));
        hasPlayedPullSound = false;
        return true;
    }finally {
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
            arrow = new FrostbiteArrow(level, shooter, weaponStack, ammoStack);

            if (shooter instanceof Player player) {
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
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter,
                shooter.getXRot(),
                shooter.getYRot() + angle,
                0.0F,
                velocity,
                inaccuracy
        );
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
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

        hasPlayedPullSound = false;
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
            tooltip.accept(Component.translatable("item.many_bows.frostbite_bow.tooltip")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.translatable("item.many_bows.frostbite_bow.tooltip.ability")
                    .withStyle(ChatFormatting.DARK_AQUA));
            tooltip.accept(Component.translatable("item.many_bows.frostbite_bow.tooltip.frostwalker")
                    .withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }

    private void createPullingSnowParticles(Level level, LivingEntity entity) {
        double offsetX = entity.getRandom().nextGaussian() * 0.2D;
        double offsetY = entity.getRandom().nextGaussian() * 0.2D + 0.3D;
        double offsetZ = entity.getRandom().nextGaussian() * 0.2D;
        level.addParticle(ParticleTypes.SNOWFLAKE,
                entity.getX() + offsetX,
                entity.getY() + entity.getBbHeight() / 1.5 + offsetY,
                entity.getZ() + offsetZ,
                0.0D, 0.0D, 0.0D
        );
    }
}
