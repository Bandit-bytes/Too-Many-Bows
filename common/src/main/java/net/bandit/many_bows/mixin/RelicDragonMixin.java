package net.bandit.many_bows.mixin;

import net.bandit.many_bows.relic.*;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragon.class)
public abstract class RelicDragonMixin {
    @Inject(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"))
    private void tmb$wyrmHeart(EnderDragonPart part, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EnderDragon dragon = (EnderDragon)(Object)this;
        if (!(source.getEntity() instanceof ServerPlayer player) || !(source.getDirectEntity()
                instanceof AbstractArrow) || dragon.getHealth() > dragon.getMaxHealth() * 0.2f
                || !dragon.getPhaseManager().getCurrentPhase().isSitting() || dragon.getTags().contains("tmb_wyrm_heart_claimed"))
            return;

        if (!player.getMainHandItem().is(Items.BOW) && !player.getOffhandItem().is(Items.BOW))
            return;

        if (dragon.getDragonFight() == null || !dragon.getDragonFight().hasPreviouslyKilledDragon())
            return;

        for (ItemStack effigy : player.getInventory().items) if (effigy.is(ItemRegistry.WYRM_EFFIGY.get()) && RelicData.read(effigy).getBoolean("HuntBound")) {
            effigy.shrink(1); dragon.addTag("tmb_wyrm_heart_claimed"); RelicQuests.give(player,
                    new ItemStack(ItemRegistry.HEART_OF_THE_WYRM.get())); RelicQuests.award(player, "heart_of_the_wyrm");
                    return;
        }
    }
}
