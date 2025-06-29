package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class CrimsonNexusBow extends BowItem {
    private final WeakHashMap<Player, Long> activeLifeDrain = new WeakHashMap<>();
    public CrimsonNexusBow(Properties properties) {
        super(properties);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || !selected || level.isClientSide) return;

        Long lastUsedTime = activeLifeDrain.get(player);
        if (lastUsedTime == null || level.getGameTime() - lastUsedTime > 60) return; // Only for 3 seconds (60 ticks)

        if (level.getGameTime() % 20 == 0) { // Run once per second
            float damage; // Default

            // Scale based on ranged_weapon:damage / 4
            var registry = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = player.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    damage = (float) attrInstance.getValue() / 4.0F;
                } else {
                    damage = 1.0F;
                }
            } else {
                damage = 1.0F;
            }

            AABB area = new AABB(player.getX() - 10, player.getY() - 10, player.getZ() - 10,
                    player.getX() + 10, player.getY() + 10, player.getZ() + 10);

            level.getEntities(player, area, e -> e instanceof LivingEntity && e != player)
                    .forEach(target -> {
                        if (target instanceof LivingEntity livingEntity) {
                            livingEntity.hurt(player.damageSources().magic(), damage);
                            player.heal(0.25F);
                        }
                    });
        }
    }


    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                // Play custom sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.5F);

                // Calculate health cost (Prevents self-kill)
                float healthCost = player.getHealth() > 4.0F ? 2.0F : 0.0F;
                player.hurt(player.damageSources().magic(), healthCost);

                // Determine arrow type
                ItemStack arrowStack = player.getProjectile(bowStack);
                ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem ?
                        (ArrowItem) arrowStack.getItem() : (ArrowItem) Items.ARROW;

                AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, player, bowStack);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;


                // Fire the arrow
                Holder<Attribute> rangedDamageAttr = level.registryAccess()
                        .registryOrThrow(Registries.ATTRIBUTE)
                        .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                        .orElse(null);

                if (rangedDamageAttr != null) {
                    var attrInstance = player.getAttribute(rangedDamageAttr);
                    if (attrInstance != null) {
                        float damage = (float) attrInstance.getValue();
                        arrow.setBaseDamage(damage / 2.5F);
                    } else {
                        arrow.setBaseDamage(arrow.getBaseDamage() + 3.0);
                    }
                } else {
                    arrow.setBaseDamage(arrow.getBaseDamage() + 3.0);
                }

                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                activeLifeDrain.put(player, level.getGameTime());

                if (player.getHealth() == player.getMaxHealth()) {
                    arrow.setCritArrow(true);
                }

                level.addFreshEntity(arrow);
                bowStack.hurtAndBreak(1, player,(EquipmentSlot.MAINHAND));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }
//Fixed
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
        return 1;
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
            // Detailed information when holding Shift
            tooltipComponents.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.info").withStyle(ChatFormatting.DARK_RED));
            tooltipComponents.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.health_cost", "2.0").withStyle(ChatFormatting.RED));
            tooltipComponents.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            // Base message prompting to hold Shift
            tooltipComponents.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }
}
