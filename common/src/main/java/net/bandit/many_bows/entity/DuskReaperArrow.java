package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DuskReaperArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    private static final byte MARKER_MASK = 0x10;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    private static final float BASE_DAMAGE = 8.0f;
    private static final int SLOWNESS_DURATION = 60;
    private static final int WEAKNESS_DURATION = 60;
    private static final int GLOW_DURATION = 200;
    private static final int HARM_DURATION = 100;
    private float baseDamage = BASE_DAMAGE;


    public DuskReaperArrow(EntityType<? extends DuskReaperArrow> entityType, Level level) {
        super(entityType, level);
        this.baseDamage = BASE_DAMAGE;
    }

    public DuskReaperArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.baseDamage = BASE_DAMAGE;
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) return;
        if (target == this.getOwner()) return;

        Level lvl = target.level();

        if (!lvl.isClientSide()) {
            float scaledDamage = this.baseDamage;

            if (this.getOwner() instanceof LivingEntity shooter) {
                var attrLookup = lvl.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);

                var rangedRefOpt = attrLookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));
                if (rangedRefOpt.isPresent()) {
                    var inst = shooter.getAttribute(rangedRefOpt.get());
                    if (inst != null) scaledDamage = (float) inst.getValue() * 2F;
                }
            }

            target.hurt(this.damageSources().magic(), scaledDamage * this.powerMultiplier);

            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0));

            target.addTag("manybows:marked_for_death");

            if (lvl instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(),
                        20, 0.35, 0.35, 0.35, 0.02);
            }

        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.5F);

        this.discard();
    }


    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}
