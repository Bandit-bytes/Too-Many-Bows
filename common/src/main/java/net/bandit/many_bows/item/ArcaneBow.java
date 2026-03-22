package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.ArcaneBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class ArcaneBow extends ModBowItem {

    private static final String CONFIG_NAME = "arcane_bow";

    public ArcaneBow(Properties properties) {
        super(properties);
    }

    private static ArcaneBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, ArcaneBowConfig.class, ArcaneBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) {
            return;
        }

        ArcaneBowConfig config = config();
        List<ItemStack> projectiles = draw(bowStack, player.getProjectile(bowStack), player);

        if (!projectiles.isEmpty() || player.getAbilities().instabuild) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge);

            if (power >= config.minimum_pull_power) {
                if (level instanceof ServerLevel serverLevel) {
                    fireConfiguredArrows(serverLevel, player, bowStack, projectiles, power, config);
                }

                if (config.shoot_sound_enabled) {
                    level.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ARROW_SHOOT,
                            SoundSource.PLAYERS,
                            config.shoot_sound_volume,
                            config.shoot_sound_pitch
                    );
                }

                player.awardStat(Stats.ITEM_USED.get(this));

                if (!player.getAbilities().instabuild && config.damage_bow_on_release) {
                    damageBow(bowStack, player, InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    private void fireConfiguredArrows(ServerLevel serverLevel, Player player, ItemStack bowStack, List<ItemStack> projectileStacks, float power, ArcaneBowConfig config) {
        if (projectileStacks.isEmpty() && !player.getAbilities().instabuild) {
            return;
        }

        ItemStack projectileStack = projectileStacks.isEmpty() ? new ItemStack(Items.ARROW) : projectileStacks.get(0);
        if (!(projectileStack.getItem() instanceof ArrowItem arrowItem)) {
            return;
        }

        int arrowCount = Math.max(1, config.arrow_count);
        int centerIndex = arrowCount / 2;
        float basePitch = player.getXRot();
        float baseYaw = player.getYRot();

        for (int i = 0; i < arrowCount; i++) {
            AbstractArrow arrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);

            double damage = arrow.getBaseDamage();

            if (config.direct_hit_damage_override >= 0.0D) {
                damage = config.direct_hit_damage_override;
            } else if (config.use_ranged_damage_attribute_for_damage) {
                Holder<Attribute> rangedDamageAttr = serverLevel.registryAccess()
                        .registryOrThrow(Registries.ATTRIBUTE)
                        .getHolder(ResourceLocation.fromNamespaceAndPath(
                                config.ranged_damage_attribute_namespace,
                                config.ranged_damage_attribute_path
                        ))
                        .orElse(null);

                if (rangedDamageAttr != null) {
                    AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                    if (attrInstance != null && config.ranged_damage_attribute_divisor != 0.0D) {
                        damage = attrInstance.getValue() / config.ranged_damage_attribute_divisor;
                    }
                }
            }

            damage = (damage * config.direct_hit_damage_multiplier) + config.direct_hit_damage_bonus;
            arrow.setBaseDamage(damage);

            if (config.apply_bow_damage_attribute) {
                applyBowDamageAttribute(arrow, player);
            }

            if (config.apply_bow_crit) {
                tryApplyBowCrit(arrow, player, config.bow_crit_multiplier);
            }

            float offsetIndex = i - centerIndex;
            float spreadOffset = offsetIndex * config.spread_angle_degrees;

            arrow.shootFromRotation(
                    player,
                    basePitch,
                    baseYaw + spreadOffset,
                    0.0F,
                    power * config.velocity_multiplier,
                    config.inaccuracy
            );

            boolean isCenterArrow = i == centerIndex;
            arrow.pickup = parsePickupMode(isCenterArrow ? config.center_arrow_pickup : config.side_arrow_pickup);

            serverLevel.addFreshEntity(arrow);
        }

        if (!player.getAbilities().instabuild && config.consume_one_arrow_per_shot) {
            projectileStack.shrink(1);
        }
    }

    private AbstractArrow.Pickup parsePickupMode(String value) {
        if (value == null) {
            return AbstractArrow.Pickup.CREATIVE_ONLY;
        }

        return switch (value.toLowerCase()) {
            case "allowed" -> AbstractArrow.Pickup.ALLOWED;
            case "disallowed" -> AbstractArrow.Pickup.DISALLOWED;
            default -> AbstractArrow.Pickup.CREATIVE_ONLY;
        };
    }

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
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();

        if (!hasArrows && !player.getAbilities().instabuild) {
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
            tooltipComponents.add(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipComponents.add(Component.translatable("item.many_bows.arcane_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.arcane_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}