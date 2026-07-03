package net.bandit.many_bows.mixin;

import net.bandit.many_bows.compat.OriginsQuiverCompat;
import net.bandit.many_bows.item.ModBowItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerProjectileMixin {

    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private void tooManyBows$useOriginsQuiver(
            ItemStack weaponStack,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!cir.getReturnValue().isEmpty()) {
            return;
        }

        if (!(weaponStack.getItem() instanceof ModBowItem bow)) {
            return;
        }

        Player player = (Player) (Object) this;
        ItemStack quiverProjectile = OriginsQuiverCompat.findProjectile(
                player,
                bow.getAllSupportedProjectiles()
        );

        if (!quiverProjectile.isEmpty()) {
            cir.setReturnValue(quiverProjectile);
        }
    }
}
