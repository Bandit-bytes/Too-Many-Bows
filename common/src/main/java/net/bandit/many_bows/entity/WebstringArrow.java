package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.WebstringBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class WebstringArrow extends AbstractArrow {

    private int lifetime = 0;

    public WebstringArrow(EntityType<? extends WebstringArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public WebstringArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.WEBSTRING_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private WebstringBowConfig config() {
        return WebstringBowConfig.get();
    }

    private void applyConfigValues() {
        WebstringBowConfig config = config();
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

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            WebstringBowConfig config = config();

            if (config.apply_slowness) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.SLOWNESS,
                        config.slowness_duration_ticks,
                        config.slowness_amplifier
                ));
            }
        }

        if (config().discard_on_entity_hit) {
            this.discard();
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
}