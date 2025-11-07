package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;


public class HunterArrow extends AbstractArrow {

    public HunterArrow(EntityType<? extends HunterArrow> entityType, Level level) {
        super(entityType, level);
    }

    public HunterArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.HUNTER_ARROW.get(), shooter, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) return;
        if (!this.level().isClientSide && isPassiveMob(target) && this.getOwner() instanceof Player) {
            if (!target.isAlive() || target.isDeadOrDying()) {
                improveDrops(target);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    private boolean isPassiveMob(LivingEntity entity) {
        EntityType<?> t = entity.getType();
        return t == EntityType.COW ||
                t == EntityType.PIG ||
                t == EntityType.SHEEP ||
                t == EntityType.CHICKEN ||
                t == EntityType.RABBIT;
    }

    private void improveDrops(LivingEntity entity) {
        RandomSource rng = this.level().random;;
        boolean cooked = entity.isOnFire();

        if (entity instanceof Cow) {
            int beef = 1 + rng.nextInt(3);
            int leather = rng.nextInt(2);
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_BEEF : Items.BEEF, beef));
            if (leather > 0) entity.spawnAtLocation(new ItemStack(Items.LEATHER, leather));

        } else if (entity instanceof Pig) {
            int pork = 1 + rng.nextInt(3);
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_PORKCHOP : Items.PORKCHOP, pork));

        } else if (entity instanceof Sheep sheep) {
            int mutton = 1 + rng.nextInt(3);
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_MUTTON : Items.MUTTON, mutton));
            if (!sheep.isSheared()) {
                entity.spawnAtLocation(new ItemStack(woolFor(sheep.getColor())));
            }

        } else if (entity instanceof Chicken) {
            int meat = 1;
            int feathers = rng.nextInt(3);
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_CHICKEN : Items.CHICKEN, meat));
            if (feathers > 0) entity.spawnAtLocation(new ItemStack(Items.FEATHER, feathers));

        } else if (entity instanceof Rabbit) {
            int meat = 1 + rng.nextInt(2);
            int hide = rng.nextInt(2);
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_RABBIT : Items.RABBIT, meat));
            if (hide > 0) entity.spawnAtLocation(new ItemStack(Items.RABBIT_HIDE, hide));
            if (rng.nextFloat() < 0.10f) {
                entity.spawnAtLocation(new ItemStack(Items.RABBIT_FOOT));
            }
        }
    }

    private Block woolFor(DyeColor color) {
        switch (color) {
            case WHITE: return Blocks.WHITE_WOOL;
            case ORANGE: return Blocks.ORANGE_WOOL;
            case MAGENTA: return Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_WOOL;
            case YELLOW: return Blocks.YELLOW_WOOL;
            case LIME: return Blocks.LIME_WOOL;
            case PINK: return Blocks.PINK_WOOL;
            case GRAY: return Blocks.GRAY_WOOL;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_WOOL;
            case CYAN: return Blocks.CYAN_WOOL;
            case PURPLE: return Blocks.PURPLE_WOOL;
            case BLUE: return Blocks.BLUE_WOOL;
            case BROWN: return Blocks.BROWN_WOOL;
            case GREEN: return Blocks.GREEN_WOOL;
            case RED: return Blocks.RED_WOOL;
            case BLACK: return Blocks.BLACK_WOOL;
            default: return Blocks.WHITE_WOOL;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
