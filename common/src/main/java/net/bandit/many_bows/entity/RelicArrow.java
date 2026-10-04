package net.bandit.many_bows.entity;

import net.bandit.many_bows.item.RelicBow;
import net.bandit.many_bows.relic.*;

import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.*;


public class RelicArrow extends AbstractArrow {
    private RelicBow.Kind kind = RelicBow.Kind.EVENTIDE;
    private boolean ready, perfect, empowered, airborne, impact;
    private int age, phase, trialShot;
    private double visualDominance;
    private UUID targetId, bowId;
    private Vec3 origin = Vec3.ZERO;
    private final Map<Integer, Vec3> fallingStarDestinations = new HashMap<>();
    private final Set<UUID> hit = new HashSet<>();

    public RelicArrow(EntityType<? extends RelicArrow> type, Level level) {
        super(type, level);
    }

    public RelicArrow(Level level, LivingEntity shooter, ItemStack ammo, ItemStack bow) {
        super(EntityRegistry.RELIC_ARROW.get(), shooter, level, ammo, bow);
        if (!RelicData.read(bow).hasUUID("RelicId")) RelicData.edit(bow, t -> t.putUUID("RelicId", UUID.randomUUID()));
        bowId = RelicData.read(bow).getUUID("RelicId");
    }

    public void configure(RelicBow.Kind kind,
                          boolean ready,
                          boolean perfect,
                          boolean empowered,
                          Vec3 origin,
                          boolean airborne,
                          int shot) {
        this.kind = kind;
        this.ready = ready;
        this.perfect = perfect;
        this.empowered = empowered;
        this.origin = origin;
        this.airborne = airborne;
        this.trialShot = shot;
        visualDominance = kind == RelicBow.Kind.WORLDEATER
                ? Math.min(1, RelicData.read(bow()).getInt("Dominance")
                        / (double) Math.max(1, RelicConfig.get().maxDominance)) : 0;
    }

    private ItemStack bow() {
        if (!(getOwner() instanceof Player player) || bowId == null)
            return ItemStack.EMPTY;

        for (ItemStack stack : player.getInventory().items)
            if (matches(stack)) return stack;
        for (ItemStack stack : player.getInventory().offhand)
            if (matches(stack)) return stack;
        return ItemStack.EMPTY;
    }

    public void setRelicPiercing(byte level) {
        ((net.bandit.many_bows.mixin.RelicArrowInvoker)this).tmb$setPierceLevel(level);
    }

    private boolean matches(ItemStack stack) {
        var t = RelicData.read(stack); return t.hasUUID("RelicId") && bowId.equals(t.getUUID("RelicId"));
    }

    public static boolean validTarget(LivingEntity target, Entity owner) {
        if (!target.isAlive() || target == owner || target.isInvulnerable() || target.isSpectator()) return false;
        if (owner != null && (owner.isAlliedTo(target) || target.isAlliedTo(owner))) return false;
        if (target instanceof Player && !RelicConfig.get().affectPlayers) return false;
        return !RelicConfig.get().protectPets || !(target instanceof TamableAnimal pet) || !pet.isTame();
    }

    private DamageSource arcane() {
        var key = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("too_many_bows", "relic_arcane"));
        return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), this, getOwner());
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        Entity target = entity instanceof EnderDragonPart part ? part.parentMob : entity;
        return super.canHitEntity(entity) && !hit.contains(target.getUUID());
    }

    private boolean deal(LivingEntity target, float damage) {
        if (!validTarget(target, getOwner())) return false;
        boolean alive = target.isAlive();
        boolean accepted = target
                instanceof EnderDragon dragon ? dragon.hurt(dragon.head, arcane(), damage) : target.hurt(arcane(), damage);
        if (alive && !target.isAlive()) killed(target);
        return accepted;
    }

    private void killed(LivingEntity target) {
        ItemStack bow = bow(); if (bow.isEmpty()) return;
        if (kind == RelicBow.Kind.WORLDEATER) RelicData.edit(
                bow, t -> t.putInt("Dominance", Math.min(Math.max(1, RelicConfig.get().maxDominance), t.getInt("Dominance") + 1))
        );
        if (kind == RelicBow.Kind.BLUNTED && getOwner()
                instanceof ServerPlayer player) RelicQuests.trialKill(player, bow, target, origin, airborne, trialShot);
    }

    @Override protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            super.onHitEntity(result); return;
        }

        Entity struck = result.getEntity();
        LivingEntity target = struck instanceof EnderDragonPart part ? part.parentMob : struck instanceof LivingEntity living ? living : null;

        if (target == null) { resetPrecision(); if (kind == RelicBow.Kind.BLUNTED) RelicQuests.trialMiss(bow(), trialShot); super.onHitEntity(result);
            return;
        }
        if (!validTarget(target, getOwner())) { resetPrecision(); if (kind == RelicBow.Kind.BLUNTED) RelicQuests.trialMiss(bow(), trialShot); discard();
            return;
        }
        if (!hit.add(target.getUUID()))
            return;
        if (kind == RelicBow.Kind.GODSPLITTER && perfect) {
            boolean headshot = result.getLocation().y >= target.getY() + target.getBbHeight() * 0.75;
            ItemStack bow = bow(); var data = RelicData.read(bow);

            int stacks = data.hasUUID("SeverTarget") && target.getUUID().equals(data.getUUID("SeverTarget")) ? Math.min(5, data.getInt("Severance") + 1) : 1;
            if (!bow.isEmpty()) RelicData.edit(bow, t -> { t.putUUID("SeverTarget", target.getUUID()); t.putInt("Severance", stacks); });
            float damage = (float)(getBaseDamage() * getDeltaMovement().length()) * (headshot ? RelicConfig.get().headshotMultiplier : 1);
            boolean boss = target
                    instanceof EnderDragon || target
                    instanceof WitherBoss || target.getType().is(RelicQuests.BOSSES);

            if (!boss && headshot && target.getHealth() <= target.getMaxHealth() * 0.15f) damage = Math.max(damage, target.getHealth() + target.getAbsorptionAmount() + 1);
            // 75% bypass damage, 25% ordinary projectile damage; fifth stack fully bypasses armor.
            float bypass = stacks == 5 ? 1 : 0.75f;
            if (isOnFire()) target.igniteForSeconds(5);
            boolean accepted = deal(target, damage * bypass);
            if (accepted && target.isAlive() && bypass < 1) {
                target.invulnerableTime = 0;
                boolean alive = target.isAlive();
                target.hurt(level().damageSources().arrow(this, getOwner()), damage * (1 - bypass));
                if (alive && !target.isAlive()) killed(target);
            }
            RelicCombatEffects.fracture(server, result.getLocation(), stacks);
            server.sendParticles(headshot ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT, target.getX(), result.getLocation().y, target.getZ(), 25, 0.25, 0.25, 0.25, 0.1);
            server.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 1.6f);
            if (hit.size() >= 5) discard();
            return;
        }

        if (kind == RelicBow.Kind.GODSPLITTER) {
            RelicCombatEffects.fracture(server, result.getLocation(), 0);
        }
        boolean alive = target.isAlive();
        // Delay removal for ability carriers: vanilla arrows discard on ordinary entity impact.
        setRelicPiercing((byte)1);
        super.onHitEntity(result);
        if (alive && !target.isAlive()) killed(target);
        else if (kind == RelicBow.Kind.BLUNTED) RelicQuests.trialMiss(bow(), trialShot);
        if (target.isAlive()) {
            var pickup = getPickupItem();
            var potion = pickup.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            if (potion != null) potion.forEachEffect(effect -> target.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect), getOwner()));
            if (pickup.is(Items.SPECTRAL_ARROW)) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 200), getOwner());
        }
        if (ready && kind == RelicBow.Kind.EVENTIDE) {
            targetId = target.getUUID(); startImpact(); linkConstellation(server, target);
        } else if (ready && kind == RelicBow.Kind.WORLDEATER) {
            targetId = target.getUUID(); startImpact();
        } else discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel server && kind == RelicBow.Kind.GODSPLITTER) {
            RelicCombatEffects.fracture(server, result.getLocation(), perfect ? 2 : 0);
        }

        if (ready && kind == RelicBow.Kind.WORLDEATER) startImpact();
        else { resetPrecision(); if (kind == RelicBow.Kind.BLUNTED) RelicQuests.trialMiss(bow(), trialShot); }
    }

    private void resetPrecision() {
        if (kind == RelicBow.Kind.GODSPLITTER && hit.isEmpty() && !bow().isEmpty()) RelicData.edit(bow(),
                t -> { t.remove("SeverTarget"); t.putInt("Severance", 0); });
    }

    private void startImpact() {
        impact = true; phase = 0; age = 0; setDeltaMovement(Vec3.ZERO); setNoGravity(true); setNoPhysics(true); hasImpulse = true;
    }

    private void linkConstellation(ServerLevel level, LivingEntity target) {
        ItemStack bow = bow(); if (bow.isEmpty()) return;
        var tag = RelicData.read(bow); long now = level.getGameTime();
        List<UUID> ids = new ArrayList<>();
        if (now - tag.getLong("ConstellationTime") < 160) for (int i = 0; i < 3; i++) if (tag.hasUUID("Star" + i)) ids.add(tag.getUUID("Star" + i));
        boolean repeated = ids.contains(target.getUUID());
        if (!repeated) { if (ids.size() == 3) ids.remove(0); ids.add(target.getUUID()); }
        int alignment = repeated ? Math.min(5, tag.getInt("Alignment") + 1) : 0;
        RelicData.edit(bow, t -> { for (int i = 0; i < 3; i++) t.remove("Star" + i); for (int i = 0; i < ids.size(); i++) t.putUUID("Star" + i, ids.get(i)); t.putLong("ConstellationTime", now); t.putInt("Alignment", alignment); });
        if (ids.size() == 3) for (UUID id : ids) {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity other && other != target && other.distanceTo(target) <= 64) { deal(other, RelicConfig.get().lanceDamage); beam(level, target.position().add(0, 1, 0), other.position().add(0, 1, 0), ParticleTypes.END_ROD); }
        }
    }

    private static void beam(ServerLevel level, Vec3 from, Vec3 to, net.minecraft.core.particles.SimpleParticleType type) {
        int steps = Math.min(80, Math.max(1, (int)(from.distanceTo(to) * 2)));
        for (int i = 0; i <= steps; i++) { Vec3 p = from.lerp(to, (double)i / steps); level.sendParticles(type, p.x, p.y, p.z, 1, 0, 0, 0, 0); }
    }

    @Override
    public void tick() {
        if (!(level() instanceof ServerLevel server)) { super.tick(); return; }
        age++;
        if (age > 220) { resetPrecision(); if (kind == RelicBow.Kind.BLUNTED && hit.isEmpty()) RelicQuests.trialMiss(bow(), trialShot); discard(); return; }
        if (impact) {
            setDeltaMovement(Vec3.ZERO); phase++;
            if (kind == RelicBow.Kind.EVENTIDE) starfall(server); else dragonfire(server);
            return;
        }
        if (kind == RelicBow.Kind.WORLDEATER && ready && !inGround) {
            Vec3 direction = getDeltaMovement().normalize();
            LivingEntity closest = null; double dist = 8;
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8))) {
                Vec3 offset = target.getEyePosition().subtract(position());
                if (validTarget(target, getOwner()) && offset.normalize().dot(direction) > 0.85 && offset.length() < dist) { closest = target; dist = offset.length(); }
            }
            if (closest != null) setDeltaMovement(getDeltaMovement().lerp(closest.getEyePosition().subtract(position()).normalize().scale(getDeltaMovement().length()), 0.08));
            for (Projectile other : server.getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(0.6))) if (other != this && other.getOwner() != getOwner() && other instanceof AbstractArrow) other.discard();
            if (age % 2 == 0) { server.sendParticles(ParticleTypes.DRAGON_BREATH, getX(), getY(), getZ(), 5, 0.2, 0.2, 0.2, 0.01); wings(server, position(), direction, empowered ? 3 : 1); }
        }
        if (!inGround) {
            if (kind == RelicBow.Kind.GODSPLITTER) {
                RelicCombatEffects.precisionTrail(server, position(), getDeltaMovement(), perfect);
            } else if (kind == RelicBow.Kind.WORLDEATER) {
                RelicCombatEffects.worldTrail(server, position(), getDeltaMovement(),
                        age, visualDominance, empowered);
            }
        }
        if (kind == RelicBow.Kind.EVENTIDE && !inGround) {
            Vec3 motion = getDeltaMovement();
            if (motion.lengthSqr() > 0.01) {
                // Interpolate along this tick's path, rather than isolated dots at the arrow.
                for (int i = 0; i < 3; i++) {
                    Vec3 point = position().add(motion.scale(i / 3.0));
                    CelestialEffects.send(server,
                            i == 0 ? RelicParticleRegistry.ANGEL_TRAIL.get()
                                    : RelicParticleRegistry.FALLING_STAR.get(),
                            point, i == 0 ? motion : Vec3.ZERO, 96);
                }
            }
        }
        super.tick();
    }

    private void starfall(ServerLevel server) {
        Entity entity = targetId == null ? null : server.getEntity(targetId);
        if (entity != null) setPos(entity.position());

        int delay = Math.max(10, Math.min(100, RelicConfig.get().starfallDelayTicks));
        if (phase % 5 == 0 && phase < delay) {
            server.sendParticles(RelicParticleRegistry.FALLING_STAR.get(),
                    getX(), getY() + 2, getZ(), 3, 0.8, 0.4, 0.8, 0.02);
        }

        // Schedule each descending core twelve ticks before its damage pulse.
        // Even a ten-tick configured delay has a visible descent.
        for (int lance = 0; lance < 5; lance++) {
            int impactTick = delay + lance * 8;
            int launchTick = Math.max(1, impactTick - 12);
            if (phase == launchTick) {
                Vec3 strike = position().add(
                        Math.cos(lance * 1.3) * 1.5, 0,
                        Math.sin(lance * 1.3) * 1.5);
                fallingStarDestinations.put(lance, strike);
                int flightTicks = impactTick - launchTick;
                double height = flightTicks * 1.5;
                // count=0 transmits an exact velocity through the particle packet.
                CelestialEffects.send(server, RelicParticleRegistry.FALLING_STAR.get(),
                        strike.add(0, height, 0), new Vec3(0, -1.5, flightTicks), 128);
            }

            if (phase != impactTick) continue;
            Vec3 strike = fallingStarDestinations.remove(lance);
            if (strike == null) {
                // Restored arrows can resume their damage sequence without stored visuals.
                strike = position().add(Math.cos(lance * 1.3) * 1.5, 0,
                        Math.sin(lance * 1.3) * 1.5);
            }
            server.sendParticles(RelicParticleRegistry.FALLING_STAR.get(),
                    strike.x, strike.y + 0.4, strike.z,
                    18, 0.6, 0.3, 0.6, 0.15);
            server.sendParticles(ParticleTypes.EXPLOSION,
                    strike.x, strike.y, strike.z, 2, 0.4, 0.2, 0.4, 0);
            server.playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.PLAYERS, 0.6F, 1.5F);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class,
                    new AABB(strike, strike).inflate(3))) {
                float extra = lance == 4
                        ? Math.min(100, target.getMaxHealth()
                        * Math.max(0, RelicConfig.get().finalMaxHealthFraction)
                        * (1 + RelicData.read(bow()).getInt("Alignment") * 0.15F))
                        : 0;
                deal(target, RelicConfig.get().lanceDamage + extra);
            }
        }
        if (phase >= delay + 40) discard();
    }

    private void dragonfire(ServerLevel server) {
        if (phase == 1) {
            RelicCombatEffects.worldImpact(server, position(), empowered);
            Vec3 from = origin.add(0, 2, 0), to = position().add(0, 1, 0);
            Vec3 delta = to.subtract(from); if (delta.length() > 64) from = to.subtract(delta.normalize().scale(64));
            beam(server, from, to, ParticleTypes.DRAGON_BREATH);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(3))) {
                if (distanceToSegment(target.position(), from, to) <= (empowered ? 4 : 2) && validTarget(target, getOwner())) {
                    deal(target, RelicConfig.get().dragonDiveDamage * (empowered ? 2 : 1)); target.push(0, 0.7, 0); target.igniteForSeconds(4);
                }
            }
            server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1, 0.7f);
        }

        double radius = empowered ? 7 : 3;
        if (phase % 5 == 0) { server.sendParticles(ParticleTypes.DRAGON_BREATH, getX(), getY() + 0.3, getZ(), empowered ? 40 : 15, radius / 2, 0.25, radius / 2, 0.02); wings(server, position().add(0, empowered ? 8 : 3, 0), position().subtract(origin).normalize(), empowered ? 4 : 2); }
        if (phase % 20 == 0) for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 2, radius))) if (target.distanceToSqr(this) <= radius * radius) deal(target, RelicConfig.get().dragonfireDamage * (empowered ? 2 : 1));
        if (phase >= Math.max(20, Math.min(180, RelicConfig.get().dragonfireDurationTicks))) discard();
    }

    private static double distanceToSegment(
            Vec3 p, Vec3 a, Vec3 b) { Vec3 d = b.subtract(a); double t = Math.max(0, Math.min(1, p.subtract(a).dot(d) / Math.max(0.001, d.lengthSqr())));
        return p.distanceTo(a.add(d.scale(t)));
    }

    private void wings(ServerLevel server, Vec3 center, Vec3 direction, double scale) {
        Vec3 side = new Vec3(-direction.z, 0, direction.x).normalize();
        for (int s : new int[]{-1, 1}) {
            Vec3 tip = center.add(side.scale(s * scale * 2)).add(0, scale, 0);
            beam(server, center, tip, ParticleTypes.DRAGON_BREATH);
            beam(server, tip, center.subtract(direction.scale(scale * 2)).add(side.scale(s * scale)), ParticleTypes.DRAGON_BREATH);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t); t.putDouble("VisualDominance", visualDominance); t.putInt("RelicKind", kind.ordinal()); t.putBoolean("Ready", ready);
        t.putBoolean("Perfect", perfect); t.putBoolean("Empowered", empowered); t.putBoolean("Airborne", airborne);
        t.putBoolean("Impact", impact); t.putInt("RelicAge", age); t.putInt("RelicPhase", phase); t.putInt("TrialShot", trialShot);
        t.putDouble("OriginX", origin.x); t.putDouble("OriginY", origin.y); t.putDouble("OriginZ", origin.z);
        if (targetId != null) t.putUUID("RelicTarget", targetId); if (bowId != null) t.putUUID("RelicBowId", bowId);
        int i = 0; for (UUID id : hit) t.putUUID("RelicHit" + i++, id); t.putInt("RelicHits", i);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        visualDominance = Math.max(0, Math.min(1, t.getDouble("VisualDominance"))); kind = RelicBow.Kind.values()[Math.max(0, Math.min(3, t.getInt("RelicKind")))];
        ready = t.getBoolean("Ready"); perfect = t.getBoolean("Perfect"); empowered = t.getBoolean("Empowered"); airborne = t.getBoolean("Airborne");
        impact = t.getBoolean("Impact"); age = t.getInt("RelicAge"); phase = t.getInt("RelicPhase"); trialShot = t.getInt("TrialShot");
        origin = new Vec3(t.getDouble("OriginX"), t.getDouble("OriginY"), t.getDouble("OriginZ"));
        if (t.hasUUID("RelicTarget")) targetId = t.getUUID("RelicTarget"); if (t.hasUUID("RelicBowId")) bowId = t.getUUID("RelicBowId");
        hit.clear(); for (int i = 0; i < Math.min(5, t.getInt("RelicHits")); i++) if (t.hasUUID("RelicHit" + i)) hit.add(t.getUUID("RelicHit" + i));
    }
}
