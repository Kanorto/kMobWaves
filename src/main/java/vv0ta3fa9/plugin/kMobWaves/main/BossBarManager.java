package vv0ta3fa9.plugin.kMobWaves.main;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;

public class BossBarManager {
    
    private final KMobWaves plugin;
    private BossBar bossBar;
    private int currentWave;
    private int totalMobs;
    private String titleTemplate;
    
    public BossBarManager(@NotNull KMobWaves plugin) {
        this.plugin = plugin;
    }
    
    public void createBossBar(int waveNumber, int totalMobs, String customTitle) {
        if (!plugin.getConfigManager().isBossBarEnabled()) {
            return;
        }
        
        removeBossBar();
        
        this.currentWave = waveNumber;
        this.totalMobs = totalMobs;
        
        this.titleTemplate = customTitle != null ? customTitle : plugin.getConfigManager().getBossBarTitle();
        
        String title = formatTitle(this.titleTemplate, totalMobs, totalMobs);
        title = plugin.getConfigManager().COLORIZER.colorize(title);
        
        BarColor color = parseColor(plugin.getConfigManager().getBossBarColor());
        BarStyle style = parseStyle(plugin.getConfigManager().getBossBarStyle());
        
        bossBar = Bukkit.createBossBar(title, color, style);
        bossBar.setProgress(1.0);
        
        String mode = plugin.getConfigManager().getBossBarMode();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (shouldShowBossBar(player, mode)) {
                bossBar.addPlayer(player);
            }
        }
    }
    
    private boolean shouldShowBossBar(@NotNull Player player, @NotNull String mode) {
        switch (mode.toUpperCase()) {
            case "ALL":
                return true;
            case "ADMIN":
                return player.hasPermission("kmobwaves.bossbar");
            case "NONE":
            default:
                return false;
        }
    }
    
    public void updateProgress(int remainingMobs) {
        if (bossBar == null || !plugin.getConfigManager().isBossBarEnabled()) {
            return;
        }
        
        String title = this.titleTemplate != null ? this.titleTemplate : plugin.getConfigManager().getBossBarTitle();
        title = formatTitle(title, remainingMobs, totalMobs);
        title = plugin.getConfigManager().COLORIZER.colorize(title);
        
        bossBar.setTitle(title);
        
        double progress = totalMobs > 0 ? (double) remainingMobs / totalMobs : 0.0;
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
    }
    
    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }
    
    public void addPlayer(@NotNull Player player) {
        if (bossBar != null && plugin.getConfigManager().isBossBarEnabled()) {
            String mode = plugin.getConfigManager().getBossBarMode();
            if (shouldShowBossBar(player, mode)) {
                bossBar.addPlayer(player);
            }
        }
    }
    
    public void removePlayer(@NotNull Player player) {
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }
    
    private String formatTitle(String title, int remaining, int total) {
        return title
            .replace("%wave%", String.valueOf(currentWave))
            .replace("%remaining%", String.valueOf(remaining))
            .replace("%total%", String.valueOf(total));
    }
    
    private BarColor parseColor(String colorName) {
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неверный цвет BossBar: " + colorName + ", используется YELLOW");
            return BarColor.YELLOW;
        }
    }
    
    private BarStyle parseStyle(String styleName) {
        try {
            return BarStyle.valueOf(styleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неверный стиль BossBar: " + styleName + ", используется SEGMENTED_10");
            return BarStyle.SEGMENTED_10;
        }
    }
}
