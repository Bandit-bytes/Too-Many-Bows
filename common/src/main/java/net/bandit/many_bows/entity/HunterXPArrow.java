package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.HunterXpBowConfig;
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

    private static final String CONFIG_NAME = "emerald_sage_bow";

    public HunterXPArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public HunterXPArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.HUNTER_XP_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static HunterXpBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, HunterXpBowConfig.class, HunterXpBowConfig::new);
    }

    private void applyConfiguredValues() {
        HunterXpBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            HunterXpBowConfig config = config();

            if (config.spawn_xp_on_living_hit) {
                String id = target.getType().builtInRegistryHolder().key().location().toString();

                if (!config.xp_blacklist.contains(id)) {
                    spawnExperienceOrb(target, config.xp_amount);
                }
            }
        }

        if (config().discard_after_entity_hit) {
            this.discard();
        }
    }

    private void spawnExperienceOrb(LivingEntity target, int xpAmount) {
        if (xpAmount > 0) {
            level().addFreshEntity(new ExperienceOrb(level(), target.getX(), target.getY(), target.getZ(), xpAmount));
        }
    }

    @Override
    public ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}