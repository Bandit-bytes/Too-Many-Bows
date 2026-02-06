//package net.bandit.many_bows.entity;
//
//import net.bandit.many_bows.registry.EntityRegistry;
//import net.minecraft.core.particles.ParticleTypes;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.network.protocol.game.ClientGamePacketListener;
//import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.projectile.AbstractArrow;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.EntityHitResult;
//
//public class SonicBoomProjectile extends AbstractArrow {
//    private float powerMultiplier = 1.0F;
//
//    public void setPowerMultiplier(float power) {
//        this.powerMultiplier = power;
//    }
//    private int lifetime = 60;
//    private int tickCount = 0;
//
//    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
//        super(entityType, level);
//        this.setNoGravity(true);
//    }
//
//    public SonicBoomProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
//        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
//        this.setNoGravity(true);
//    }
//
//    @Override
//    protected void onHitEntity(EntityHitResult result) {
//        super.onHitEntity(result);
//        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
//            LivingEntity shooter = this.getOwner() instanceof LivingEntity le ? le : null;
//            float scaledDamage = 20.0F;
//
//            if (shooter != null) {
//                var registry = level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
//                var rangedAttrHolder = registry.getHolder(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);
//
//                if (rangedAttrHolder != null) {
//                    var attrInstance = shooter.getAttribute(rangedAttrHolder);
//                    if (attrInstance != null) {
//                        scaledDamage = (float) attrInstance.getValue() * 1.5F + 12;
//                    }
//                }
//            }
//
//            float finalDamage = scaledDamage * this.powerMultiplier;
//            target.hurt(damageSources().sonicBoom(this), finalDamage);
//            target.knockback(2.0F, Math.sin(this.getYRot() * Math.PI / 180.0F), -Math.cos(this.getYRot() * Math.PI / 180.0F));
//            level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5F, 1.0F);
//            this.discard();
//        }
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//        tickCount++;
//
//        if (!this.level().isClientSide && --this.lifetime <= 0) {
//            this.discard();
//        }
//
//        if (this.level().isClientSide) {
//            createSonicBoomSpiral();
//        }
//    }
//    private void createSonicBoomSpiral() {
//        int particles = 25;
//        double radius = 0.5;
//        double spiralExpansionRate = 0.15;
//
//        for (int i = 0; i < particles; i++) {
//            double angle = 2 * Math.PI * (i + tickCount * 0.1);
//            double offsetX = radius * Math.cos(angle);
//            double offsetZ = radius * Math.sin(angle);
//            double offsetY = tickCount * 0.05 - (i * 0.01);
//
//            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
//                    this.getX() + offsetX,
//                    this.getY() + offsetY,
//                    this.getZ() + offsetZ,
//                    0.0D, 0.0D, 0.0D);
//
//            radius += spiralExpansionRate;
//        }
//    }
//
//    @Override
//    protected ItemStack getPickupItem() {
//        return ItemStack.EMPTY;
//    }
//
//    @Override
//    protected ItemStack getDefaultPickupItem() {
//        return null;
//    }
//}
