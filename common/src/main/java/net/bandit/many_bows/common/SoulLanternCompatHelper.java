package net.bandit.many_bows.common;

import dev.architectury.platform.Platform;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.lang.reflect.Method;
import java.util.Optional;

public final class SoulLanternCompatHelper {

    private SoulLanternCompatHelper() {
    }

    public static boolean isSoulLanternEquipped(Player player) {
        if (player == null) {
            return false;
        }

        Item soulLantern = ItemRegistry.SOUL_LANTERN.get();

        // Fabric / Trinkets
        if (Platform.isModLoaded("trinkets")) {
            try {
                Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
                Method getTrinketComponent = trinketsApiClass.getMethod("getTrinketComponent", net.minecraft.world.entity.LivingEntity.class);

                Object result = getTrinketComponent.invoke(null, player);
                if (result instanceof Optional<?> optional && optional.isPresent()) {
                    Object component = optional.get();
                    Method isEquipped = component.getClass().getMethod("isEquipped", Item.class);
                    Object equipped = isEquipped.invoke(component, soulLantern);

                    if (equipped instanceof Boolean b && b) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // NeoForge / Curios
        if (Platform.isModLoaded("curios")) {
            try {
                Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getCuriosInventory = curiosApiClass.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);

                Object result = getCuriosInventory.invoke(null, player);
                if (result instanceof Optional<?> optional && optional.isPresent()) {
                    Object inventory = optional.get();
                    Method isEquipped = inventory.getClass().getMethod("isEquipped", Item.class);
                    Object equipped = isEquipped.invoke(inventory, soulLantern);

                    if (equipped instanceof Boolean b && b) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        return false;
    }
}