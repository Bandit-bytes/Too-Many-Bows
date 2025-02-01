package net.bandit.many_bows.entity;


import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class HunterArrow extends AbstractArrow {

    public HunterArrow(EntityType<? extends HunterArrow> entityType, Level level) {
        super(entityType, level);
    }

    public HunterArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.HUNTER_ARROW.get(), shooter, level, bowStack, arrowStack);
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof LivingEntity target) {
            if (isPassiveMob(target) && this.getOwner() instanceof Player player) {
                improveDrops(target);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }

    private boolean isPassiveMob(LivingEntity entity) {
        return entity.getType() == EntityType.COW ||
                entity.getType() == EntityType.PIG ||
                entity.getType() == EntityType.SHEEP ||
                entity.getType() == EntityType.CHICKEN ||
                entity.getType() == EntityType.RABBIT;
    }

    private void improveDrops(LivingEntity entity) {
        // Drop extra leather and cooked beef
        entity.spawnAtLocation(Items.LEATHER, 2);
        entity.spawnAtLocation(Items.COOKED_BEEF, 2);
    }

}
