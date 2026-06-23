package net.bandit.many_bows.common;

import net.bandit.many_bows.entity.LightOrbEntity;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

public final class LanternLightHelper {

    private LanternLightHelper() {
    }

    public static void ensureLanternLight(Player player, int lightLevel) {
        if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int clampedLight = Mth.clamp(lightLevel, 0, 15);
        if (clampedLight <= 0) {
            return;
        }

        UUID owner = player.getUUID();

        List<LightOrbEntity> existing = serverLevel.getEntitiesOfClass(
                LightOrbEntity.class,
                player.getBoundingBox().inflate(6.0D),
                orb -> owner.equals(orb.getOwnerUUID())
        );

        if (!existing.isEmpty()) {
            LightOrbEntity orb = existing.get(0);
            orb.setLightLevel(clampedLight);
            orb.refreshLifetime();
            return;
        }

        LightOrbEntity orb = new LightOrbEntity(EntityRegistry.LIGHT_ORB.get(), serverLevel);
        orb.setOwnerUUID(owner);
        orb.setLightLevel(clampedLight);
        orb.refreshLifetime();
        orb.setPos(player.getX(), player.getY() + 1.0D, player.getZ());

        serverLevel.addFreshEntity(orb);
    }
}