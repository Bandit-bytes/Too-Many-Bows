package net.bandit.many_bows.client;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface PullSpeedItem {
    float getPullTicks(ItemStack stack, LivingEntity entity);
}

