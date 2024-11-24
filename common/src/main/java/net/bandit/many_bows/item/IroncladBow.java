package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IroncladBow extends BowItem {
    private static final double PULL_RADIUS = 10.0;
    private static final double PULL_STRENGTH = 0.15;

    public IroncladBow(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (entity instanceof Player player && !level.isClientSide()) {
            createPullingEffect(level, player);
        }
        super.onUseTick(level, entity, stack, count);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : findArrowInInventory(player);

                if (hasInfinity || !arrowStack.isEmpty()) {
                    Arrow arrow = new Arrow(level, player);
                    arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

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

                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    level.addFreshEntity(arrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.2F);

                    if (!hasInfinity) {
                        arrowStack.shrink(1);
                    }
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }

    private void createPullingEffect(Level level, Player player) {
        AABB pullArea = new AABB(player.getX() - PULL_RADIUS, player.getY() - PULL_RADIUS, player.getZ() - PULL_RADIUS,
                player.getX() + PULL_RADIUS, player.getY() + PULL_RADIUS, player.getZ() + PULL_RADIUS);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, pullArea);

        for (LivingEntity target : entities) {
            if (target != player) {
                Vec3 direction = player.position().subtract(target.position()).normalize().scale(PULL_STRENGTH);
                target.setDeltaMovement(target.getDeltaMovement().add(direction));
            }
        }
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.enchant(Enchantments.INFINITY_ARROWS, 1);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.ironclad_bow.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.too_many_bows.ironclad_bow.tooltip.ability").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.too_many_bows.ironclad_bow.tooltip.legend").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
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
        return repair.is(ItemRegistry.REPAIR_CRYSTAL.get());
    }
}
