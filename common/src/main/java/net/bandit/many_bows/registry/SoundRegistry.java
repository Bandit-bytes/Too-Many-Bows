package net.bandit.many_bows.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ManyBowsMod.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> VAULT_PORTAL_OPEN =
            SOUND_EVENTS.register("vault_portal_open",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "vault_portal_open")
                    ));

    public static final RegistrySupplier<SoundEvent> VAULT_PORTAL_FIRE =
            SOUND_EVENTS.register("vault_portal_fire",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "vault_portal_fire")
                    ));

    public static final RegistrySupplier<SoundEvent> VAULT_PORTAL_IMPACT =
            SOUND_EVENTS.register("vault_portal_impact",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "vault_portal_impact")
                    ));

    public static final RegistrySupplier<SoundEvent> GRAVEWIRE_FIRE =
            SOUND_EVENTS.register("gravewire_fire",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "gravewire_fire")
                    ));

    public static final RegistrySupplier<SoundEvent> WINDBOW_FIRE =
            SOUND_EVENTS.register("windbow_fire",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "windbow_fire")
                    ));


    public static final RegistrySupplier<SoundEvent> SOULHOARD_RELEASE =
            SOUND_EVENTS.register("soulhoard_release",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "soulhoard_release")
                    ));

    public static final RegistrySupplier<SoundEvent> SOULHOARD_HARVEST =
            SOUND_EVENTS.register("soulhoard_harvest",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "soulhoard_harvest")
                    ));

    public static void register() {
        SOUND_EVENTS.register();
    }
}