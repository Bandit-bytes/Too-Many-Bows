package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.ShulkerBlastProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShulkerBlastBow extends BowItem {
    public ShulkerBlastBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            // Calculate the bow's charge time and power
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Ensure the bow is sufficiently charged
            if (power >= 0.1F) {
                // Check if the player is in creative mode
                boolean hasInfiniteArrows = player.getAbilities().instabuild;

                // Find an arrow in the player's inventory if not in creative mode
                ItemStack arrowStack = findArrowInInventory(player);

                // Check if the player has arrows or is in creative mode
                if (hasInfiniteArrows || !arrowStack.isEmpty()) {
                    // Create and shoot the ShulkerBlastProjectile
                    ShulkerBlastProjectile projectile = new ShulkerBlastProjectile(level, player);
                    projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                    projectile.setOwner(player);

                    level.addFreshEntity(projectile);

                    // Play the bow shooting sound
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // Damage the bow after shooting
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                    // Consume an arrow if the player is not in creative mode
                    if (!hasInfiniteArrows) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }
                }
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.description").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }
}
