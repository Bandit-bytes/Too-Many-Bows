package net.bandit.many_bows.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class HunterArrow extends Arrow {
    public HunterArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public HunterArrow(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof LivingEntity target) {
            if (isPassiveMob(target) && this.getOwner() instanceof Player player) {
                improveDrops(target, player);
            }
        }
    }


    private boolean isPassiveMob(LivingEntity entity) {
        return entity.getType() == EntityType.COW ||
                entity.getType() == EntityType.PIG ||
                entity.getType() == EntityType.SHEEP ||
                entity.getType() == EntityType.CHICKEN ||
                entity.getType() == EntityType.RABBIT;
    }

    private void improveDrops(LivingEntity entity, Player player) {
        entity.spawnAtLocation(Items.LEATHER, 2);
        entity.spawnAtLocation(Items.COOKED_BEEF, 2);
    }
}
