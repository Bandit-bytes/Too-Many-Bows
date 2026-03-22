package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.HunterBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;

public class HunterArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "hunter_bow";

    public HunterArrow(EntityType<? extends HunterArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public HunterArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.HUNTER_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static HunterBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, HunterBowConfig.class, HunterBowConfig::new);
    }

    private void applyConfiguredValues() {
        HunterBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        HunterBowConfig config = config();

        if (!level().isClientSide && isConfiguredPassiveMob(target, config)) {
            if (config.require_player_owner && !(this.getOwner() instanceof Player)) {
                return;
            }

            if (!config.require_target_dead_or_dying || !target.isAlive() || target.isDeadOrDying()) {
                improveDrops(target, config);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    private boolean isConfiguredPassiveMob(LivingEntity entity, HunterBowConfig config) {
        String id = entity.getType().builtInRegistryHolder().key().location().toString();
        return config.passive_mob_whitelist.contains(id);
    }

    private void improveDrops(LivingEntity entity, HunterBowConfig config) {
        RandomSource rng = this.level().random;
        boolean cooked = config.use_cooked_drops_if_target_on_fire && entity.isOnFire();

        if (entity.getType() == EntityType.COW && entity instanceof Cow) {
            int beef = randomRange(rng, config.cow_beef_min, config.cow_beef_max);
            int leather = randomRange(rng, config.cow_leather_min, config.cow_leather_max);

            if (beef > 0) entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_BEEF : Items.BEEF, beef));
            if (leather > 0) entity.spawnAtLocation(new ItemStack(Items.LEATHER, leather));

        } else if (entity.getType() == EntityType.PIG && entity instanceof Pig) {
            int pork = randomRange(rng, config.pig_pork_min, config.pig_pork_max);
            if (pork > 0) entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_PORKCHOP : Items.PORKCHOP, pork));

        } else if (entity.getType() == EntityType.SHEEP && entity instanceof Sheep sheep) {
            int mutton = randomRange(rng, config.sheep_mutton_min, config.sheep_mutton_max);
            if (mutton > 0) entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_MUTTON : Items.MUTTON, mutton));

            if (config.sheep_drop_wool_if_unsheared && !sheep.isSheared()) {
                Block woolBlock = woolFor(sheep.getColor());
                if (woolBlock != null) {
                    entity.spawnAtLocation(new ItemStack(woolBlock));
                }
            }

        } else if (entity.getType() == EntityType.CHICKEN && entity instanceof Chicken) {
            int meat = randomRange(rng, config.chicken_meat_min, config.chicken_meat_max);
            int feathers = randomRange(rng, config.chicken_feather_min, config.chicken_feather_max);

            if (meat > 0) entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_CHICKEN : Items.CHICKEN, meat));
            if (feathers > 0) entity.spawnAtLocation(new ItemStack(Items.FEATHER, feathers));

        } else if (entity.getType() == EntityType.RABBIT && entity instanceof Rabbit) {
            int meat = randomRange(rng, config.rabbit_meat_min, config.rabbit_meat_max);
            int hide = randomRange(rng, config.rabbit_hide_min, config.rabbit_hide_max);

            if (meat > 0) entity.spawnAtLocation(new ItemStack(cooked ? Items.COOKED_RABBIT : Items.RABBIT, meat));
            if (hide > 0) entity.spawnAtLocation(new ItemStack(Items.RABBIT_HIDE, hide));

            if (config.rabbit_foot_chance > 0.0D && rng.nextDouble() < config.rabbit_foot_chance) {
                entity.spawnAtLocation(new ItemStack(Items.RABBIT_FOOT));
            }
        }
    }

    private int randomRange(RandomSource rng, int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        return low + rng.nextInt(high - low + 1);
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