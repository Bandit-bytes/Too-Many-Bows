package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.relic.RelicData;
import net.bandit.many_bows.relic.RelicQuests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
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
    public void releaseUsing(
            ItemStack bow,
            Level level,
            LivingEntity entity,
            int remaining
    ) {
        if (!(level instanceof ServerLevel server)
                || !(entity instanceof ServerPlayer player)) {
            return;
        }

        long time = level.getDayTime() % 24000;
        float power = getPowerForTime(getUseDuration(bow, entity) - remaining);

        if (level.dimension() != Level.OVERWORLD
                || time < 17500
                || time > 18500
                || player.getY() < level.getMaxBuildHeight() - 10
                || player.getXRot() > -85
                || !level.canSeeSky(player.blockPosition())
                || power < 1) {
            player.displayClientMessage(
                    Component.literal(
                            "Draw at the world's ceiling at midnight. "
                                    + "Aim straight into the stars."
                    ),
                    true
            );
            return;
        }

        if (RelicData.read(bow).getBoolean("StarCalled")) {
            return;
        }

        ItemStack ammo = player.getProjectile(bow);

        if (ammo.isEmpty() && !player.getAbilities().instabuild) {
            return;
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
            player.displayClientMessage(
                    Component.literal(
                            "The stars need open, explored land below you. Try again."
                    ),
                    true
            );
            return;
        }

        if (!player.getAbilities().instabuild) {
            ammo.shrink(1);
        }

        final BlockPos destination = star;

        RelicData.edit(bow, data -> {
            data.putBoolean("StarCalled", true);
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

        RelicQuests.award(player, "something_answered");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack bow = player.getItemInHand(hand);
        var data = RelicData.read(bow);

        if (!level.isClientSide
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

                return InteractionResultHolder.success(
                        player.getItemInHand(hand)
                );
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> lines,
            TooltipFlag flag
    ) {
        lines.add(
                Component.literal(
                        "Midnight. World ceiling. One arrow into the stars."
                )
        );

        var data = RelicData.read(stack);

        if (data.getBoolean("StarCalled")) {
            lines.add(
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