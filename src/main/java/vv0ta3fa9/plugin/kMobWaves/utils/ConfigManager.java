package vv0ta3fa9.plugin.kMobWaves.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.main.MobSpawnData;
import vv0ta3fa9.plugin.kMobWaves.main.WaveData;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.Runner;
import vv0ta3fa9.plugin.kMobWaves.utils.color.Colorizer;
import vv0ta3fa9.plugin.kMobWaves.utils.color.impl.LegacyAdvancedColorizer;
import vv0ta3fa9.plugin.kMobWaves.utils.color.impl.LegacyColorizer;
import vv0ta3fa9.plugin.kMobWaves.utils.color.impl.MiniMessageColorizer;
import vv0ta3fa9.plugin.kMobWaves.utils.color.impl.VanillaColorizer;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final KMobWaves plugin;
    protected FileConfiguration config;
    private File configFile;
    public Colorizer COLORIZER;
    private Runner runner;

    public ConfigManager(KMobWaves plugin, Runner runner) {
        this.plugin = plugin;
        this.runner = runner;
        loadConfigFiles();
    }

    private void loadConfigFiles() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                plugin.saveResource("config.yml", false);
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось сохранить config.yml: " + e.getMessage());
                plugin.getLogger().warning("Создайте папку plugins/kMobWaves/ вручную и дайте права на запись");
            }
        }
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }
    
    /**
     * Перезагружает конфиг из файла
     */
    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            plugin.getLogger().warning("Конфиг файл не найден: " + configFile.getPath());
        }
    }

    // ---- Гетеры ----

    public boolean getBoolean(String path, boolean def) {
        if (config == null) return def;
        return config.contains(path) ? config.getBoolean(path) : def;
    }

    public String getString(String path, String def) {
        if (config == null) return def;
        return config.contains(path) ? config.getString(path) : def;
    }

    public List<String> getStringList(String path) {
        if (config == null) return new ArrayList<>();
        return config.getStringList(path);
    }

    // ---- Общие настройки ----

    public boolean getDebug() {
        return getBoolean("debug", true);
    }
    public String getPermissionInfo() {
        return getString("info_command_permission", "kmobwaves.user");
    }

    public void setupColorizer() {
        COLORIZER = switch (getString("serializer", "LEGACY").toUpperCase()) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer(plugin);
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer(plugin);
            default -> new VanillaColorizer(plugin);
        };
    }

    @NotNull
    public List<WaveData> getWaves() {
        List<WaveData> waves = new ArrayList<>();
        if (config == null) {
            return waves;
        }
        

        if (!config.contains("Waves")) {
            return waves;
        }
        
        List<?> wavesList = config.getList("Waves");
        if (wavesList == null || wavesList.isEmpty()) {
            return waves;
        }
        
        for (int i = 0; i < wavesList.size(); i++) {
            try {
                // Получаем секцию для каждого элемента списка
                String wavePath = "Waves." + i;
                ConfigurationSection section = config.getConfigurationSection(wavePath);
                
                if (section == null) {
                    // Если не получилось получить как секцию, пробуем через Map
                    Object waveObj = wavesList.get(i);
                    if (waveObj instanceof java.util.Map) {
                        // Создаем временную секцию из Map
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> waveMap = (java.util.Map<String, Object>) waveObj;
                        section = config.createSection(wavePath);
                        for (java.util.Map.Entry<String, Object> entry : waveMap.entrySet()) {
                            section.set(entry.getKey(), entry.getValue());
                        }
                    }
                }
                
                if (section == null) {
                    plugin.getLogger().warning("Не удалось загрузить волну #" + (i + 1));
                    continue;
                }
                
                int count = section.getInt("count", i + 1);
                List<String> mobsList = section.getStringList("mobs");
                List<String> coordinatesList = section.getStringList("coordinates");
                int mobsCount = section.getInt("mobs-count", 10);
                int exceptions = section.getInt("exceptions", 10);

                List<MobSpawnData> mobs = new ArrayList<>();
                for (String mobData : mobsList) {
                    mobs.add(MobSpawnData.parse(mobData));
                }

                List<Location> coordinates = new ArrayList<>();
                
                List<World> availableWorlds = Bukkit.getWorlds();
                World defaultWorld = availableWorlds.isEmpty() ? null : availableWorlds.get(0);
                
                if (getDebug() && defaultWorld == null) {
                    plugin.getLogger().warning("Волна " + count + ": Нет доступных миров на сервере!");
                }
                
                // Пытаемся определить мир из координат
                World world = null;
                for (String coordStr : coordinatesList) {
                    World coordWorld = getWorldFromCoordinates(coordStr, defaultWorld);
                    if (coordWorld != null) {
                        world = coordWorld;
                        if (getDebug()) {
                            plugin.getLogger().info("Волна " + count + ": Определен мир '" + world.getName() + "' из координат");
                        }
                        break;
                    }
                }
                
                // Если не удалось определить мир из координат, используется дефолтный
                if (world == null) {
                    if (defaultWorld == null) {
                        plugin.getLogger().severe("Волна " + count + ": Не найден мир для спавна мобов! Укажите мир в координатах (world,x,y,z) или убедитесь, что миры загружены. Доступных миров: " + availableWorlds.size());
                        continue;
                    }
                    world = defaultWorld;
                    if (getDebug()) {
                        plugin.getLogger().info("Волна " + count + ": Используется дефолтный мир '" + world.getName() + "'");
                    }
                }
                
                for (String coordStr : coordinatesList) {
                    Location loc = parseLocation(coordStr, world);
                    if (loc != null) {
                        coordinates.add(loc);
                    }
                }
                
                if (coordinates.isEmpty()) {
                    plugin.getLogger().warning("Волна " + count + " не имеет валидных координат!");
                    continue;
                }
                
                if (mobs.isEmpty()) {
                    plugin.getLogger().warning("Волна " + count + " не имеет мобов для спавна!");
                    continue;
                }
                
                waves.add(new WaveData(count, mobs, coordinates, mobsCount, exceptions));
                
                if (getDebug()) {
                    plugin.getLogger().info("Загружена волна #" + count + " с " + mobsCount + " мобами");
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при загрузке волны #" + (i + 1) + ": " + e.getMessage());
                if (getDebug()) {
                    e.printStackTrace();
                }
            }
        }
        
        return waves;
    }
    
    /**
     * Определяет мир из строки координат
     * @param coordStr строка с координатами
     * @param defaultWorld мир по умолчанию
     * @return World или null если не удалось определить
     */
    private World getWorldFromCoordinates(@NotNull String coordStr, World defaultWorld) {
        try {
            String[] parts = coordStr.split(",");
            if (parts.length == 4) {
                String worldName = parts[0].trim();
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return world;
                }
            }
            // Если координаты без мира, возвращаем дефолтный (если есть)
            return defaultWorld;
        } catch (Exception e) {
            return defaultWorld;
        }
    }
    
    /**
     * Парсит строку координат формата "x,y,z" или "world,x,y,z"
     * @param coordStr строка с координатами
     * @param defaultWorld мир по умолчанию
     * @return Location или null при ошибке
     */
    private Location parseLocation(@NotNull String coordStr, @NotNull World defaultWorld) {
        try {
            String[] parts = coordStr.split(",");
            if (parts.length == 3) {
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                double z = Double.parseDouble(parts[2].trim());
                return new Location(defaultWorld, x, y, z);
            } else if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0].trim());
                if (world == null) {
                    world = defaultWorld;
                }
                double x = Double.parseDouble(parts[1].trim());
                double y = Double.parseDouble(parts[2].trim());
                double z = Double.parseDouble(parts[3].trim());
                return new Location(world, x, y, z);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка парсинга координат: " + coordStr + " - " + e.getMessage());
        }
        return null;
    }
}
