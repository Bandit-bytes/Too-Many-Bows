package net.bandit.many_bows.client;

import net.minecraft.client.Minecraft;


public final class ClientTooltipHelper {

    private ClientTooltipHelper() {
    }

    public static boolean hasShiftDown() {
        Minecraft minecraft = Minecraft.getInstance();

        return minecraft != null
                && minecraft.gui.screen() != null
                && minecraft.hasShiftDown();
    }
}