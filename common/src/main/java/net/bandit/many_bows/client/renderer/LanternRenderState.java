package net.bandit.many_bows.client.renderer;

public final class LanternRenderState {

    private static final ThreadLocal<Boolean> EQUIPPED_RENDER =
            ThreadLocal.withInitial(() -> false);

    private LanternRenderState() {
    }

    public static void beginEquippedRender() {
        EQUIPPED_RENDER.set(true);
    }

    public static void endEquippedRender() {
        EQUIPPED_RENDER.set(false);
    }

    public static boolean isEquippedRender() {
        return EQUIPPED_RENDER.get();
    }
}