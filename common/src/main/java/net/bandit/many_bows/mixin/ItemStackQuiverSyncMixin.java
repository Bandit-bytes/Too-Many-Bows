package net.bandit.many_bows.mixin;

import net.bandit.many_bows.compat.OriginsQuiverCompat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackQuiverSyncMixin {

    @Inject(method = "shrink", at = @At("TAIL"))
    private void tooManyBows$syncOriginsQuiver(int amount, CallbackInfo ci) {
        OriginsQuiverCompat.onTrackedStackShrunk((ItemStack) (Object) this);
    }
}
