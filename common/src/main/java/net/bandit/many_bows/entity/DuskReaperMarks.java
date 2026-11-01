package net.bandit.many_bows.entity;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.DuskReaperBowConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tracks living Dusk Reaper marks: their expiry, floating labels, and explosion on death. */
public final class DuskReaperMarks {

    private static final class Mark {
        @Nullable UUID ownerId;
        String tag;
        int expiresAtTick;
        @Nullable ArmorStand label;
        int labelExpiresAtTick;

        void discardLabel() {
            if (label != null) {
                label.discard();
                label = null;
            }
        }
    }

    private static final Map<LivingEntity, Mark> MARKS = new HashMap<>();

    private DuskReaperMarks() {
    }

    public static void init() {
        EntityEvent.LIVING_DEATH.register(DuskReaperMarks::onDeath);
        TickEvent.SERVER_POST.register(DuskReaperMarks::tick);
        LifecycleEvent.SERVER_STOPPING.register(server -> clearAll());
    }

    /** Applies or refreshes a mark. A non-positive duration lasts until the target dies. */
    public static void mark(LivingEntity target, @Nullable Entity owner, String tag, int durationTicks, MinecraftServer server) {
        Mark mark = MARKS.computeIfAbsent(target, key -> new Mark());
        mark.ownerId = owner != null ? owner.getUUID() : null;
        mark.tag = tag;
        mark.expiresAtTick = durationTicks > 0 ? server.getTickCount() + durationTicks : Integer.MAX_VALUE;
        target.addTag(tag);
    }

    /** Attaches a label to a marked target, replacing any previous label. */
    public static void attachLabel(LivingEntity target, ArmorStand label, int durationTicks, MinecraftServer server) {
        Mark mark = MARKS.get(target);
        if (mark == null) {
            label.discard();
            return;
        }
        mark.discardLabel();
        mark.label = label;
        mark.labelExpiresAtTick = durationTicks > 0 ? server.getTickCount() + durationTicks : Integer.MAX_VALUE;
    }

    private static void tick(MinecraftServer server) {
        if (MARKS.isEmpty()) {
            return;
        }

        int now = server.getTickCount();
        Iterator<Map.Entry<LivingEntity, Mark>> iterator = MARKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, Mark> entry = iterator.next();
            LivingEntity target = entry.getKey();
            Mark mark = entry.getValue();

            if (now >= mark.labelExpiresAtTick) {
                mark.discardLabel();
            }

            if (target.isRemoved() || now >= mark.expiresAtTick) {
                mark.discardLabel();
                target.removeTag(mark.tag);
                iterator.remove();
            }
        }
    }

    private static EventResult onDeath(LivingEntity entity, DamageSource source) {
        // Removed before dealing damage so that chained deaths cannot re-enter this mark.
        Mark mark = MARKS.remove(entity);
        if (mark != null) {
            mark.discardLabel();
            entity.removeTag(mark.tag);
            if (entity.level() instanceof ServerLevel level) {
                explode(level, entity, mark);
            }
        }
        return EventResult.pass();
    }

    private static void explode(ServerLevel level, LivingEntity origin, Mark mark) {
        DuskReaperBowConfig config = BowJsonConfigHelper.getConfig("dusk_reaper", DuskReaperBowConfig.class, DuskReaperBowConfig::new);
        if (!config.death_explosion_enabled) {
            return;
        }

        Entity owner = mark.ownerId != null ? level.getEntity(mark.ownerId) : null;
        DamageSource damageSource = owner != null
                ? level.damageSources().indirectMagic(origin, owner)
                : level.damageSources().magic();

        double radius = Math.max(0.0D, config.death_explosion_radius);
        double radiusSqr = radius * radius;
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, origin.getBoundingBox().inflate(radius));

        for (LivingEntity entity : nearby) {
            if (entity != origin
                    && entity.isAlive()
                    && !(entity instanceof ArmorStand)
                    && entity.distanceToSqr(origin) <= radiusSqr
                    && isValidExplosionTarget(entity, owner, config)) {
                entity.hurt(damageSource, (float) config.death_explosion_damage);
            }
        }

        if (config.death_explosion_particles_enabled) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, origin.getX(), origin.getY(0.5D), origin.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.SOUL, origin.getX(), origin.getY(0.5D), origin.getZ(), 30, radius / 3.0D, 0.5D, radius / 3.0D, 0.02D);
        }

        if (config.death_explosion_sound_enabled) {
            level.playSound(null, origin.getX(), origin.getY(), origin.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                    config.death_explosion_sound_volume, config.death_explosion_sound_pitch);
        }
    }

    private static boolean isValidExplosionTarget(LivingEntity entity, @Nullable Entity owner, DuskReaperBowConfig config) {
        if (entity == owner) {
            return config.death_explosion_affects_owner;
        }
        if (!config.death_explosion_affects_allies && owner != null && entity.isAlliedTo(owner)) {
            return false;
        }
        return config.death_explosion_affects_tamed_animals || !(entity instanceof TamableAnimal tamable && tamable.isTame());
    }

    private static void clearAll() {
        for (Map.Entry<LivingEntity, Mark> entry : MARKS.entrySet()) {
            entry.getValue().discardLabel();
            entry.getKey().removeTag(entry.getValue().tag);
        }
        MARKS.clear();
    }
}
