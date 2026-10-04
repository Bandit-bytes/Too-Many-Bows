package net.bandit.many_bows.mixin;

import net.bandit.many_bows.client.RelicTooltips;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class RelicTooltipMixin {
    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private void tmb$begin(Font font, ItemStack item, int x, int y, CallbackInfo ci) {
        String design = RelicTooltips.decoration(item);
        if (design == null) RelicTooltips.ACTIVE_DECORATION.remove();
        else RelicTooltips.ACTIVE_DECORATION.set(design);
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
    private void tmb$end(Font font, ItemStack item, int x, int y, CallbackInfo ci) { RelicTooltips.ACTIVE_DECORATION.remove(); }
}
