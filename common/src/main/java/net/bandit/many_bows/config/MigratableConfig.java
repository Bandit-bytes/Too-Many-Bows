package net.bandit.many_bows.config;

/**
 * A JSON config that can upgrade values saved by an older mod version.
 * Called after every load; return true if anything changed so the file is rewritten.
 */
public interface MigratableConfig {
    boolean migrate();
}
