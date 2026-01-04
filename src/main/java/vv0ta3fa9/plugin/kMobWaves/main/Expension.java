package vv0ta3fa9.plugin.kMobWaves.main;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;

public class Expension extends PlaceholderExpansion implements Relational{

    private final KMobWaves plugin;

    public Expension(KMobWaves plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kMobWaves";
    }

    @Override
    public @NotNull String getAuthor() {
        return "vv0ta3fa9";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("wave")) {
            if (!plugin.getWavesManager().isActive()) {
                return "Волны не активны!";
            }
            int currentWave = plugin.getWavesManager().getCurrentWaveNumber();

            return String.valueOf(currentWave);
        }
        if (params.equalsIgnoreCase("count")) {
            if (!plugin.getWavesManager().isActive()) {
                return "Волны не активны!";
            }
            int remaining = plugin.getWavesManager().getRemainingMobsCount();

            return String.valueOf(remaining);
        }
        return null;
    }
    @Override
    public String onPlaceholderRequest(Player player, Player player1, String s) {
        return "";
    }
}
