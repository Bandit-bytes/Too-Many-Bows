package net.bandit.many_bows.client.particle;

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

public final class RelicCombatParticle extends TextureSheetParticle {
    public enum Mode { SPARK, HALO, FRACTURE, MAW, SHOCKWAVE }
    private final Mode mode;
    private final float initialSize;
    private final double shockwaveRadius;

    private RelicCombatParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz, SpriteSet sprites, Mode mode) {
        super(level, x, y, z);
        this.mode = mode;
        double strength = Math.max(0, Math.min(1, vx));
        lifetime = switch (mode) {
            case SPARK -> 10;
            case HALO -> 5;
            case FRACTURE -> 9;
            case MAW -> 10;
            case SHOCKWAVE -> 20;
        };
        quadSize = switch (mode) {
            case SPARK -> 0.07F;
            case HALO -> (float) (0.36 - strength * 0.14);
            case FRACTURE -> (float) (0.65 + strength * 0.65);
            case MAW -> (float) (0.6 + strength * 0.8);
            case SHOCKWAVE -> 0.12F;
        };
        initialSize = quadSize;
        shockwaveRadius = 3 + strength * 4;
        xd = mode == Mode.SPARK ? vx : 0;
        yd = mode == Mode.SPARK ? vy : 0;
        zd = mode == Mode.SPARK ? vz : 0;
        gravity = 0;
        friction = 0.92F;
        hasPhysics = false;
        if (mode == Mode.SHOCKWAVE) setColor(0.75F, 0.28F, 1.0F);
        pickSprite(sprites);
    }

    private void spark(ParticleOptions type, double px, double py, double pz,
                       double vx, double vy, double vz) {
        Minecraft.getInstance().particleEngine.createParticle(type, px, py, pz, vx, vy, vz);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) return;
        float progress = (float) age / lifetime;
        alpha = Math.min(1, (1 - progress) * 2);
        if (mode == Mode.FRACTURE || mode == Mode.MAW) {
            quadSize = initialSize * (1 + progress * 0.4F);
        }
        if (mode == Mode.HALO) {
            oRoll = roll;
            roll += 0.045F;
        }
        if (mode == Mode.SHOCKWAVE && age % 2 == 0) {
            double radius = shockwaveRadius * progress;
            for (int i = 0; i < 24; i++) {
                double angle = i * Math.PI * 2 / 24;
                spark(ParticleTypes.DRAGON_BREATH, x + Math.cos(angle) * radius,
                        y, z + Math.sin(angle) * radius, 0, 0.015, 0);
            }
        }
    }

    @Override public int getLightColor(float partialTick) { return 0xF000F0; }
    @Override public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Mode mode;
        public Provider(SpriteSet sprites, Mode mode) { this.sprites = sprites; this.mode = mode; }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new RelicCombatParticle(level, x, y, z, vx, vy, vz, sprites, mode);
        }
    }
}
