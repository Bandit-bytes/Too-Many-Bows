package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.VitalityArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class VerdantVigorBow extends ModBowItem {

    private static final int HEALTH_BOOST_LEVEL = 1;
    private static final int REGENERATION_DURATION = 40;
    private static final int HEALTH_BOOST_DURATION = 100;
    private static final int BUFFER_DURATION = 100;
    private static final int HEALTH_BOOST_HEARTS = 4;
    private static final UUID BONUS_HEALTH_UUID = UUID.fromString("d4c9e510-70a4-4a27-b4e1-7f54e882e58a");
    private int bufferTimer = 0;

    public VerdantVigorBow(Properties properties) {
        super(properties);
    }
//    @Override
//    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
//        if (entity instanceof Player player && !world.isClientSide) {
//            boolean isHoldingBow = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
//            var attribute = Attributes.MAX_HEALTH;
//
//            ResourceLocation BONUS_HEALTH_ID = ResourceLocation.fromNamespaceAndPath("many_bows", "verdant_vigor_bonus_health");
//
//            AttributeInstance attr = player.getAttribute(attribute);
//            if (attr == null) return;
//
//            if (isHoldingBow) {
//                if (attr.getModifier(BONUS_HEALTH_ID) == null) {
//                    attr.addPermanentModifier(new AttributeModifier(
//                            BONUS_HEALTH_ID,
//                            HEALTH_BOOST_HEARTS * 2.0,
//                            AttributeModifier.Operation.ADD_VALUE
//                    ));
//                }
//            } else {
//                if (attr.getModifier(BONUS_HEALTH_ID) != null) {
//                    attr.removeModifier(BONUS_HEALTH_ID);
//                }
//            }
@Override
public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
    if (entity instanceof Player player && !world.isClientSide) {
        boolean isHoldingBow = player.getMainHandItem() == stack || player.getOffhandItem() == stack;

        if (isHoldingBow) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.HEALTH_BOOST,
                    15,
                    1,
                    true,
                    false,
                    false
            ));
        }

            if (isHoldingBow && world.getGameTime() % 40 == 0) {
                AABB area = new AABB(
                        player.getX() - 5, player.getY() - 5, player.getZ() - 5,
                        player.getX() + 5, player.getY() + 5, player.getZ() + 5
                );

                world.getEntities(player, area, e ->
                        e instanceof LivingEntity &&
                                e.isAlive() &&
                                e != player &&
                                (e.getType().getCategory().isFriendly() || e instanceof Player)
                ).forEach(entityNearby -> {
                    ((LivingEntity) entityNearby).addEffect(new MobEffectInstance(
                            MobEffects.REGENERATION,
                            REGENERATION_DURATION,
                            0,
                            true, false, false
                    ));
                });
            }
        }
    }


    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            ItemStack arrowStack = player.getProjectile(bowStack);

            if (!arrowStack.isEmpty() || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                if (power >= 0.1F) {
                    List<ItemStack> projectiles = draw(bowStack, arrowStack, player);
                    boolean arrowConsumed = false;

                    if (!projectiles.isEmpty() && level instanceof ServerLevel serverLevel) {
                        for (ItemStack projectileStack : projectiles) {
                            AbstractArrow arrow;
                            if (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW)) {
                                arrow = ((ArrowItem) projectileStack.getItem()).createArrow(serverLevel, projectileStack, player, bowStack);
                            } else {
                                arrow = new VitalityArrow(serverLevel, player, bowStack, projectileStack);
                                if (arrow instanceof VitalityArrow vitalityArrow) {
                                    Holder<Attribute> rangedDamageAttr = level.registryAccess()
                                            .registryOrThrow(Registries.ATTRIBUTE)
                                            .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                                            .orElse(null);

                                    if (rangedDamageAttr != null) {
                                        AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                                        if (attrInstance != null) {
                                            float damage = (float) attrInstance.getValue();
                                            vitalityArrow.setBaseDamage(damage / 2.25);
                                        }
                                    }
                                }
                            }

                            applyPowerEnchantment(arrow, bowStack, level);
                            applyKnockbackEnchantment(arrow, bowStack, player, level);
                            applyFlameEnchantment(arrow, bowStack, level);
                            applyBowDamageAttribute(arrow, player);
                            tryApplyBowCrit(arrow, player, 1.5D);
                            if (hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild) {
                                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            } else {
                                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                            }
                            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
                            serverLevel.addFreshEntity(arrow);
                            if (!hasInfinityEnchantment(bowStack, level) && !player.getAbilities().instabuild && !arrowConsumed) {
                                projectileStack.shrink(1);
                                arrowConsumed = true;
                            }
                        }
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        damageBow(bowStack, player, InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }
    private void applyFlameEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> flame = getEnchantmentHolder(level, Enchantments.FLAME);
        int flameLevel = EnchantmentHelper.getItemEnchantmentLevel(flame, bow);
        if (flameLevel > 0) {
            arrow.igniteForSeconds(5);
        }
    }
    private void applyKnockbackEnchantment(AbstractArrow arrow, ItemStack bow, LivingEntity shooter, Level level) {
        Holder<Enchantment> punch = getEnchantmentHolder(level, Enchantments.PUNCH);
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(punch, bow);
        if (punchLevel > 0) {
            double resistance = Math.max(0.0, 1.0 - shooter.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
            net.minecraft.world.phys.Vec3 knockbackVec = arrow.getDeltaMovement().normalize().scale(punchLevel * 0.6 * resistance);
            arrow.push(knockbackVec.x, 0.1, knockbackVec.z);
        }
    }
    private void applyPowerEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> power = getEnchantmentHolder(level, Enchantments.POWER);
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(power, bow);
        if (powerLevel > 0) {
            double extraDamage = 0.5 * powerLevel + 1.0;
            arrow.setBaseDamage(arrow.getBaseDamage() + extraDamage);
        }
    }
    private boolean hasInfinityEnchantment(ItemStack bow, Level level) {
        Holder<Enchantment> infinity = getEnchantmentHolder(level, Enchantments.INFINITY);
        return EnchantmentHelper.getItemEnchantmentLevel(infinity, bow) > 0;
    }
    private Holder<Enchantment> getEnchantmentHolder(Level level, ResourceKey<Enchantment> enchantmentKey) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(enchantmentKey);
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

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
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();
        if (!player.hasInfiniteMaterials() && !hasArrows) {
            return InteractionResultHolder.fail(bowStack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        }
    }
    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.verdant_vigor.tooltip").withStyle(ChatFormatting.GREEN));
            tooltipComponents.add(Component.translatable("item.many_bows.verdant_vigor.tooltip.ability").withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }
}
