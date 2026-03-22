package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.WebstringBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class WebstringArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "webstring";

    public WebstringArrow(EntityType<? extends WebstringArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public WebstringArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.WEBSTRING_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static WebstringBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, WebstringBowConfig.class, WebstringBowConfig::new);
    }

    private void applyConfiguredValues() {
        WebstringBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            WebstringBowConfig config = config();

            if (config.apply_slowness_on_hit) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        config.slowness_duration_ticks,
                        config.slowness_amplifier
                ));
            }
        }

        if (config().discard_after_entity_hit) {
            this.discard();
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
}