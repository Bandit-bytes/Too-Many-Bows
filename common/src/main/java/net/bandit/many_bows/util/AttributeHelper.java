package net.bandit.many_bows.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up optional attributes from other mods (e.g. ranged_weapon:damage)
 * without a hard dependency.
 */
public final class AttributeHelper {

    private AttributeHelper() {
    }

    /**
     * @return the entity's value for the given attribute, or null when the
     * attribute is not registered or the entity does not have it.
     */
    @Nullable
    public static Double getValue(@Nullable LivingEntity entity, String namespace, String path) {
        if (entity == null) {
            return null;
        }

        ResourceLocation id = ResourceLocation.tryBuild(namespace, path);
        if (id == null) {
            return null;
        }

        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attribute == null) {
            return null;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : null;
    }
}
