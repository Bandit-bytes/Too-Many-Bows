package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.IcicleJavelin;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IcicleJavelinBow extends ModBowItem {

    private static final int MIN_CHARGE_REQUIRED = 10;
    private static final double BASE_DAMAGE = 3.0D;
    private static final double CRIT_MULTIPLIER = 1.5D;

    private boolean hasPlayedPullSound = false;

    public IcicleJavelinBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge);

        if (!level.isClientSide) {
            boolean hasInfinity = canFireWithoutArrows(stack, player);
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

            if (charge >= MIN_CHARGE_REQUIRED && power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                IcicleJavelin javelin = new IcicleJavelin(level, player);
                javelin.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.5F, 1.0F);

                double dmg = BASE_DAMAGE;
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    dmg += powerLevel * 0.5D;
                }
                javelin.setBaseDamage(dmg);

                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    javelin.setKnockback(punchLevel);
                }

                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                    javelin.setSecondsOnFire(100);
                }

                applyBowDamageAttribute(javelin, player);
                tryApplyBowCrit(javelin, player, CRIT_MULTIPLIER);

                javelin.pickup = hasInfinity ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED;

                level.addFreshEntity(javelin);
                createIceParticles(level, player);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (!hasInfinity && !arrowStack.isEmpty()) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }

                damageBow(stack, player, player.getUsedItemHand());
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }

        hasPlayedPullSound = false;
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack s : player.getInventory().items) {
            if (s.getItem() instanceof ArrowItem) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.icicle_javelin_bow.tooltip").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.many_bows.icicle_javelin_bow.tooltip.ability").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }

    private void createIceParticles(Level level, LivingEntity entity) {
        for (int i = 0; i < 20; i++) {
            double offsetX = entity.getRandom().nextGaussian() * 0.3D;
            double offsetY = entity.getRandom().nextGaussian() * 0.3D;
            double offsetZ = entity.getRandom().nextGaussian() * 0.3D;
            level.addParticle(ParticleTypes.SNOWFLAKE,
                    entity.getX() + offsetX,
                    entity.getY() + entity.getBbHeight() / 1.5D + offsetY,
                    entity.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
