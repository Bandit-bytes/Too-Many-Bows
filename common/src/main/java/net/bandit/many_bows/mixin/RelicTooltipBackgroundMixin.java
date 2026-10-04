package net.bandit.many_bows.mixin;

import net.bandit.many_bows.client.RelicTooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipRenderUtil.class)
public abstract class RelicTooltipBackgroundMixin {
    @Inject(method = "renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V", at = @At("HEAD"), cancellable = true)
    private static void tmb$background(GuiGraphics graphics, int x, int y, int width, int height, int z, CallbackInfo ci) {
        String design = RelicTooltips.ACTIVE_DECORATION.get();
        if (design == null) return;
        RelicTooltips.drawBackground(graphics, x, y, width, height, z, design); ci.cancel();
    }
}
