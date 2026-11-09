package net.bandit.many_bows.config;

import java.util.function.Supplier;

public record ConfigEntry<T>(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
}