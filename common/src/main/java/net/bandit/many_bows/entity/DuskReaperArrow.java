package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class DuskReaperArrow extends AbstractArrow {

    // Arrow Damage (maybe configurable one update?)
    private static final float BASE_DAMAGE = 8.0f;
    private static final int SLOWNESS_DURATION = 60;
    private static final int WEAKNESS_DURATION = 60;
    private static final int GLOW_DURATION = 200;
    private static final int HARM_DURATION = 100;


    private static final String MARK_TAG = "manybows_marked";
    private static final String MARK_PREFIX = "manybows_marked_until:";
    private static final int MARK_DURATION_TICKS = 200;

    public DuskReaperArrow(EntityType<? extends DuskReaperArrow> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public DuskReaperArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (target == this.getOwner()) return;

            target.hurt(this.damageSources().magic(), (float) this.getBaseDamage());
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0));
            target.addEffect(new MobEffectInstance(MobEffects.HARM, HARM_DURATION, 0));

            long expireAt = target.level().getGameTime() + MARK_DURATION_TICKS;

            for (String tag : List.copyOf(target.getTags())) {
                if (tag.startsWith(MARK_PREFIX)) {
                    target.removeTag(tag);
                }
            }
            target.addTag(MARK_TAG);
            target.addTag(MARK_PREFIX + expireAt);

            if (this.getOwner() instanceof LivingEntity shooter) {
                shooter.sendSystemMessage(Component.literal(
                        "Marked " + target.getDisplayName().getString() + " for death"));
            }

            target.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY(0.5), target.getZ(), 0, 0, 0);
            for (int i = 0; i < 18; i++) {
                target.level().addParticle(ParticleTypes.SOUL,
                        target.getX(), target.getY(0.5), target.getZ(),
                        (target.getRandom().nextDouble() - 0.5) * 0.4,
                        (target.getRandom().nextDouble()) * 0.2,
                        (target.getRandom().nextDouble() - 0.5) * 0.4);
            }
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F,
                    0.9F + target.getRandom().nextFloat() * 0.2F);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.5F);
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
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    public static boolean isMarked(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) return false;

        long now = entity.level().getGameTime();
        String expiryTag = null;

        for (String tag : entity.getTags()) {
            if (tag.startsWith(MARK_PREFIX)) {
                expiryTag = tag;
                break;
            }
        }

        if (expiryTag == null) {
            return entity.getTags().contains(MARK_TAG);
        }

        try {
            long until = Long.parseLong(expiryTag.substring(MARK_PREFIX.length()));
            if (now <= until) return true;

            // server clean up
            if (!entity.level().isClientSide()) {
                entity.removeTag(MARK_TAG);
                entity.removeTag(expiryTag);
            }
        } catch (NumberFormatException ignored) {
            if (!entity.level().isClientSide()) entity.removeTag(expiryTag);
        }
        return false;
    }
}
