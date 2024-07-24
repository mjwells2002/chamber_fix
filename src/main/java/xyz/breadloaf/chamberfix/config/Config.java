package xyz.breadloaf.chamberfix.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class Config {
    public ConfigEntry<Long> resetTimeTicks;

    public Config(ConfigBuilder builder) {
        builder.header("ChamberFix Config File");

        resetTimeTicks = builder.longEntry("reset_time_ticks", (2 * 60 * 60 * 20L), 1L, Long.MAX_VALUE).comment("The number of ticks before a chamber vault resets");
    }
}
