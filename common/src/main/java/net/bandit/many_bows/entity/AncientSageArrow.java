package net.bandit.many_bows.entity;

import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class AncientSageArrow extends AbstractArrow {
    private static final float DEFAULT_ARMOR_PENETRATION_FACTOR = 0.33f;
    private static final int PARTICLE_LIFESPAN = 60;

    private float powerMultiplier = 1.0F;
    private float armorPenetration = DEFAULT_ARMOR_PENETRATION_FACTOR;
    private int particleTicksRemaining = PARTICLE_LIFESPAN;

    public AncientSageArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public AncientSageArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ANCIENT_SAGE_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public void setArmorPenetration(float armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    @Override
    public void tick() {
        super.tick();

        if (particleTicksRemaining > 0) {
            createTrailParticles();
            particleTicksRemaining--;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide()) return;
        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }

        float baseDamage = (float) ((AbstractArrowAccessor) this).manybows$getBaseDamage();

        if (this.getOwner() instanceof Player player) {
            var lookup = this.level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var holderOpt = lookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

            if (holderOpt.isPresent()) {
                AttributeInstance inst = player.getAttribute(holderOpt.get());
                if (inst != null) {
                    armorPenetration = (float) (inst.getValue() / 14.5F);
                }
            }
        }

        armorPenetration = Math.min(Math.max(armorPenetration, 0F), 1F);

        float dmg = baseDamage * armorPenetration * this.powerMultiplier;

        target.hurt(this.level().damageSources().arrow(this, this.getOwner()), dmg);

        createHitParticles();
        this.discard();
    }


    private void createHitParticles() {
        for (int i = 0; i < 15; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 0.5;
            double oy = this.random.nextDouble() * 0.5;
            double oz = (this.random.nextDouble() - 0.5) * 0.5;
            level().addParticle(ParticleTypes.ENCHANTED_HIT,
                    this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    0, 0.1, 0);
        }
    }

    private void createTrailParticles() {
        level().addParticle(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 0, 0.05, 0);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return null;
    }
}
