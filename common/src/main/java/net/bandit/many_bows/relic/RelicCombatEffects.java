package net.bandit.many_bows.relic;

import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class RelicCombatEffects {
    private RelicCombatEffects() {}

    private static Vec3 side(Vec3 aim) {
        Vec3 result = aim.cross(new Vec3(0, 1, 0));
        return result.lengthSqr() < 0.001 ? new Vec3(1, 0, 0) : result.normalize();
    }

    public static void precisionCharge(ServerLevel level, Player player, int elapsed, int steady) {
        if (elapsed % 4 != 0 && steady != 30) return;
        Vec3 aim = player.getLookAngle();
        Vec3 center = player.getEyePosition().add(aim.scale(1.7)).add(0, -0.12, 0);
        Vec3 right = side(aim), up = right.cross(aim).normalize();
        double progress = Math.min(1, steady / 30.0);
        double radius = 0.34 - progress * 0.16;
        if (elapsed >= 20) CelestialEffects.send(level, RelicParticleRegistry.PRECISION_HALO.get(),
                center, new Vec3(progress, 0, 0), 48);
        for (int i = 0; i < 4; i++) {
            double angle = elapsed * 0.08 + i * Math.PI / 2;
            Vec3 offset = right.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
            CelestialEffects.send(level, RelicParticleRegistry.GOLD_SPARK.get(),
                    center.add(offset), offset.scale(-0.06), 48);
        }
        if (steady == 30) CelestialEffects.send(level, RelicParticleRegistry.PRECISION_HALO.get(),
                center.add(aim.scale(0.4)), new Vec3(1, 0, 0), 48);
    }

    public static void worldCharge(ServerLevel level, Player player, int elapsed, double dominance) {
        if (elapsed % 4 != 0) return;
        Vec3 aim = player.getLookAngle();
        Vec3 center = player.getEyePosition().add(aim.scale(1.5)).add(0, -0.18, 0);
        Vec3 right = side(aim), up = right.cross(aim).normalize();
        for (int i = 0; i < 3; i++) {
            double angle = elapsed * 0.12 + i * Math.PI * 2 / 3;
            Vec3 offset = right.scale(Math.cos(angle) * 0.32).add(up.scale(Math.sin(angle) * 0.32));
            CelestialEffects.send(level, net.minecraft.core.particles.PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F),
                    center.add(offset), offset.scale(-0.08), 48);
        }
        if (dominance >= 1 && elapsed >= 20 && elapsed % 12 == 0) {
            CelestialEffects.send(level, RelicParticleRegistry.WYRM_MAW.get(),
                    center.add(aim.scale(0.5)), new Vec3(0.2, 0, 0), 48);
        }
    }

    public static void precisionTrail(ServerLevel level, Vec3 position, Vec3 motion, boolean perfect) {
        int steps = Math.min(16, Math.max(1, (int) Math.ceil(motion.length() * 2)));
        for (int i = 0; i < steps; i++) {
            Vec3 point = position.add(motion.scale(i / (double) steps));
            CelestialEffects.send(level, RelicParticleRegistry.GOLD_SPARK.get(), point, Vec3.ZERO, 96);
            if (perfect && i % 3 == 0) CelestialEffects.send(level, ParticleTypes.END_ROD,
                    point, Vec3.ZERO, 96);
        }
    }

    public static void fracture(ServerLevel level, Vec3 point, int stacks) {
        CelestialEffects.send(level, RelicParticleRegistry.SEVERANCE_FLASH.get(), point,
                new Vec3(Math.min(1, stacks / 5.0), 0, 0), 96);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            CelestialEffects.send(level, RelicParticleRegistry.GOLD_SPARK.get(), point,
                    new Vec3(Math.cos(angle) * 0.16, Math.sin(angle) * 0.16, 0), 96);
        }
    }

    public static void worldTrail(ServerLevel level, Vec3 position, Vec3 motion,
                                  int age, double dominance, boolean empowered) {
        if (age % 2 != 0) return;
        Vec3 right = side(motion.normalize());
        double spread = 0.15 + dominance * 0.22;
        for (int i = -1; i <= 1; i++) {
            Vec3 point = position.add(right.scale(i * spread));
            CelestialEffects.send(level, net.minecraft.core.particles.PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), point,
                    motion.scale(-0.025), 96);
        }
        if (empowered && age % 8 == 0) CelestialEffects.send(level,
                RelicParticleRegistry.WYRM_MAW.get(), position, new Vec3(1, 0, 0), 96);
    }

    public static void worldImpact(ServerLevel level, Vec3 position, boolean empowered) {
        CelestialEffects.send(level, RelicParticleRegistry.WYRM_SHOCKWAVE.get(),
                position.add(0, 0.2, 0), new Vec3(empowered ? 1 : 0, 0, 0), 96);
        CelestialEffects.send(level, RelicParticleRegistry.WYRM_MAW.get(),
                position.add(0, 1.1, 0), new Vec3(empowered ? 1 : 0.25, 0, 0), 96);
    }
}
