package vv0ta3fa9.plugin.kMobWaves;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import vv0ta3fa9.plugin.kMobWaves.main.CommandManager;
import vv0ta3fa9.plugin.kMobWaves.main.Expension;
import vv0ta3fa9.plugin.kMobWaves.main.WavesManager;
import vv0ta3fa9.plugin.kMobWaves.utils.ConfigManager;
import vv0ta3fa9.plugin.kMobWaves.utils.MessagesManager;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.PaperRunner;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.Runner;
import vv0ta3fa9.plugin.kMobWaves.utils.Utils;

public final class KMobWaves extends JavaPlugin {

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private Utils utils;
    private final Runner runner = new PaperRunner(this);
    private CommandManager commandsManager;
    private WavesManager wavesManager;
    
    @Override
    public void onEnable() {
        try {
            loadingConfiguration();
            configManager.setupColorizer();
            registerCommands();
            registerExpensions();
        } catch (Exception e) {
            getLogger().severe("ОШИБКА ВКЛЮЧЕНИЯ ПЛАГИНА! Выключение плагина...");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (wavesManager != null) {
            if (wavesManager.isActive()) {
                wavesManager.stopWaves();
            }
        }
    }

    private void loadingConfiguration() {
        try {
            configManager = new ConfigManager(this, runner);
            utils = new Utils();
            messagesManager = new MessagesManager(this, runner);
            commandsManager = new CommandManager(this, runner);
            wavesManager = new WavesManager(this, runner);
        } catch (Exception e) {
            getLogger().severe("ОШИБКА ЗАГРУЗКИ КОНФИГУРАЦИИ! Выключение плагина...");
            e.printStackTrace();
            throw e;
        }
    }
    private void registerCommands() {
        if (getCommand("kmobwaves") != null) getCommand("kmobwaves").setExecutor(commandsManager);
        else getLogger().severe("Команда 'kmobwaves' не найдена в plugin.yml!");
    }
    private void registerExpensions() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Expension(this).register();
            getLogger().info("PlaceholderAPI зарегистрирован.");
        } else {
            getLogger().warning("PlaceholderAPI не найден! Плейсхолдеры работать не будут.");
        }
    }

    // ---- Геттеры ----

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Utils getUtils() {
        return utils;
    }
    
    public WavesManager getWavesManager() {
        return wavesManager;
    }
}