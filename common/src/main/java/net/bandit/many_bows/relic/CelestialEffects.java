package net.bandit.many_bows.relic;

import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class CelestialEffects {
    private CelestialEffects() {}

    public static void send(ServerLevel level, net.minecraft.core.particles.ParticleOptions type,
                            Vec3 position, Vec3 velocity, double range) {
        for (ServerPlayer viewer : level.players()) {
            if (viewer.position().distanceToSqr(position) <= range * range) {
                sendTo(level, viewer, type, position, velocity);
            }
        }
    }

    public static void sendTo(ServerLevel level, ServerPlayer viewer, net.minecraft.core.particles.ParticleOptions type,
                              Vec3 position, Vec3 velocity) {
        level.sendParticles(viewer, type, true, true, position.x, position.y, position.z,
                0, velocity.x, velocity.y, velocity.z, 1);
    }

    public static void ritual(ServerLevel level, ServerPlayer caller, Vec3 landing) {
        Vec3 start = caller.getEyePosition().add(caller.getLookAngle().scale(22));
        Vec3 displacement = landing.subtract(start);
        send(level, RelicParticleRegistry.RITUAL_STAR.get(), start, displacement, 192);
        // Caller always receives the ritual, including unusually large height differences.
        if (caller.position().distanceToSqr(start) > 192 * 192) {
            sendTo(level, caller, RelicParticleRegistry.RITUAL_STAR.get(), start, displacement);
        }
    }
}
