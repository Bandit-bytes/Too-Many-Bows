package net.bandit.many_bows.fabric;

import net.bandit.many_bows.registry.AttributesRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public class ModAttributesFabric {

    public static void init() {
        FabricDefaultAttributeRegistry.register(
                EntityType.PLAYER,
                Player.createAttributes()
                        .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DRAW_SPEED.get()), 1.0D)
                        .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.BOW_DAMAGE.get()), 1.0D)
        );
    }
}
