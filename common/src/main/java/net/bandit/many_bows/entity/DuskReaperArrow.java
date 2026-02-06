//package net.bandit.many_bows.entity;
//
//import net.bandit.many_bows.registry.EntityRegistry;
//import net.minecraft.core.particles.ParticleTypes;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.TickTask;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.effect.MobEffects;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.decoration.ArmorStand;
//import net.minecraft.world.entity.projectile.AbstractArrow;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.EntityHitResult;
//
//public class DuskReaperArrow extends AbstractArrow {
//    private float powerMultiplier = 1.0F;
//
//    private static final byte MARKER_MASK = 0x10;
//
//    public void setPowerMultiplier(float power) {
//        this.powerMultiplier = power;
//    }
//
//    private static final float BASE_DAMAGE = 8.0f;
//    private static final int SLOWNESS_DURATION = 60;
//    private static final int WEAKNESS_DURATION = 60;
//    private static final int GLOW_DURATION = 200;
//    private static final int HARM_DURATION = 100;
//
//    public DuskReaperArrow(EntityType<? extends DuskReaperArrow> entityType, Level level) {
//        super(entityType, level);
//        this.setBaseDamage(BASE_DAMAGE);
//    }
//
//    public DuskReaperArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
//        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level, bowStack, arrowStack);
//        this.setBaseDamage(BASE_DAMAGE);
//    }
//
//    @Override
//    protected void onHitEntity(EntityHitResult result) {
//        super.onHitEntity(result);
//
//        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
//            if (target == this.getOwner()) return;
//
//            Level lvl = target.level();
//            float scaledDamage = (float) this.getBaseDamage();
//
//            if (this.getOwner() instanceof LivingEntity shooter) {
//                var reg = lvl.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
//                var rangedAttrHolder = reg.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);
//                if (rangedAttrHolder != null) {
//                    var inst = shooter.getAttribute(rangedAttrHolder);
//                    if (inst != null) scaledDamage = (float) inst.getValue() * 2F;
//                }
//            }
//
//            target.hurt(this.damageSources().magic(), scaledDamage * this.powerMultiplier);
//            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 1));
//            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 1));
//            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0));
//
//            target.addTag("manybows:marked_for_death");
//
//            if (lvl instanceof net.minecraft.server.level.ServerLevel sl) {
//                spawnFloatingLabel(sl, target, Component.literal("Marked for Death"), WEAKNESS_DURATION);
//            }
//        }
//
//        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
//                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.5F);
//        this.discard();
//    }
//
//    @Override
//    protected ItemStack getPickupItem() {
//        return ItemStack.EMPTY;
//    }
//
//    @Override
//    protected ItemStack getDefaultPickupItem() {
//        return ItemStack.EMPTY;
//    }
//
//    private static void spawnFloatingLabel(ServerLevel sl, LivingEntity target,
//                                           Component text, int durationTicks) {
//        var stand = new ArmorStand(
//                sl, target.getX(), target.getY() + target.getBbHeight() + 0.25, target.getZ()
//        );
//
//        stand.setInvisible(true);
//        stand.setNoGravity(true);
//        stand.setInvulnerable(true);
//        stand.setCustomName(text);
//        stand.setCustomNameVisible(true);
//        stand.setSilent(true);
//
//        // ▶ make it a marker via DATA_CLIENT_FLAGS
//        var data = stand.getEntityData();
//        byte flags = data.get(ArmorStand.DATA_CLIENT_FLAGS);
//        data.set(ArmorStand.DATA_CLIENT_FLAGS, (byte)(flags | MARKER_MASK));
//
//        sl.addFreshEntity(stand);
//        stand.startRiding(target, true);
//
//        sl.getServer().execute(() ->
//                sl.getServer().tell(new TickTask(sl.getServer().getTickCount() + durationTicks, () -> {
//                    if (stand.isAlive()) stand.discard();
//                    if (target.isAlive()) target.removeTag("manybows:marked_for_death");
//                }))
//        );
//    }
//}
