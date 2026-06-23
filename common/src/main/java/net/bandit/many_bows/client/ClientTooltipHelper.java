package net.bandit.many_bows.client;

import net.minecraft.client.Minecraft;

/**
 * Keeps direct Minecraft client references out of item classes that are also
 * loaded by dedicated servers.
 */
public final class ClientTooltipHelper {
    private ClientTooltipHelper() {
    }

    public static boolean hasShiftDown() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.screen != null && minecraft.hasShiftDown();
    }
}
