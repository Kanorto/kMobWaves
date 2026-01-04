package vv0ta3fa9.plugin.kMobWaves.utils.color.impl;

import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.utils.color.Colorizer;

public class VanillaColorizer implements Colorizer {

    private final KMobWaves plugin;

    public VanillaColorizer(KMobWaves plugin) {
        this.plugin = plugin;
    }

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return plugin.getUtils().translateAlternateColorCodes('&', message);
    }
}
