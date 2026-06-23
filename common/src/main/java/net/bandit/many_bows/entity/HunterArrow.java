package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.HunterBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class HunterArrow extends AbstractArrow {

    private int lifetime = 0;

    public HunterArrow(EntityType<? extends HunterArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public HunterArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.HUNTER_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private HunterBowConfig config() {
        return HunterBowConfig.get();
    }

    private void applyConfigValues() {
        HunterBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > config().max_lifetime_ticks) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        HunterBowConfig config = config();

        if (!level().isClientSide()
                && level() instanceof ServerLevel serverLevel
                && isSupportedMob(target, config)
                && (!config.require_player_owner || this.getOwner() instanceof Player)
                && (!config.require_killing_blow || !target.isAlive() || target.isDeadOrDying())) {

            improveDrops(serverLevel, target, config);
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    private boolean isSupportedMob(LivingEntity entity, HunterBowConfig config) {
        EntityType<?> t = entity.getType();

        return (config.affect_cows && t == EntityType.COW)
                || (config.affect_pigs && t == EntityType.PIG)
                || (config.affect_sheep && t == EntityType.SHEEP)
                || (config.affect_chickens && t == EntityType.CHICKEN)
                || (config.affect_rabbits && t == EntityType.RABBIT);
    }

    private void improveDrops(ServerLevel serverLevel, LivingEntity entity, HunterBowConfig config) {
        RandomSource rng = serverLevel.getRandom();
        boolean cooked = entity.isOnFire();

        if (entity.getType() == EntityType.COW && entity instanceof Cow) {
            int beef = randomRange(rng, config.cow_beef_min, config.cow_beef_max);
            int leather = randomRange(rng, config.cow_leather_min, config.cow_leather_max);

            if (beef > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(cooked ? Items.COOKED_BEEF : Items.BEEF, beef));
            }
            if (leather > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(Items.LEATHER, leather));
            }

        } else if (entity.getType() == EntityType.PIG && entity instanceof Pig) {
            int pork = randomRange(rng, config.pig_pork_min, config.pig_pork_max);

            if (pork > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(cooked ? Items.COOKED_PORKCHOP : Items.PORKCHOP, pork));
            }

        } else if (entity.getType() == EntityType.SHEEP && entity instanceof Sheep sheep) {
            int mutton = randomRange(rng, config.sheep_mutton_min, config.sheep_mutton_max);

            if (mutton > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(cooked ? Items.COOKED_MUTTON : Items.MUTTON, mutton));
            }

            if (config.sheep_drop_wool_if_unsheared && !sheep.isSheared()) {
                Block woolBlock = woolFor(sheep.getColor());
                if (woolBlock != null) {
                    entity.spawnAtLocation(serverLevel, new ItemStack(woolBlock));
                }
            }

        } else if (entity.getType() == EntityType.CHICKEN && entity instanceof Chicken) {
            int meat = randomRange(rng, config.chicken_meat_min, config.chicken_meat_max);
            int feathers = randomRange(rng, config.chicken_feather_min, config.chicken_feather_max);

            if (meat > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(cooked ? Items.COOKED_CHICKEN : Items.CHICKEN, meat));
            }
            if (feathers > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(Items.FEATHER, feathers));
            }

        } else if (entity.getType() == EntityType.RABBIT && entity instanceof Rabbit) {
            int meat = randomRange(rng, config.rabbit_meat_min, config.rabbit_meat_max);
            int hide = randomRange(rng, config.rabbit_hide_min, config.rabbit_hide_max);

            if (meat > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(cooked ? Items.COOKED_RABBIT : Items.RABBIT, meat));
            }
            if (hide > 0) {
                entity.spawnAtLocation(serverLevel, new ItemStack(Items.RABBIT_HIDE, hide));
            }
            if (rng.nextFloat() < config.rabbit_foot_chance) {
                entity.spawnAtLocation(serverLevel, new ItemStack(Items.RABBIT_FOOT));
            }
        }
    }

    private int randomRange(RandomSource rng, int min, int max) {
        int realMin = Math.min(min, max);
        int realMax = Math.max(min, max);
        return realMin + rng.nextInt(realMax - realMin + 1);
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

    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}