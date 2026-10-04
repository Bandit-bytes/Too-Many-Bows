package net.bandit.many_bows.neoforge.client;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.RelicTooltips;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Render supplied artwork and normal tooltip components in one pass.
 * Does not depend on a Color event or a particular vanilla background overload. */
@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ManyBowsTooltipDecorations {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void render(RenderTooltipEvent.Pre event) {
        String design = RelicTooltips.decoration(event.getItemStack());
        if (design == null || event.getComponents().isEmpty()) return;
        int width = 0, height = event.getComponents().size() == 1 ? -2 : 0;
        for (var component : event.getComponents()) {
            width = Math.max(width, component.getWidth(event.getFont()));
            height += component.getHeight();
        }
        var position = event.getTooltipPositioner().positionTooltip(event.getScreenWidth(), event.getScreenHeight(), event.getX(), event.getY(), width, height);
        int x = Math.max(12, Math.min(position.x(), event.getScreenWidth() - width - 12));
        int y = Math.max(12, Math.min(position.y(), event.getScreenHeight() - height - 12));
        event.setCanceled(true);
        RelicTooltips.ACTIVE_DECORATION.remove();
        var graphics = event.getGraphics();
        graphics.flush();
        RelicTooltips.drawBackground(graphics, x, y, width, height, 400, design);
        graphics.pose().pushPose(); graphics.pose().translate(0, 0, 400);
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        int cursor = y;
        for (int i = 0; i < event.getComponents().size(); i++) {
            var component = event.getComponents().get(i);
            component.renderText(event.getFont(), x, cursor, graphics.pose().last().pose(), buffers);
            cursor += component.getHeight() + (i == 0 ? 2 : 0);
        }
        buffers.endBatch();
        cursor = y;
        for (int i = 0; i < event.getComponents().size(); i++) {
            var component = event.getComponents().get(i);
            component.renderImage(event.getFont(), x, cursor, graphics);
            cursor += component.getHeight() + (i == 0 ? 2 : 0);
        }
        graphics.flush(); graphics.pose().popPose();
    }
}
