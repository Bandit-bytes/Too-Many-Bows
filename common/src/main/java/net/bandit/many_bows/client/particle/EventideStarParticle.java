package net.bandit.many_bows.client.particle;

import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

public final class EventideStarParticle extends TextureSheetParticle {
    public enum Mode { STAR, RITUAL, ANGEL, BEACON }

    private final Mode mode;
    private final boolean falling;
    private final Vec3 side;
    private final Vec3 wingUp;

    private EventideStarParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz, SpriteSet sprites, Mode mode) {
        super(level, x, y, z);
        this.mode = mode;
        falling = mode == Mode.RITUAL || (mode == Mode.STAR && vy <= -0.5);
        lifetime = switch (mode) {
            case RITUAL -> 100;
            case ANGEL -> 14;
            case BEACON -> 40;
            case STAR -> falling ? (int) Math.max(1, Math.min(12, vz > 0 ? vz : 12)) : 24;
        };
        // Ritual packet velocity is the complete displacement to its landing point.
        xd = mode == Mode.RITUAL ? vx / lifetime : mode == Mode.STAR ? vx : 0;
        yd = mode == Mode.RITUAL ? vy / lifetime : mode == Mode.STAR ? vy : 0;
        zd = mode == Mode.RITUAL ? vz / lifetime : mode == Mode.STAR && !falling ? vz : 0;
        quadSize = switch (mode) {
            case RITUAL -> 1.15F;
            case ANGEL -> 0.18F;
            case BEACON -> 0.65F;
            case STAR -> falling ? 0.75F : 0.14F;
        };
        Vec3 direction = new Vec3(vx, vy, vz).normalize();
        Vec3 cross = direction.cross(new Vec3(0, 1, 0));
        side = cross.lengthSqr() < 0.001 ? new Vec3(1, 0, 0) : cross.normalize();
        wingUp = side.cross(direction).normalize();
        gravity = 0;
        friction = 1;
        hasPhysics = false;
        pickSprite(sprites);
        if (mode == Mode.ANGEL) drawWings();
    }

    // Create the secondary particles directly so distant ritual trails remain visible.
    // This method is client-only and never sends an extra network packet.
    private void spark(ParticleOptions type, double px, double py, double pz,
                       double vx, double vy, double vz) {
        Minecraft.getInstance().particleEngine.createParticle(type, px, py, pz, vx, vy, vz);
    }

    private void drawWings() {
        for (int sign : new int[] {-1, 1}) {
            for (int feather = 1; feather <= 4; feather++) {
                double span = feather * 0.20;
                Vec3 offset = side.scale(sign * span)
                        .add(wingUp.scale(0.3 * Math.sin(feather * Math.PI / 5)));
                spark(ParticleTypes.END_ROD, x + offset.x, y + offset.y, z + offset.z,
                        0, 0.005, 0);
            }
        }
    }

    @Override
    public void tick() {
        double oldX = x, oldY = y, oldZ = z;
        super.tick();
        if (!isAlive()) {
            if (mode == Mode.RITUAL) {
                for (int i = 0; i < 40; i++) {
                    double angle = i * Math.PI * 2 / 40;
                    spark(RelicParticleRegistry.FALLING_STAR.get(), x, y, z,
                            Math.cos(angle) * 0.12, 0.04, Math.sin(angle) * 0.12);
                }
            }
            return;
        }
        alpha = Math.min(1.0F, (lifetime - age) / (falling ? 3.0F : 10.0F));
        if (falling) {
            int segments = mode == Mode.RITUAL ? 6 : 4;
            for (int i = 0; i < segments; i++) {
                double t = (double) i / segments;
                double px = oldX + (x - oldX) * t;
                double py = oldY + (y - oldY) * t;
                double pz = oldZ + (z - oldZ) * t;
                spark(RelicParticleRegistry.FALLING_STAR.get(), px, py, pz, 0, 0, 0);
                spark(ParticleTypes.END_ROD, px, py, pz, 0, 0.01, 0);
            }
        }
        if (mode == Mode.BEACON && age % 5 == 0) {
            for (int i = 0; i < 20; i++) {
                double angle = i * Math.PI * 2 / 20 + age * 0.03;
                spark(RelicParticleRegistry.FALLING_STAR.get(),
                        x + Math.cos(angle) * 1.5, y + 0.15, z + Math.sin(angle) * 1.5,
                        0, 0.015, 0);
            }
            for (int i = 1; i <= 16; i++) {
                spark(ParticleTypes.END_ROD, x, y + i, z, 0, 0.025, 0);
            }
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Mode mode;

        public Provider(SpriteSet sprites) { this(sprites, Mode.STAR); }

        public Provider(SpriteSet sprites, Mode mode) {
            this.sprites = sprites;
            this.mode = mode;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new EventideStarParticle(level, x, y, z, vx, vy, vz, sprites, mode);
        }
    }
}
