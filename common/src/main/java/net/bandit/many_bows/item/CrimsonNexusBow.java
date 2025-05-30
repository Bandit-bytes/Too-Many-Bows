package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static net.bandit.many_bows.config.ManyBowsConfigHolder.CONFIG;

public class CrimsonNexusBow extends BowItem {

    public CrimsonNexusBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.5F);

                float healthCost = player.getHealth() <= 4.0F ? 0 : 2.0F;
                player.hurt(player.damageSources().magic(), healthCost);
                ItemStack arrowStack = player.getProjectile(stack);
                ArrowItem arrowItem;

                if (arrowStack.isEmpty() || !(arrowStack.getItem() instanceof ArrowItem)) {
                    arrowItem = (ArrowItem) Items.ARROW;
                    arrowStack = new ItemStack(Items.ARROW);
                } else {
                    arrowItem = (ArrowItem) arrowStack.getItem();
                }

                AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, player);
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                applyEnchantments(stack, arrow);

                if (player.getHealth() == player.getMaxHealth()) {
                    arrow.setCritArrow(true);
                    arrow.setSecondsOnFire(200);
                }

                level.addFreshEntity(arrow);

                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getAbilities().instabuild || player.getProjectile(stack).isEmpty() || this.getAllSupportedProjectiles().test(player.getProjectile(stack))) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }


    private void applyEnchantments(ItemStack stack, AbstractArrow arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 1.5);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player && selected && !level.isClientSide) {
            if (level.getGameTime() % 20 == 0 && player.getHealth() < player.getMaxHealth()) {
                AABB area = new AABB(player.getX() - 8, player.getY() - 4, player.getZ() - 8,
                        player.getX() + 8, player.getY() + 4, player.getZ() + 8);

                List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && e.isAlive() && !(e instanceof Player));

                BowLootConfig config = ManyBowsConfigHolder.CONFIG;

                nearby.removeIf(target -> {
                    String id = target.getType().builtInRegistryHolder().key().location().toString();
                    return config.emeraldSageCrimsonNexusBlacklist.contains(id);
                });

                if (!nearby.isEmpty()) {
                    LivingEntity target = nearby.get(level.random.nextInt(nearby.size()));
                    target.hurt(player.damageSources().magic(), 2.0F);
                    player.heal(1.0F);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.3F, 1.5F);

                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
                }
            }
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> stack.getItem() instanceof ArrowItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.info").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.health_cost", "2.0").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            // Base message prompting to hold Shift
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }
}
