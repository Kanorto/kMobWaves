package vv0ta3fa9.plugin.kGiftMechanic.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kGiftMechanic.kGiftMechanic;
import vv0ta3fa9.plugin.kGiftMechanic.utils.Color.Colorizer;
import vv0ta3fa9.plugin.kGiftMechanic.utils.Color.impl.LegacyAdvancedColorizer;
import vv0ta3fa9.plugin.kGiftMechanic.utils.Color.impl.LegacyColorizer;
import vv0ta3fa9.plugin.kGiftMechanic.utils.Color.impl.VanillaColorizer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private final kGiftMechanic plugin;
    protected FileConfiguration config;
    private File configFile;
    public Colorizer COLORIZER;

    public ConfigManager(kGiftMechanic plugin) {
        this.plugin = plugin;
        loadConfigFiles();
    }

    private void loadConfigFiles() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                try {
                    plugin.saveResource("config.yml", false);
                } catch (Exception e) {
                    plugin.getLogger().warning("Не удалось сохранить config.yml: " + e.getMessage());
                    plugin.getLogger().warning("Создайте папку plugins/kGiftMechanic/ вручную и дайте права на запись");
                }
            }
            if (configFile.exists()) {
                config = YamlConfiguration.loadConfiguration(configFile);
            }
        });
    }

    public String getString(String path, String def) {
        if (config == null) return def;
        return config.contains(path) ? config.getString(path) : def;
    }

    public List<String> getStringList(String path) {
        if (config == null) return new ArrayList<>();
        return config.getStringList(path);
    }

    public Set<String> getGiftLocations() {
        Set<String> locations = new HashSet<>();
        List<String> giftList = getStringList("gifts.locations");
        locations.addAll(giftList);
        return locations;
    }

    public int getGiftCount() {
        return getGiftLocations().size();
    }

    public String getGiftCollectMessage(int collected, int total) {
        String message = getString("messages.gift-collected", "&aВы забрали 1 подарок! Осталось %collected%/%total%");
        return message.replace("%collected%", String.valueOf(collected))
                .replace("%total%", String.valueOf(total));
    }

    public String getRewardSuccessMessage() {
        return getString("messages.reward-success", "&aМолодец! Ты выполнил все задания!");
    }

    public String getNotAllGiftsMessage(int collected, int total) {
        String message = getString("messages.not-all-gifts", "&cВы собрали не все подарки! Собрано: %collected%/%total%");
        return message.replace("%collected%", String.valueOf(collected))
                     .replace("%total%", String.valueOf(total));
    }

    public String getAlreadyClaimedMessage() {
        return getString("messages.already-claimed", "&cВы уже получили награду!");
    }

    public String getGiftAlreadyCollectedMessage() {
        return getString("messages.gift-already-collected", "&cВы уже собрали этот подарок!");
    }

    public List<String> getRewardCommands() {
        return getStringList("command-on-complete");
    }

    /**
     * Добавить координаты подарка в конфиг
     * @param giftKey координаты в формате "world:x:y:z"
     * @return true если успешно добавлено, false если уже существует
     */
    public boolean addGiftLocation(String giftKey) {
        if (config == null || configFile == null) {
            return false;
        }

        List<String> locations = getStringList("gifts.locations");
        if (locations.contains(giftKey)) {
            return false;
        }
        locations.add(giftKey);
        config.set("gifts.locations", locations);
        try {
            config.save(configFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка сохранения конфига: " + e.getMessage());
            return false;
        }
    }

    public void setupColorizer() {
        COLORIZER = switch (getString("serializer", "LEGACY").toUpperCase()) {
            case "LEGACY" -> new LegacyColorizer(plugin);
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer(plugin);
            default -> new VanillaColorizer(plugin);
        };
    }
}

