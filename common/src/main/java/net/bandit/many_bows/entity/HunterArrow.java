package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;


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

        if (!(result.getEntity() instanceof LivingEntity target)) return;

        if (!level().isClientSide && isPassiveMob(target) && this.getOwner() instanceof Player) {
            if (!target.isAlive() || target.isDeadOrDying()) {
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
        EntityType<?> t = entity.getType();
        return t == EntityType.COW ||
                t == EntityType.PIG ||
                t == EntityType.SHEEP ||
                t == EntityType.CHICKEN ||
                t == EntityType.RABBIT;
    }

    /**
     *
     *
     */
    private void improveDrops(LivingEntity entity) {
        RandomSource rng = this.level().random;
        boolean cooked = entity.isOnFire();

        if (entity.getType() == EntityType.COW && entity instanceof Cow) {
            int beef = 1 + rng.nextInt(3);   // 1–3
            int leather = rng.nextInt(2);    // 0–1
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_BEEF : Items.BEEF, beef));
            if (leather > 0) entity.spawnAtLocation(new ItemStack(Items.LEATHER, leather));
        } else if (entity.getType() == EntityType.PIG && entity instanceof Pig) {
            int pork = 1 + rng.nextInt(3);   // 1–3
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_PORKCHOP : Items.PORKCHOP, pork));
        } else if (entity.getType() == EntityType.SHEEP && entity instanceof Sheep sheep) {
            int mutton = 1 + rng.nextInt(3); // 1–3
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_MUTTON : Items.MUTTON, mutton));

            // Sheep drop 1 wool of their fleece color if not sheared
            if (!sheep.isSheared()) {
                Block woolBlock = woolFor(sheep.getColor());
                if (woolBlock != null) entity.spawnAtLocation(new ItemStack(woolBlock));
            }
        } else if (entity.getType() == EntityType.CHICKEN && entity instanceof Chicken) {
            int meat = 1;
            int feathers = 0 + rng.nextInt(3); // 0–2
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_CHICKEN : Items.CHICKEN, meat));
            if (feathers > 0) entity.spawnAtLocation(new ItemStack(Items.FEATHER, feathers));
        } else if (entity.getType() == EntityType.RABBIT && entity instanceof Rabbit) {
            int meat = rng.nextInt(2) + 1;   // 1–2
            int hide = rng.nextInt(2);       // 0–1
            entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_RABBIT : Items.RABBIT, meat));
            if (hide > 0) entity.spawnAtLocation(new ItemStack(Items.RABBIT_HIDE, hide));
            // rabbit's foot bonus
            if (rng.nextFloat() < 0.10f) {
                entity.spawnAtLocation(new ItemStack(Items.RABBIT_FOOT));
            }
        }
    }

    private Block woolFor(DyeColor color) {
        return switch (color) {
            case WHITE -> Blocks.WHITE_WOOL;
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case RED -> Blocks.RED_WOOL;
            case BLACK -> Blocks.BLACK_WOOL;
        };
    }
}
