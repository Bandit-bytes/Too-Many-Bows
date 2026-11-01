package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.DuskReaperBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DuskReaperArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "dusk_reaper";
    private static final byte MARKER_MASK = 0x10;

    private float powerMultiplier = 1.0F;

    public DuskReaperArrow(EntityType<? extends DuskReaperArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public DuskReaperArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static DuskReaperBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, DuskReaperBowConfig.class, DuskReaperBowConfig::new);
    }

    private void applyConfiguredValues() {
        DuskReaperBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) {
            return;
        }

        DuskReaperBowConfig config = config();

        if (result.getEntity() instanceof LivingEntity target) {
            if (config.ignore_owner && target == this.getOwner()) {
                if (config.discard_if_owner_hit) {
                    this.discard();
                }
                return;
            }

            // Marked before damage is dealt so that a killing shot also triggers the death explosion.
            if (config.apply_marked_tag && !config.marked_tag.isBlank() && level() instanceof ServerLevel sl) {
                DuskReaperMarks.mark(target, this.getOwner(), config.marked_tag, config.marked_tag_duration_ticks, sl.getServer());
            }

            if (config.apply_direct_arrow_damage) {
                target.hurt(this.damageSources().arrow(this, this.getOwner()), (float) this.getBaseDamage());
            }

            if (config.bonus_magic_damage_enabled) {
                float scaledDamage = resolveBonusMagicDamage(target.level(), config);

                if (config.bonus_magic_damage_scales_with_power_multiplier) {
                    scaledDamage *= this.powerMultiplier;
                }

                if (scaledDamage > 0.0F) {
                    target.hurt(this.damageSources().magic(), scaledDamage);
                }
            }

            if (config.apply_slowness) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        config.slowness_duration_ticks,
                        config.slowness_amplifier
                ));
            }

            if (config.apply_weakness) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        config.weakness_duration_ticks,
                        config.weakness_amplifier
                ));
            }

            if (config.apply_glowing) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        config.glowing_duration_ticks,
                        config.glowing_amplifier
                ));
            }

            if (config.floating_label_enabled && target.isAlive() && level() instanceof ServerLevel sl) {
                spawnFloatingLabel(
                        sl,
                        target,
                        Component.translatable(config.floating_label_text),
                        config.floating_label_duration_ticks,
                        config.floating_label_y_offset,
                        config.floating_label_use_marker
                );
            }
        }

        if (config.impact_sound_enabled) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS,
                    config.impact_sound_volume,
                    config.impact_sound_pitch
            );
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private float resolveBonusMagicDamage(Level level, DuskReaperBowConfig config) {
        float scaledDamage = (float) config.bonus_magic_damage_base;

        if (config.use_ranged_damage_attribute_for_bonus_magic_damage && this.getOwner() instanceof LivingEntity shooter) {
            var reg = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = reg.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var inst = shooter.getAttribute(rangedAttrHolder);
                if (inst != null) {
                    scaledDamage = (float) (inst.getValue() * config.ranged_damage_attribute_multiplier);
                }
            }
        }

        return scaledDamage;
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    private static void spawnFloatingLabel(
            ServerLevel sl,
            LivingEntity target,
            Component text,
            int durationTicks,
            double yOffset,
            boolean useMarker
    ) {
        ArmorStand stand = new ArmorStand(
                sl,
                target.getX(),
                target.getY() + target.getBbHeight() + yOffset,
                target.getZ()
        ) {
            // Labels are tied to an in-memory mark, so they must not outlive it in saved chunks.
            @Override
            public boolean saveAsPassenger(CompoundTag tag) {
                return false;
            }
        };

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);
        stand.setSilent(true);

        if (useMarker) {
            var data = stand.getEntityData();
            byte flags = data.get(ArmorStand.DATA_CLIENT_FLAGS);
            data.set(ArmorStand.DATA_CLIENT_FLAGS, (byte) (flags | MARKER_MASK));
        }

        sl.addFreshEntity(stand);
        stand.startRiding(target, true);
        DuskReaperMarks.attachLabel(target, stand, durationTicks, sl.getServer());
    }
}