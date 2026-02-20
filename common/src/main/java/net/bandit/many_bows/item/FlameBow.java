package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.entity.FlameArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

public class FlameBow extends ModBowItem {

    public FlameBow(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, user, stack, remainingUseDuration);

        if (!level.isClientSide) return;
        if (!(level instanceof ClientLevel clientLevel)) return;
        if (!(user instanceof Player player)) return;

        if (player.getUseItem() != stack) return;

        int useDuration = this.getUseDuration(stack, user);
        int charge = useDuration - remainingUseDuration;
        float power = getPowerForTime(charge);

        if (power < 0.25f) return;

        spawnFlameInfernoParticles(clientLevel, player, power);
    }

    // Fire colors - from yellow to orange to red
    private static final DustParticleOptions FLAME_YELLOW =
            new DustParticleOptions(new Vector3f(1.0f, 0.9f, 0.2f), 0.7f);

    private static final DustParticleOptions FLAME_ORANGE =
            new DustParticleOptions(new Vector3f(1.0f, 0.5f, 0.1f), 0.6f);

    private static final DustParticleOptions FLAME_RED =
            new DustParticleOptions(new Vector3f(1.0f, 0.2f, 0.05f), 0.6f);

    private static final DustParticleOptions FLAME_CORE =
            new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.8f), 0.5f);

    private static void spawnFlameInfernoParticles(ClientLevel level, Player player, float power) {
        Vec3 playerPos = player.position().add(0, 0.1, 0);
        float time = player.tickCount * 0.08f;

        if (player.tickCount % 2 == 0) {
            drawGroundFireRings(level, playerPos, power, time);
        }
        // Floating embers around player
        if (player.tickCount % 3 == 0) {
            spawnFloatingEmbers(level, playerPos, power);
        }

        // Ground heat waves
        if (player.tickCount % 4 == 0 && power > 0.5f) {
            spawnHeatWaves(level, playerPos, power, time);
        }

        if (power > 0.9f && player.tickCount % 2 == 0) {
            drawInfernoCrown(level, player, time);
        }

        // Explosive fire burst pulses
        if (player.tickCount % 10 == 0 && power > 0.6f) {
            spawnFireBurst(level, playerPos, power);
        }
    }

    private static void drawGroundFireRings(ClientLevel level, Vec3 center, float power, float time) {

        int numRings = power > 0.7f ? 4 : 3;

        for (int ring = 0; ring < numRings; ring++) {
            float radius = 1.4f + ring * 0.7f;
            int points = 24 + ring * 6;
            float rotation = time * (0.6f + ring * 0.4f) * (ring % 2 == 0 ? 1 : -1);

            for (int i = 0; i < points; i++) {
                if (i % 2 != (level.getGameTime() + ring) % 2) continue;

                float angle = (i / (float) points) * Mth.TWO_PI + rotation;
                double x = center.x + Mth.cos(angle) * radius;
                double z = center.z + Mth.sin(angle) * radius;
                double y = center.y + Mth.sin(i * 0.8f + time) * 0.08;

                DustParticleOptions particle = ring == 0 ? FLAME_YELLOW :
                        ring == 1 ? FLAME_ORANGE : FLAME_RED;

                level.addParticle(particle, x, y, z, 0, 0, 0);
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.02, 0);

                if (i % 4 == 0) {
                    level.addParticle(ParticleTypes.SMOKE, x, y + 0.2, z, 0, 0.05, 0);
                }
            }
        }
    }


    private static void spawnFloatingEmbers(ClientLevel level, Vec3 center, float power) {

        int numEmbers = (int) (6 + power * 4);

        for (int i = 0; i < numEmbers; i++) {
            float angle = level.random.nextFloat() * Mth.TWO_PI;
            float radius = 1.0f + level.random.nextFloat() * 1.5f;

            double x = center.x + Mth.cos(angle) * radius;
            double z = center.z + Mth.sin(angle) * radius;
            double y = center.y + level.random.nextFloat() * 2.0;

            // Embers rise and drift
            double vx = (level.random.nextFloat() - 0.5) * 0.02;
            double vy = 0.04 + level.random.nextFloat() * 0.02;
            double vz = (level.random.nextFloat() - 0.5) * 0.02;

            level.addParticle(FLAME_ORANGE, x, y, z, vx, vy, vz);
            level.addParticle(ParticleTypes.LAVA, x, y, z, vx, vy, vz);
        }
    }

    private static void spawnHeatWaves(ClientLevel level, Vec3 center, float power, float time) {

        int points = 32;
        float radius = 2.5f + Mth.sin(time * 2) * 0.3f;

        for (int i = 0; i < points; i++) {
            if (i % 3 != (int)(time * 8) % 3) continue;

            float angle = (i / (float) points) * Mth.TWO_PI + time;
            double x = center.x + Mth.cos(angle) * radius;
            double z = center.z + Mth.sin(angle) * radius;
            double y = center.y + 0.05;

            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            level.addParticle(FLAME_CORE, x, y, z, 0, 0, 0);
        }
    }

    private static void drawFireVortex(ClientLevel level, Vec3 center, float power, float time) {

        int layers = 8;

        for (int layer = 0; layer < layers; layer++) {
            float height = layer * 0.4f;
            float radius = 1.5f - layer * 0.15f;
            int points = 12;

            for (int i = 0; i < points; i++) {
                if (i % 2 != 0) continue;

                float angle = (i / (float) points) * Mth.TWO_PI + time * 2 + layer * 0.5f;
                double x = center.x + Mth.cos(angle) * radius;
                double z = center.z + Mth.sin(angle) * radius;
                double y = center.y + height;

                level.addParticle(FLAME_ORANGE, x, y, z, 0, 0.06, 0);
                level.addParticle(ParticleTypes.FLAME, x, y, z,
                        -Mth.sin(angle) * 0.05, 0.08, Mth.cos(angle) * 0.05);
            }
        }
    }

    private static void drawInfernoCrown(ClientLevel level, Player player, float time) {
        Vec3 center = player.position().add(0, 2.3, 0);

        int points = 20;
        float radius = 0.7f;

        for (int i = 0; i < points; i++) {
            if (i % 2 != 0) continue;

            float angle = (i / (float) points) * Mth.TWO_PI + time;
            float heightVar = (i % 3 == 0) ? 0.25f : 0.1f;

            double x = center.x + Mth.cos(angle) * radius;
            double z = center.z + Mth.sin(angle) * radius;
            double y = center.y + heightVar;

            level.addParticle(FLAME_YELLOW, x, y, z, 0, 0.06, 0);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.08, 0);

            double ix = center.x + Mth.cos(angle) * radius * 0.4f;
            double iz = center.z + Mth.sin(angle) * radius * 0.4f;
            level.addParticle(FLAME_CORE, ix, y, iz, 0, 0, 0);
        }
    }

    private static void spawnFireBurst(ClientLevel level, Vec3 center, float power) {
        // Explosive burst of flames outward
        int numBursts = (int) (12 + power * 8);

        for (int i = 0; i < numBursts; i++) {
            float angle = (i / (float) numBursts) * Mth.TWO_PI;
            float speed = 0.15f + level.random.nextFloat() * 0.1f;

            double vx = Mth.cos(angle) * speed;
            double vz = Mth.sin(angle) * speed;
            double vy = 0.02;

            level.addParticle(FLAME_ORANGE, center.x, center.y + 0.5, center.z, vx, vy, vz);
            level.addParticle(ParticleTypes.FLAME, center.x, center.y + 0.5, center.z, vx, vy + 0.03, vz);
        }
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            ItemStack arrowStack = player.getProjectile(bowStack);

            if (!arrowStack.isEmpty() || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                if (power >= 0.1F) {
                    List<ItemStack> projectiles = draw(bowStack, arrowStack, player);
                    boolean arrowConsumed = false;

                    if (!projectiles.isEmpty() && level instanceof ServerLevel serverLevel) {
                        for (ItemStack projectileStack : projectiles) {
                            AbstractArrow arrow;
                            if (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW)) {
                                arrow = ((ArrowItem) projectileStack.getItem()).createArrow(serverLevel, projectileStack, player, bowStack);
                            } else {
                                arrow = new FlameArrow(serverLevel, player, bowStack, projectileStack);
                                if (arrow instanceof FlameArrow flameArrow) {
                                    Holder<Attribute> rangedDamageAttr = level.registryAccess()
                                            .registryOrThrow(Registries.ATTRIBUTE)
                                            .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                                            .orElse(null);

                                    if (rangedDamageAttr != null) {
                                        AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                                        if (attrInstance != null) {
                                            float damage = (float) attrInstance.getValue();
                                            flameArrow.setBaseDamage(damage / 2.5);
                                        }
                                    }
                                    flameArrow.setPowerMultiplier(power);
                                }
                            }

                            applyPowerEnchantment(arrow, bowStack, level);
                            applyKnockbackEnchantment(arrow, bowStack, player, level);
                            applyFlameEnchantment(arrow, bowStack, level);
                            applyBowDamageAttribute(arrow, player);
                            tryApplyBowCrit(arrow, player, 1.5D);
                            if (hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild) {
                                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            } else {
                                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                            }
                            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
                            serverLevel.addFreshEntity(arrow);
                            if (!hasInfinityEnchantment(bowStack, level) && !player.getAbilities().instabuild && !arrowConsumed) {
                                projectileStack.shrink(1);
                                arrowConsumed = true;
                            }
                        }
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        damageBow(bowStack, player, InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    private void applyFlameEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> flame = getEnchantmentHolder(level, Enchantments.FLAME);
        int flameLevel = EnchantmentHelper.getItemEnchantmentLevel(flame, bow);
        if (flameLevel > 0) {
            arrow.igniteForSeconds(5);
        }
    }

    private void applyKnockbackEnchantment(AbstractArrow arrow, ItemStack bow, LivingEntity shooter, Level level) {
        Holder<Enchantment> punch = getEnchantmentHolder(level, Enchantments.PUNCH);
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(punch, bow);
        if (punchLevel > 0) {
            double resistance = Math.max(0.0, 1.0 - shooter.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            net.minecraft.world.phys.Vec3 knockbackVec = arrow.getDeltaMovement().normalize().scale(punchLevel * 0.6 * resistance);
            arrow.push(knockbackVec.x, 0.1, knockbackVec.z);
        }
    }

    private void applyPowerEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> power = getEnchantmentHolder(level, Enchantments.POWER);
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(power, bow);
        if (powerLevel > 0) {
            double extraDamage = 0.5 * powerLevel + 1.0;
            arrow.setBaseDamage(arrow.getBaseDamage() + extraDamage);
        }
    }

    private boolean hasInfinityEnchantment(ItemStack bow, Level level) {
        Holder<Enchantment> infinity = getEnchantmentHolder(level, Enchantments.INFINITY);
        return EnchantmentHelper.getItemEnchantmentLevel(infinity, bow) > 0;
    }

    private Holder<Enchantment> getEnchantmentHolder(Level level, ResourceKey<Enchantment> enchantmentKey) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(enchantmentKey);
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();
        if (!player.hasInfiniteMaterials() && !hasArrows) {
            return InteractionResultHolder.fail(bowStack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        }
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.too_many_bows.flame_bow.tooltip").withStyle(ChatFormatting.RED));
            tooltipComponents.add(Component.translatable("item.too_many_bows.flame_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.too_many_bows.flame_bow.tooltip.legend").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}