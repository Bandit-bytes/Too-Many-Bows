package net.bandit.many_bows.registry;

import net.bandit.many_bows.enchantment.RicochetEnchantment;
import net.bandit.many_bows.enchantment.SpectralEnchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import static net.bandit.many_bows.ManyBowsMod.MOD_ID;

public class EnchantmentRegistry {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(MOD_ID, Registries.ENCHANTMENT);

    public static final RegistrySupplier<Enchantment> RICOCHET = ENCHANTMENTS.register("ricochet",
            () -> new RicochetEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final RegistrySupplier<Enchantment> SPECTRAL = ENCHANTMENTS.register("spectral",
            SpectralEnchantment::new);

    public static void register() {
        ENCHANTMENTS.register();
    }
}
