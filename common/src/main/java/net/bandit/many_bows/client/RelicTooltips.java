package net.bandit.many_bows.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


public final class RelicTooltips {
    public static final ThreadLocal<String> ACTIVE_DECORATION = new ThreadLocal<>();
    public static String decoration(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals("too_many_bows")) return null;
        return switch (id.getPath()) {
            case "eventide", "worldeater", "godsplitter" -> "mythic_animated";
            case "heart_of_the_wyrm", "wyrm_effigy" -> "mythic";
            case "vaultpiercer", "gravewire_bow", "soulhoard", "dusk_reaper", "crimson_nexus", "dragons_breath" -> "legendary_animated";
            case "blunted_edge", "dormant_celestial_bow", "ancient_sage_bow", "arc_heavens", "sentinels_wrath" -> "legendary";
            case "celestial_limb", "celestial_string", "celestial_grip", "cursed_stone", "soul_fragment", "rift_shard", "power_crystal" -> "collectible";
            case "wind_glove", "sharpshot_ring", "stormbound_signet", "fletchers_talisman", "dead_eyes_pendant" -> "cosmetic";
            case "hunter_bow", "ethereal_hunter", "verdant_viper", "verdant_vigor", "vitality_weaver", "soul_lantern" -> "pet";
            case "arcane_bow", "astral_bound", "auroras_grace", "spectral_whisper", "necro_flame_bow", "demons_grasp", "cursed_lantern", "twin_shadows" -> "epic";
            case "frostbite", "cyroheart_bow", "tidal_bow", "wind_bow", "dark_bow", "shulker_blast", "beacon_beam_bow" -> "rare";
            default -> "common";
        };
    }

    public static void drawBackground(GuiGraphics graphics, int x, int y, int width, int height, int z, String design) {
        graphics.pose().pushPose(); graphics.pose().translate(0, 0, z);
        graphics.fill(x, y, x + width, y + height, 0xB0100D18);
        graphics.flush();
        RenderSystem.setShaderColor(1, 1, 1, 0.30F);
        try {
            graphics.blitSprite(sprite("tooltip/" + design + "_background"), x - 12, y - 12, width + 24, height + 24);
        }
        finally {
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        graphics.blitSprite(sprite("tooltip/" + design + "_frame"), x - 12, y - 12, width + 24, height + 24);
        graphics.pose().popPose();
    }

    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath("too_many_bows", path);
    }
}
