package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.EtherealArrow;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class EtherealHunterBow extends ModBowItem {
    private static final int HUNGER_COST = 1;

    public EtherealHunterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge);

            if (power >= 0.1F && (consumeHunger(player) || player.getAbilities().instabuild)) {
                if (level instanceof ServerLevel serverLevel) {
                    boolean arrowConsumed = false;

                    ItemStack fakeArrow = new ItemStack(Items.ARROW);
                    List<ItemStack> projectiles = draw(bowStack, fakeArrow, player);

                    for (ItemStack projectileStack : projectiles) {
                        AbstractArrow arrow;

                        if (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW)) {
                            arrow = ((ArrowItem) projectileStack.getItem()).createArrow(serverLevel, projectileStack, player, bowStack);
                        }  else {
                            arrow = new EtherealArrow(serverLevel, player, bowStack, projectileStack);
                            if (arrow instanceof EtherealArrow etherealArrow) {
                                Holder<Attribute> rangedDamageAttr = level.registryAccess()
                                        .registryOrThrow(Registries.ATTRIBUTE)
                                        .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                                        .orElse(null);

                                if (rangedDamageAttr != null) {
                                    AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                                    if (attrInstance != null) {
                                        float damage = (float) attrInstance.getValue();
                                        etherealArrow.setBaseDamage(damage / 1.5);
                                    }
                                }
                            }
                        }

                        applyPowerEnchantment(arrow, bowStack, level);
                        applyKnockbackEnchantment(arrow, bowStack, player, level);
                        applyFlameEnchantment(arrow, bowStack, level);
                        applyBowDamageAttribute(arrow, player);
                        tryApplyBowCrit(arrow, player, 1.5D);

                        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);

                        serverLevel.addFreshEntity(arrow);

                        if (!player.getAbilities().instabuild && !arrowConsumed) {
                            projectileStack.shrink(1);
                            arrowConsumed = true;
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

    private boolean consumeHunger(Player player) {
        int currentHunger = player.getFoodData().getFoodLevel();
        if (currentHunger >= HUNGER_COST) {
            player.getFoodData().setFoodLevel(currentHunger - HUNGER_COST);
            return true;
        }

        player.displayClientMessage(Component.translatable("item.many_bows.ethereal_hunter.no_hunger")
                .withStyle(ChatFormatting.RED), true);
        return false;
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

    private Holder<Enchantment> getEnchantmentHolder(Level level, ResourceKey<Enchantment> enchantmentKey) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(enchantmentKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        if (consumeHunger(player) || player.getAbilities().instabuild) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        } else {
            return InteractionResultHolder.fail(bowStack);
        }
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
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> false; // No arrows required
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.info").withStyle(ChatFormatting.DARK_PURPLE));
            tooltipComponents.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.hunger", HUNGER_COST).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.legend").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }
}
