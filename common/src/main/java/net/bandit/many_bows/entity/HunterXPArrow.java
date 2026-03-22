package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.HunterXpBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class HunterXPArrow extends AbstractArrow {

    private int lifetime = 0;

    public HunterXPArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public HunterXPArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.HUNTER_XP_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private HunterXpBowConfig config() {
        return HunterXpBowConfig.get();
    }

    private void applyConfigValues() {
        HunterXpBowConfig config = config();
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

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            HunterXpBowConfig config = config();

            String id = level()
                    .registryAccess()
                    .lookupOrThrow(Registries.ENTITY_TYPE)
                    .getKey(target.getType())
                    .toString();

            boolean blacklisted = config.xp_blacklist.contains(id);
            boolean shouldReward =
                    config.reward_xp_on_hit ||
                            (!config.require_killing_blow || !target.isAlive() || target.isDeadOrDying());

            if (!blacklisted && shouldReward) {
                spawnExperienceOrb(target, config.xp_orb_amount);
            }
        }

        if (config().discard_on_entity_hit) {
            this.discard();
        }
    }

    private void spawnExperienceOrb(LivingEntity target, int xpAmount) {
        if (xpAmount > 0) {
            level().addFreshEntity(
                    new ExperienceOrb(level(), target.getX(), target.getY(), target.getZ(), xpAmount)
            );
        }
    }

    @Override
    public ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}