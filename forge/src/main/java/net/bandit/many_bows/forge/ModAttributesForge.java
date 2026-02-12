package net.bandit.many_bows.forge;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributesForge {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AttributesRegistry.BOW_DRAW_SPEED.get(), 1.0D);
        event.add(EntityType.PLAYER, AttributesRegistry.BOW_DAMAGE.get(), 1.0D);
        event.add(EntityType.PLAYER, AttributesRegistry.BOW_CRIT_CHANCE.get(), 0.0D);
    }
}
