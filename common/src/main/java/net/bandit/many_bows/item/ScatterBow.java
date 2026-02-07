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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ScatterBow extends ModBowItem {

    private static final int PELLETS = 8;

    public ScatterBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try {
            if (!(entity instanceof Player player)) return false;

            ItemStack ammoInInv = player.getProjectile(bowStack);
            boolean infinite = player.hasInfiniteMaterials();

            // Needs at least 1 arrow unless infinite
            if (ammoInInv.isEmpty() && !infinite) return false;

            int charge = this.getUseDuration(bowStack, entity) - chargeTime;

            float mult = this.manybows$getChargeMultiplier(bowStack, entity);
            int scaledCharge = Math.max(0, (int) (charge * mult));

            float power = getPowerForTime(scaledCharge);
            if (power < 0.1F) return false;

            if (level instanceof ServerLevel serverLevel) {
                // consume exactly ONE arrow if not infinite
                ItemStack ammoToConsume = ammoInInv;
                if (!infinite) {
                    // If the selected stack is empty for some reason, re-fetch once
                    if (ammoToConsume.isEmpty()) ammoToConsume = player.getProjectile(bowStack);
                    if (ammoToConsume.isEmpty()) return false;
                }

                // compute a small base damage for each pellet
                float pelletBaseDamage = getPelletBaseDamage(serverLevel, player);

                for (int i = 0; i < PELLETS; i++) {
                    // Create a normal arrow projectile (not consuming extra ammo per pellet)
                    AbstractArrow arrow = createVanillaArrow(serverLevel, player, bowStack, ammoInInv, power == 1.0F);

                    // set pellet base damage baseline (then enchant + bow attrib + crit will modify)
                    arrow.setBaseDamage(pelletBaseDamage);

                    // ✅ enchant behavior like AethersCall
                    EnchantmentHelper.onProjectileSpawned(serverLevel, bowStack, arrow, (item) -> {});

                    // your attributes
                    applyBowDamageAttribute(arrow, player);
                    tryApplyBowCrit(arrow, player, 1.5D);

                    // Pellet pickup behavior (same as “normal bow”)
                    arrow.pickup = infinite ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;

                    // Spread per pellet
                    float yawOffset = (serverLevel.getRandom().nextFloat() - 0.5F) * 20.0F;
                    float pitchOffset = (serverLevel.getRandom().nextFloat() - 0.5F) * 10.0F;

                    // shoot (more punchy than default)
                    arrow.shootFromRotation(
                            player,
                            player.getXRot() + pitchOffset,
                            player.getYRot() + yawOffset,
                            0.0F,
                            power * 3.0F,
                            1.0F
                    );

                    serverLevel.addFreshEntity(arrow);
                }

                // consume ONE arrow per shot (not per pellet)
                if (!infinite) {
                    ammoToConsume.shrink(1);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.awardStat(Stats.ITEM_USED.get(this));

            if (!player.hasInfiniteMaterials()) {
                damageBow(bowStack, player, InteractionHand.MAIN_HAND);
            }

            return true;
        } finally {
            if (level.isClientSide()) {
                this.manybows$resetPullVisual(bowStack);
            }
        }
    }

    private static AbstractArrow createVanillaArrow(ServerLevel level, Player shooter, ItemStack bowStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem
                && (ammoStack.is(Items.SPECTRAL_ARROW) || ammoStack.is(Items.TIPPED_ARROW))) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, bowStack);
        } else {

            arrow = new Arrow(level, shooter, bowStack, ammoStack);
        }

        if (crit) arrow.setCritArrow(true);
        return arrow;
    }

    private static float getPelletBaseDamage(Level level, Player player) {
        float base = 2.0F;

        var lookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var holderOpt = lookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

        if (holderOpt.isPresent()) {
            Holder<Attribute> holder = holderOpt.get();
            AttributeInstance inst = player.getAttribute(holder);
            if (inst != null) {
                base = (float) inst.getValue() / 11.0F;
            }
        }

        return base;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int charge) {
        float f = (float) charge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
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
            tooltip.accept(Component.translatable("item.many_bows.scatter_bow.tooltip")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.many_bows.scatter_bow.tooltip.ability")
                    .withStyle(ChatFormatting.DARK_GREEN));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
