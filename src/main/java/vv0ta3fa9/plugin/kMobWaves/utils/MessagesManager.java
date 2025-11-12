package vv0ta3fa9.plugin.kMobWaves.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.Runner;

import java.io.File;

public class MessagesManager {
    private final KMobWaves plugin;
    private Runner runner;
    private FileConfiguration messagesconfig;
    private File messagesConfigFile;

    public MessagesManager(KMobWaves plugin, Runner runner) {
        this.runner = runner;
        this.plugin = plugin;
        loadFiles();
    }

    private void loadFiles() {
        messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesConfigFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        reloadMessages();
    }
    
    /**
     * Reloads the messages configuration from file
     */
    public void reloadMessages() {
        if (messagesConfigFile == null) {
            messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (messagesConfigFile.exists()) {
            messagesconfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
        } else {
            plugin.getLogger().warning("Messages file not found: " + messagesConfigFile.getPath());
        }
    }

    private String getMessage(String path, String defaultValue) {
        if (messagesconfig == null) return defaultValue;
        return messagesconfig.getString(path, defaultValue);
    }

    // ---- system ---- //
    public String nopermission() {
        return getMessage("system.no-permission", "§cУ тебя нет прав.");
    }
    public String reloadplugin() {
        return getMessage("system.reload-plugin", "§aБип-пуп успешная перезагрузкая");
    }
    public String playeronly() {
        return messagesconfig.getString("system.console-only", "§cЭта команда доступна только из консоли!");
    }

    // ---- another ---- //
    public String info(String count, String currentWave) {
        return getMessage("another.info", "&eОсталось &a%count% &eсуществ.")
                .replace("%count%", count)
                .replace("%wave%", currentWave);
    }
}