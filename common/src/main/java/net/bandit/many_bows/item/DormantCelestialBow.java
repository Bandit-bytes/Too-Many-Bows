package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.registry.RelicParticleRegistry;
import net.bandit.many_bows.relic.CelestialEffects;
import net.bandit.many_bows.relic.RelicData;
import net.bandit.many_bows.relic.RelicQuests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DormantCelestialBow extends BowItem {

    public DormantCelestialBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(
            ItemStack bow,
            Level level,
            LivingEntity entity,
            int remaining
    ) {
        if (!(level instanceof ServerLevel server)
                || !(entity instanceof ServerPlayer player)) {
            return false;
        }

        long time = level.getOverworldClockTime() % 24000;
        float power = getPowerForTime(getUseDuration(bow, entity) - remaining);

        if (level.dimension() != Level.OVERWORLD
                || time < 17500
                || time > 18500
                || player.getY() < level.getMaxY() + 1 - 10
                || player.getXRot() > -85
                || !level.canSeeSky(player.blockPosition())
                || power < 1) {
            RelicQuests.message(player, 
                    Component.literal(
                            "Draw at the world's ceiling at midnight. "
                                    + "Aim straight into the stars."
                    ),
                    true
            );
            return false;
        }

        if (RelicData.read(bow).getBoolean("StarCalled")) {
            return false;
        }

        ItemStack ammo = player.getProjectile(bow);

        if (ammo.isEmpty() && !player.getAbilities().instabuild) {
            return false;
        }

        // Search only loaded terrain to avoid generating distant chunks.
        BlockPos star = null;

        for (int attempt = 0; attempt < 32; attempt++) {
            int x = player.getBlockX() + player.getRandom().nextInt(193) - 96;
            int z = player.getBlockZ() + player.getRandom().nextInt(193) - 96;

            if (!server.hasChunkAt(new BlockPos(x, 0, z))) {
                continue;
            }

            int y = server.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    x,
                    z
            );

            BlockPos candidate = new BlockPos(x, y, z);

            if (server.getWorldBorder().isWithinBounds(candidate)) {
                star = candidate;
                break;
            }
        }

        if (star == null) {
            RelicQuests.message(player, 
                    Component.literal(
                            "The stars need open, explored land below you. Try again."
                    ),
                    true
            );
            return false;
        }

        if (!player.getAbilities().instabuild) {
            ammo.shrink(1);
        }

        final BlockPos destination = star;

        RelicData.edit(bow, data -> {
            data.putBoolean("StarCalled", true);
            data.putLong("StarVisualTime", level.getGameTime());
            data.putInt("StarX", destination.getX());
            data.putInt("StarY", destination.getY());
            data.putInt("StarZ", destination.getZ());
        });

        player.sendSystemMessage(
                Component.literal(
                        "Something answered. Falling Star: "
                                + star.getX() + ", "
                                + star.getY() + ", "
                                + star.getZ()
                )
        );

        CelestialEffects.ritual(server, player, Vec3.atBottomCenterOf(destination));
        RelicQuests.award(player, "something_answered");
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        ServerLevel server = level;
        if (!(entity instanceof ServerPlayer player)
                || slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND || level.dimension() != Level.OVERWORLD
                || level.getGameTime() % 40 != 0) return;
        var data = RelicData.read(stack);
        if (!data.getBoolean("StarCalled")) return;
        Vec3 landing = new Vec3(data.getInt("StarX") + 0.5,
                data.getInt("StarY"), data.getInt("StarZ") + 0.5);
        // Delay the landing marker until the comet has completed its five-second descent.
        if (data.contains("StarVisualTime")
                && level.getGameTime() - data.getLong("StarVisualTime") < 100) return;
        if (player.position().distanceToSqr(landing) <= 512 * 512) {
            CelestialEffects.sendTo(server, player, RelicParticleRegistry.STAR_BEACON.get(),
                    landing, Vec3.ZERO);
        }
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack bow = player.getItemInHand(hand);
        var data = RelicData.read(bow);

        if (!level.isClientSide()
                && level.dimension() == Level.OVERWORLD
                && data.getBoolean("StarCalled")) {
            BlockPos star = new BlockPos(
                    data.getInt("StarX"),
                    data.getInt("StarY"),
                    data.getInt("StarZ")
            );

            if (player.distanceToSqr(Vec3.atCenterOf(star)) < 36) {
                bow.shrink(1);
                player.setItemInHand(
                        hand,
                        new ItemStack(ItemRegistry.EVENTIDE.get())
                );

                if (player instanceof ServerPlayer serverPlayer) {
                    RelicQuests.award(serverPlayer, "eventide");
                }

                ((ServerLevel) level).sendParticles(
                        ParticleTypes.END_ROD,
                        player.getX(),
                        player.getY() + 1,
                        player.getZ(),
                        80,
                        1,
                        1,
                        1,
                        0.2
                );

                player.sendSystemMessage(
                        Component.literal("EVENTIDE AWAKENS")
                );

                return InteractionResult.SUCCESS;
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<Component> lines,
            TooltipFlag flag
    ) {
        lines.accept(
                Component.literal(
                        "Midnight. World ceiling. One arrow into the stars."
                )
        );

        var data = RelicData.read(stack);

        if (data.getBoolean("StarCalled")) {
            lines.accept(
                    Component.literal(
                            "Falling Star: "
                                    + data.getInt("StarX") + ", "
                                    + data.getInt("StarY") + ", "
                                    + data.getInt("StarZ")
                    )
            );
        }
    }
}