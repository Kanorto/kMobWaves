package vv0ta3fa9.plugin.kGiftMechanic.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import vv0ta3fa9.plugin.kGiftMechanic.kGiftMechanic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private final kGiftMechanic plugin;
    private static final String DATA_FILE = "data.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Map<String, PlayerData> playerData = new HashMap<>();

    public DataManager(kGiftMechanic plugin) {
        this.plugin = plugin;
        loadData();
    }
    
    private void loadData() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(plugin.getDataFolder(), DATA_FILE);
            if (!file.exists()) {
                plugin.getLogger().info("Генерируем data.json...");
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, PlayerData>>() {
                }.getType();
                Map<String, PlayerData> loadedData = GSON.fromJson(reader, type);
                if (loadedData != null) {
                    playerData = loadedData;
                    plugin.getLogger().info("Загружено " + playerData.size() + " игроков из data.json");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка загрузки data.json: " + e.getMessage());
            }
        });
    }
    
    public void saveData() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(plugin.getDataFolder(), DATA_FILE);
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(playerData, writer);
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка сохранения data.json: " + e.getMessage());
            }
        });
    }

    public PlayerData getPlayerData(String playerName) {
        return playerData.get(playerName);
    }

    public PlayerData getOrCreatePlayerData(String playerName) {
        PlayerData data = playerData.get(playerName);
        if (data == null) {
            data = new PlayerData(playerName);
            playerData.put(playerName, data);
            saveData();
        }
        return data;
    }

    public void updatePlayerData(PlayerData data) {
        playerData.put(data.getPlayerKey(), data);
        saveData();
    }

}
