package net.bandit.many_bows.compat;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * Optional compatibility for Apoli/Origins inventory powers.
 *
 * The integration uses reflection so Origins is not a required dependency of
 * Too Many Bows. When Origins is present, arrows stored in an active
 * origins:inventory power can be returned as ammunition for this mod's bows.
 */
public final class OriginsQuiverCompat {

    private static final String POWER_HOLDER_CLASS =
            "io.github.apace100.apoli.component.PowerHolderComponent";
    private static final String INVENTORY_POWER_CLASS =
            "io.github.apace100.apoli.power.InventoryPower";

    private static final Map<ItemStack, QuiverSlot> TRACKED_STACKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Class<?> inventoryPowerClass;
    private static Method getPowersMethod;

    private OriginsQuiverCompat() {
    }

    /**
     * Finds the first supported arrow in any active Origins inventory power.
     */
    public static ItemStack findProjectile(
            Player player,
            Predicate<ItemStack> supportedProjectiles
    ) {
        for (Object power : getInventoryPowers(player)) {
            if (!(power instanceof Container container)) {
                continue;
            }

            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack candidate = container.getItem(slot);
                if (isSupportedArrow(candidate, supportedProjectiles)) {
                    TRACKED_STACKS.put(candidate, new QuiverSlot(container, slot));
                    return candidate;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Marks the owning Origins inventory power dirty after a bow directly
     * shrinks the returned ItemStack.
     */
    public static void onTrackedStackShrunk(ItemStack stack) {
        QuiverSlot source = TRACKED_STACKS.remove(stack);
        if (source == null) {
            return;
        }

        ItemStack current = source.container().getItem(source.slot());
        if (current.isEmpty()) {
            source.container().setItem(source.slot(), ItemStack.EMPTY);
        } else {
            source.container().setChanged();
        }
    }

    public static int countProjectiles(
            Player player,
            Predicate<ItemStack> supportedProjectiles
    ) {
        int total = 0;

        for (Object power : getInventoryPowers(player)) {
            if (!(power instanceof Container container)) {
                continue;
            }

            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack candidate = container.getItem(slot);
                if (isSupportedArrow(candidate, supportedProjectiles)) {
                    total += candidate.getCount();
                }
            }
        }

        return total;
    }

    /**
     * Removes up to {@code requested} arrows from Origins inventory powers and
     * returns the amount actually removed.
     */
    public static int consumeProjectiles(
            Player player,
            Predicate<ItemStack> supportedProjectiles,
            int requested
    ) {
        if (requested <= 0) {
            return 0;
        }

        int removed = 0;

        for (Object power : getInventoryPowers(player)) {
            if (!(power instanceof Container container)) {
                continue;
            }

            for (int slot = 0;
                 slot < container.getContainerSize() && removed < requested;
                 slot++) {
                ItemStack candidate = container.getItem(slot);
                if (!isSupportedArrow(candidate, supportedProjectiles)) {
                    continue;
                }

                int amount = Math.min(candidate.getCount(), requested - removed);
                ItemStack extracted = container.removeItem(slot, amount);
                removed += extracted.getCount();
            }

            if (removed >= requested) {
                break;
            }
        }

        return removed;
    }

    private static boolean isSupportedArrow(
            ItemStack stack,
            Predicate<ItemStack> supportedProjectiles
    ) {
        return !stack.isEmpty()
                && stack.getItem() instanceof ArrowItem
                && supportedProjectiles.test(stack);
    }

    private static Collection<?> getInventoryPowers(Player player) {
        initialize();
        if (!available) {
            return Collections.emptyList();
        }

        try {
            Object result = getPowersMethod.invoke(null, player, inventoryPowerClass);
            return result instanceof Collection<?> collection
                    ? collection
                    : Collections.emptyList();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Collections.emptyList();
        }
    }

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        try {
            Class<?> powerHolderClass = Class.forName(POWER_HOLDER_CLASS);
            inventoryPowerClass = Class.forName(INVENTORY_POWER_CLASS);

            for (Method method : powerHolderClass.getMethods()) {
                if (method.getName().equals("getPowers")
                        && Modifier.isStatic(method.getModifiers())
                        && method.getParameterCount() == 2
                        && method.getParameterTypes()[1] == Class.class) {
                    getPowersMethod = method;
                    break;
                }
            }

            available = getPowersMethod != null;
        } catch (ClassNotFoundException | LinkageError ignored) {
            available = false;
        }
    }

    private record QuiverSlot(Container container, int slot) {
    }
}
