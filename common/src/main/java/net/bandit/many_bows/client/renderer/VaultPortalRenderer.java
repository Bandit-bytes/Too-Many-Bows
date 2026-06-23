package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.VaultPortalEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * Temporary 26.1.2-safe renderer.
 *
 * The old custom quad renderer used the pre-render-state EntityRenderer API.
 * This keeps the entity registered and compile-safe while the visual renderer
 * can be reworked against the 26.1 render-state pipeline.
 */
public class VaultPortalRenderer extends NoopRenderer<VaultPortalEntity> {

    public VaultPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
