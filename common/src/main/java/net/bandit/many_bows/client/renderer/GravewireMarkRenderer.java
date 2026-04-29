package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.GravewireMarkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * Temporary 1.21.11-safe renderer.
 *
 * The old custom quad renderer used the pre-render-state EntityRenderer API.
 * This keeps the entity registered and compile-safe while the visual renderer
 * can be reworked against the 1.21.11 render-state pipeline.
 */
public class GravewireMarkRenderer extends NoopRenderer<GravewireMarkEntity> {

    public GravewireMarkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
