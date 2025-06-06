package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DuskReaperArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private static final float BASE_DAMAGE = 8.0f;
    private static final int SLOWNESS_DURATION = 60;
    private static final int WEAKNESS_DURATION = 60;
    private static final int GLOW_DURATION = 200;
    private static final int HARM_DURATION = 100;

    public DuskReaperArrow(EntityType<? extends DuskReaperArrow> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public DuskReaperArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (target == this.getOwner()) {
                return;
            }
            Level level = target.level();
            float scaledDamage = (float) this.getBaseDamage();

            if (this.getOwner() instanceof LivingEntity shooter) {
                var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
                var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

                if (rangedAttrHolder != null) {
                    var attrInstance = shooter.getAttribute(rangedAttrHolder);
                    if (attrInstance != null) {
                        scaledDamage = (float) attrInstance.getValue() * 2F; // AoE hit scaling
                    } else {
                        scaledDamage = (float) this.getBaseDamage();
                    }
                } else {
                    scaledDamage = (float) this.getBaseDamage();
                }
            } else {
                scaledDamage = (float) this.getBaseDamage();
            }
            target.hurt(this.damageSources().magic(), scaledDamage * this.powerMultiplier);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0));

            // Mark the target for death
            target.setCustomName(Component.literal("Marked for Death"));
            target.setCustomNameVisible(true);
        }

        // Play impact sound and discard the arrow
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.5F);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SOUL, this.getX(), this.getY(), this.getZ(), 0, 0.1, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }
}
