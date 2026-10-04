package net.bandit.many_bows.mixin;

import net.bandit.many_bows.relic.RelicQuests;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class RelicPlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void tmb$relicTick(CallbackInfo ci) {
        RelicQuests.playerTick((ServerPlayer)(Object)this);
    }
}
