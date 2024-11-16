package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class HunterXPArrow extends AbstractArrow {

    public HunterXPArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public HunterXPArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.HUNTER_XP_ARROW.get(), shooter, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            int xpAmount = 10;
            level().addFreshEntity(new ExperienceOrb(level(), target.getX(), target.getY(), target.getZ(), xpAmount));
        }
        this.discard();
    }

    @Override
    public ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
