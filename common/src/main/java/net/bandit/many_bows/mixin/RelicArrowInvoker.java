package net.bandit.many_bows.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(AbstractArrow.class)
public interface RelicArrowInvoker {
    @Invoker("setPierceLevel")
    void tmb$setPierceLevel(byte level);
}
