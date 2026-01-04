package vv0ta3fa9.plugin.kMobWaves.utils.color.impl;

import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.utils.color.Colorizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyColorizer implements Colorizer {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    private final KMobWaves plugin;

    public LegacyColorizer(KMobWaves plugin) {
        this.plugin = plugin;
    }
    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            char[] group = matcher.group(1).toCharArray();
            matcher.appendReplacement(builder,
                    plugin.getUtils().COLOR_CHAR + "x" +
                            plugin.getUtils().COLOR_CHAR + group[0] +
                            plugin.getUtils().COLOR_CHAR + group[1] +
                            plugin.getUtils().COLOR_CHAR + group[2] +
                            plugin.getUtils().COLOR_CHAR + group[3] +
                            plugin.getUtils().COLOR_CHAR + group[4] +
                            plugin.getUtils().COLOR_CHAR + group[5]);
        }
        message = matcher.appendTail(builder).toString();
        return plugin.getUtils().translateAlternateColorCodes('&', message);
    }

}
