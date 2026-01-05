package vv0ta3fa9.plugin.kGiftMechanic.utils.Color.impl;

import vv0ta3fa9.plugin.kGiftMechanic.kGiftMechanic;
import vv0ta3fa9.plugin.kGiftMechanic.utils.Color.Colorizer;

public class VanillaColorizer implements Colorizer {

    private final kGiftMechanic plugin;

    public VanillaColorizer(kGiftMechanic plugin) {
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
