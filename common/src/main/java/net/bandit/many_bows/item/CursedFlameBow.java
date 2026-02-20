package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.CursedFlameArrow;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

public class CursedFlameBow extends ModBowItem {

    private static final Identifier RANGED_WEAPON_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");
    private static final Identifier CURSED_FLAME_SIGIL_CRIT_ID =
            Identifier.fromNamespaceAndPath("too_many_bows", "cursed_flame_sigil_crit");

    public CursedFlameBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try{
            if (!(entity instanceof Player player)) return false;

        ItemStack ammoInInv = player.getProjectile(bowStack);
        if (ammoInInv.isEmpty() && !player.hasInfiniteMaterials()) {
            return false;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        float power = getPowerForTime(scaledCharge);
        if (power < 0.1F) return false;

        List<ItemStack> projectiles = ProjectileWeaponItem.draw(bowStack, ammoInInv, player);
        if (projectiles.isEmpty()) return false;

        if (level instanceof ServerLevel serverLevel) {
            this.shoot(
                    serverLevel,
                    player,
                    player.getUsedItemHand(),
                    bowStack,
                    projectiles,
                    power * 2.5F,
                    1.0F,
                    power == 1.0F,
                    null
            );
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.PLAYERS,
                1.0F, 1.2F);

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }finally {
            if (level.isClientSide()) {
                this.manybows$resetPullVisual(bowStack);
            }
        }
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem
                && (ammoStack.is(Items.SPECTRAL_ARROW) || ammoStack.is(Items.TIPPED_ARROW))) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {
            arrow = new CursedFlameArrow(level, shooter, weaponStack, ammoStack);

            if (arrow instanceof CursedFlameArrow cursedArrow) {
                var attrLookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
                var refOpt = attrLookup.get(RANGED_WEAPON_DAMAGE_ID);
                if (refOpt.isPresent()) {
                    AttributeInstance inst = shooter.getAttribute(refOpt.get());
                    if (inst != null) {
                        float damage = (float) inst.getValue();
                        cursedArrow.setBaseDamage(damage / 2.5F);
                    }
                }

                cursedArrow.setPowerMultiplier(getLastKnownPowerFallback(weaponStack));
            }
        }

        if (crit) arrow.setCritArrow(true);

        if (level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weaponStack, arrow, (item) -> {});
        }

        if (shooter instanceof Player player) {
            applyBowDamageAttribute(arrow, player);

            if (!level.isClientSide()) {
                double sigilCritBonus = 0.0D;
                float power = getLastKnownPowerFallback(weaponStack);

                if (power >= 0.25f) {
                    sigilCritBonus = Mth.lerp(power, 0.05D, 0.20D);
                    applySigilCritBonus(level, player, sigilCritBonus);
                }

                tryApplyBowCrit(arrow, player, 1.5D);
                clearSigilCritBonus(level, player);
            } else {
                tryApplyBowCrit(arrow, player, 1.5D);
            }
        }
        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }


    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) f = 1.0F;
        return f;
    }
    private static int rgb(float r, float g, float b) {
        int ri = Mth.clamp((int)(r * 255.0f), 0, 255);
        int gi = Mth.clamp((int)(g * 255.0f), 0, 255);
        int bi = Mth.clamp((int)(b * 255.0f), 0, 255);
        return (ri << 16) | (gi << 8) | bi;
    }


    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();

        if (!player.hasInfiniteMaterials() && !hasArrows) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, user, stack, remainingUseDuration);

        if (!level.isClientSide()) return;
        if (!(level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel)) return;
        if (!(user instanceof Player player)) return;

        if (player.getUseItem() != stack) return;

        int useDuration = this.getUseDuration(stack, user);
        int charge = useDuration - remainingUseDuration;
        float power = getPowerForTime(charge);

        if (power < 0.25f) return;

        spawnMysticalRitualParticles(clientLevel, player, power);
    }

    private static final DustParticleOptions MYSTIC_PURPLE =
            new DustParticleOptions(rgb(0.75f, 0.25f, 0.95f), 0.7f);

    private static final DustParticleOptions DEEP_PURPLE =
            new DustParticleOptions(rgb(0.45f, 0.15f, 0.65f), 0.6f);

    private static final DustParticleOptions BRIGHT_ACCENT =
            new DustParticleOptions(rgb(0.95f, 0.55f, 1.0f), 0.5f);

    private static final DustParticleOptions DARK_CORE =
            new DustParticleOptions(rgb(0.25f, 0.08f, 0.35f), 0.55f);


    private static void spawnMysticalRitualParticles(ClientLevel level, Player player, float power) {
        Vec3 playerPos = player.position().add(0, 0.1, 0);
        float time = player.tickCount * 0.05f;

        int complexity = (int) Mth.lerp(power, 3, 6);

        if (player.tickCount % 2 == 0) {
            drawGroundRitualCircles(level, playerPos, power, time);
        }

        if (player.tickCount % 2 == 0) {
            drawOrbitingGlyphs(level, player, power, time, complexity);
        }

        if (player.tickCount % 2 == 0) {
            drawAscendingHelixes(level, playerPos, power, time);
        }

        if (player.tickCount % 3 == 0 && power > 0.5f) {
            drawFloatingSymbols(level, player, power, time);
        }

        if (player.tickCount % 6 == 0 && power > 0.6f) {
            spawnGroundPulse(level, playerPos, power);
        }

        if (player.tickCount % 2 == 0 && power > 0.4f) {
            spawnMysticMist(level, player, power);
        }

        if (power > 0.9f && player.tickCount % 2 == 0) {
            drawPowerCrown(level, player, time);
        }

        if (power > 0.75f && player.tickCount % 3 == 0) {
            drawOuterEnergyRing(level, playerPos, power, time);
        }
    }

    private static void drawGroundRitualCircles(ClientLevel level, Vec3 center, float power, float time) {
        int numCircles = power > 0.6f ? 4 : 3;

        for (int circle = 0; circle < numCircles; circle++) {
            float radius = 1.3f + circle * 0.65f;
            int points = 20 + circle * 8;
            float rotation = time * (0.5f + circle * 0.3f) * (circle % 2 == 0 ? 1 : -1);

            for (int i = 0; i < points; i++) {
                if (i % 2 != (level.getGameTime() + circle) % 2) continue;

                float angle = (i / (float) points) * Mth.TWO_PI + rotation;
                double x = center.x + Mth.cos(angle) * radius;
                double z = center.z + Mth.sin(angle) * radius;
                double y = center.y + Mth.sin(i * 0.5f + time) * 0.06;

                DustParticleOptions particle = circle == 0 ? BRIGHT_ACCENT :
                        circle == 1 ? MYSTIC_PURPLE :
                                circle == 2 ? DEEP_PURPLE : MYSTIC_PURPLE;

                level.addParticle(particle, x, y, z, 0, 0, 0);
            }
        }

        if (level.getGameTime() % 4 == 0) {
            for (int i = 0; i < 4; i++) {
                float angle = i * Mth.HALF_PI + time * 0.2f;
                float radius = 2.2f;
                double x = center.x + Mth.cos(angle) * radius;
                double z = center.z + Mth.sin(angle) * radius;
                double y = center.y + 0.1;

                level.addParticle(BRIGHT_ACCENT, x, y, z, 0, 0.03, 0);
            }
        }
    }

    private static void drawOrbitingGlyphs(ClientLevel level, Player player, float power,
                                           float time, int numGlyphs) {
        Vec3 center = player.position().add(0, 1.0, 0);
        float orbitRadius = 1.6f;

        for (int i = 0; i < numGlyphs; i++) {
            float angle = (i / (float) numGlyphs) * Mth.TWO_PI + time;
            float heightOffset = Mth.sin(time * 2 + i) * 0.35f;

            double x = center.x + Mth.cos(angle) * orbitRadius;
            double z = center.z + Mth.sin(angle) * orbitRadius;
            double y = center.y + heightOffset;

            for (int j = 0; j < 6; j++) {
                double yOffset = (j - 2.5) * 0.09;
                level.addParticle(MYSTIC_PURPLE, x, y + yOffset, z, 0, 0, 0);

                if (j % 2 == 0) {
                    level.addParticle(DEEP_PURPLE, x + 0.06, y + yOffset, z, 0, 0, 0);
                    level.addParticle(DEEP_PURPLE, x - 0.06, y + yOffset, z, 0, 0, 0);
                }
            }

            for (int trail = 1; trail <= 2; trail++) {
                double trailX = center.x + Mth.cos(angle - 0.2f * trail) * orbitRadius;
                double trailZ = center.z + Mth.sin(angle - 0.2f * trail) * orbitRadius;
                level.addParticle(DARK_CORE, trailX, y, trailZ, 0, 0, 0);
            }
        }
    }

    private static void drawAscendingHelixes(ClientLevel level, Vec3 center, float power, float time) {
        int numHelixes = 2;

        for (int helix = 0; helix < numHelixes; helix++) {
            int points = 12; // More points
            float direction = helix == 0 ? 1 : -1;

            for (int i = 0; i < points; i++) {
                float t = (i / (float) points);
                float height = t * 2.8f;
                float angle = t * Mth.TWO_PI * 2.5f + time * direction;
                float radius = 0.9f + t * 0.45f;

                double x = center.x + Mth.cos(angle) * radius;
                double z = center.z + Mth.sin(angle) * radius;
                double y = center.y + height;

                DustParticleOptions particle = helix == 0 ? MYSTIC_PURPLE : DEEP_PURPLE;

                level.addParticle(particle, x, y, z, 0, 0.015, 0);
            }
        }
    }

    private static void drawFloatingSymbols(ClientLevel level, Player player, float power, float time) {
        Vec3 center = player.position().add(0, 2.6, 0);

        int numSymbols = (int) (4 + power);

        for (int i = 0; i < numSymbols; i++) {
            float angle = (i / (float) numSymbols) * Mth.TWO_PI + time * 0.5f;
            float radius = 0.65f;
            float bob = Mth.sin(time * 2 + i * 2) * 0.18f;

            double x = center.x + Mth.cos(angle) * radius;
            double z = center.z + Mth.sin(angle) * radius;
            double y = center.y + bob;

            level.addParticle(BRIGHT_ACCENT, x, y, z, 0, 0, 0);
            level.addParticle(MYSTIC_PURPLE, x + 0.1, y, z, 0, 0, 0);
            level.addParticle(MYSTIC_PURPLE, x - 0.1, y, z, 0, 0, 0);
            level.addParticle(MYSTIC_PURPLE, x, y + 0.1, z, 0, 0, 0);
            level.addParticle(MYSTIC_PURPLE, x, y - 0.1, z, 0, 0, 0);
        }
    }

    private static void spawnGroundPulse(ClientLevel level, Vec3 center, float power) {
        int numRings = 3;

        for (int ring = 0; ring < numRings; ring++) {
            float radius = 0.6f + ring * 0.6f;
            int points = 28; // More particles

            for (int i = 0; i < points; i++) {
                float angle = (i / (float) points) * Mth.TWO_PI;
                double x = center.x + Mth.cos(angle) * radius;
                double z = center.z + Mth.sin(angle) * radius;
                double y = center.y;

                double vx = Mth.cos(angle) * 0.1;
                double vz = Mth.sin(angle) * 0.1;

                level.addParticle(MYSTIC_PURPLE, x, y, z, vx, 0, vz);
            }
        }
    }

    private static void spawnMysticMist(ClientLevel level, Player player, float power) {
        Vec3 center = player.position().add(0, 1.0, 0);

        for (int i = 0; i < 5; i++) {
            double angle = level.random.nextFloat() * Mth.TWO_PI;
            double radius = 1.0 + level.random.nextFloat() * 1.0;

            double x = center.x + Mth.cos((float) angle) * radius;
            double z = center.z + Mth.sin((float) angle) * radius;
            double y = center.y + (level.random.nextFloat() - 0.5) * 1.8;

            double vx = (center.x - x) * 0.012;
            double vy = 0.025;
            double vz = (center.z - z) * 0.012;

            level.addParticle(DARK_CORE, x, y, z, vx, vy, vz);
        }
    }

    private static void drawPowerCrown(ClientLevel level, Player player, float time) {
        Vec3 center = player.position().add(0, 2.3, 0);

        int points = 16;
        float radius = 0.6f;

        for (int i = 0; i < points; i++) {
            if (i % 2 != 0) continue;

            float angle = (i / (float) points) * Mth.TWO_PI;
            float heightVariation = (i % 3 == 0) ? 0.2f : 0;

            double x = center.x + Mth.cos(angle) * radius;
            double z = center.z + Mth.sin(angle) * radius;
            double y = center.y + heightVariation;

            level.addParticle(BRIGHT_ACCENT, x, y, z, 0, 0.04, 0);

            double ix = center.x + Mth.cos(angle) * radius * 0.5f;
            double iz = center.z + Mth.sin(angle) * radius * 0.5f;
            level.addParticle(MYSTIC_PURPLE, ix, y, iz, 0, 0, 0);
        }
    }

    private static void drawOuterEnergyRing(ClientLevel level, Vec3 center, float power, float time) {
        float radius = 3.0f + Mth.sin(time * 2) * 0.2f;
        int points = 32;

        for (int i = 0; i < points; i++) {
            if (i % 3 != (int)(time * 10) % 3) continue;

            float angle = (i / (float) points) * Mth.TWO_PI + time * 0.8f;
            double x = center.x + Mth.cos(angle) * radius;
            double z = center.z + Mth.sin(angle) * radius;
            double y = center.y + Mth.sin(i * 0.3f + time * 3) * 0.15;

            level.addParticle(BRIGHT_ACCENT, x, y, z, 0, 0, 0);
        }
    }

    private static void applySigilCritBonus(Level level, Player player, double bonus) {
        var critHolderOpt = level.registryAccess()
                .lookupOrThrow(Registries.ATTRIBUTE)
                .get(BuiltInRegistries.ATTRIBUTE.getKey(AttributesRegistry.BOW_CRIT_CHANCE.get()));

        if (critHolderOpt.isEmpty()) return;

        var inst = player.getAttribute(critHolderOpt.get());
        if (inst == null) return;

        inst.removeModifier(CURSED_FLAME_SIGIL_CRIT_ID);

        inst.addTransientModifier(new AttributeModifier(
                CURSED_FLAME_SIGIL_CRIT_ID,
                bonus,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    private static void clearSigilCritBonus(Level level, Player player) {
        var critHolderOpt = level.registryAccess()
                .lookupOrThrow(Registries.ATTRIBUTE)
                .get(BuiltInRegistries.ATTRIBUTE.getKey(AttributesRegistry.BOW_CRIT_CHANCE.get()));

        if (critHolderOpt.isEmpty()) return;

        var inst = player.getAttribute(critHolderOpt.get());
        if (inst == null) return;

        inst.removeModifier(CURSED_FLAME_SIGIL_CRIT_ID);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.accept(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip.ability")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip.legend")
                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return Minecraft.getInstance().hasShiftDown();
    }

    private float getLastKnownPowerFallback(ItemStack weaponStack) {
        return 1.0F;
    }
}
