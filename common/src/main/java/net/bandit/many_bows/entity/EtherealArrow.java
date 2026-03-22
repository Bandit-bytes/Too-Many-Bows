package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.EtherealHunterBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EtherealArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "ethereal_hunter";

    private boolean hasHit = false;
    private int hitTimer = 0;

    public EtherealArrow(EntityType<? extends EtherealArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public EtherealArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ETHEREAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static EtherealHunterBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, EtherealHunterBowConfig.class, EtherealHunterBowConfig::new);
    }

    private void applyConfiguredValues() {
        EtherealHunterBowConfig config = config();

        this.setNoGravity(config.no_gravity);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    public void tick() {
        super.tick();

        EtherealHunterBowConfig config = config();

        if (hasHit && config.discard_after_hit_delay) {
            hitTimer++;
            if (hitTimer >= Math.max(0, config.hit_discard_delay_ticks)) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && config().start_hit_timer_on_entity_hit) {
            this.hasHit = true;
            this.hitTimer = 0;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide() && config().start_hit_timer_on_block_hit) {
            this.hasHit = true;
            this.hitTimer = 0;
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        return config().allow_pickup && super.tryPickup(player);
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}