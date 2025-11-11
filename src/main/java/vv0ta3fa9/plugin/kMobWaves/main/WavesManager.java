package vv0ta3fa9.plugin.kMobWaves.main;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.Runner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WavesManager implements Listener {
    
    private final KMobWaves plugin;
    private final Runner runner;
    private final Map<UUID, WaveData> activeMobs = new ConcurrentHashMap<>  ();
    private final List<WaveData> waves = new ArrayList<>();
    private int currentWaveIndex = -1;
    private boolean isActive = false;
    private int taskId = -1;
    private boolean isCheckingCompletion = false; // Флаг для предотвращения множественных проверок
    
    public WavesManager(@NotNull KMobWaves plugin, @NotNull Runner runner) {
        this.plugin = plugin;
        this.runner = runner;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    

    public void loadWaves() {
        // Перезагружаем конфиг перед загрузкой волн
        plugin.getConfigManager().reloadConfig();
        waves.clear();
        waves.addAll(plugin.getConfigManager().getWaves());
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Загружено волн: " + waves.size());
        }
    }
    

    public boolean startWaves() {
        if (isActive) {
            plugin.getLogger().warning("Волны уже активны!");
            return false;
        }
        
        loadWaves();
        
        if (waves.isEmpty()) {
            plugin.getLogger().warning("Нет волн для запуска! Проверьте конфиг.");
            return false;
        }
        
        isActive = true;
        currentWaveIndex = 0;
        startWave(currentWaveIndex);
        return true;
    }
    
    /**
     * Запускает волны с определенной волны по номеру
     */
    public boolean startWavesFromWave(int waveNumber) {
        if (isActive) {
            plugin.getLogger().warning("Волны уже активны!");
            return false;
        }
        
        loadWaves();
        
        if (waves.isEmpty()) {
            plugin.getLogger().warning("Нет волн для запуска! Проверьте конфиг.");
            return false;
        }
        
        // Ищем волну по номеру (count)
        int foundIndex = -1;
        for (int i = 0; i < waves.size(); i++) {
            if (waves.get(i).getCount() == waveNumber) {
                foundIndex = i;
                break;
            }
        }
        
        if (foundIndex == -1) {
            plugin.getLogger().warning("Волна с номером " + waveNumber + " не найдена!");
            return false;
        }
        
        isActive = true;
        currentWaveIndex = foundIndex;
        startWave(currentWaveIndex);
        
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Запуск волн с волны #" + waveNumber + " (индекс: " + foundIndex + ")");
        }
        
        return true;
    }

    public void stopWaves() {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        currentWaveIndex = -1;
        isCheckingCompletion = false;
        
        if (taskId != -1) {
            runner.run(() -> {
                plugin.getServer().getScheduler().cancelTask(taskId);
                taskId = -1;
            });
        }
        

        runner.run(() -> {
            for (UUID mobId : new ArrayList<>(activeMobs.keySet())) {
                Entity entity = plugin.getServer().getEntity(mobId);
                if (entity != null && !entity.isDead()) {
                    entity.remove();
                }
                activeMobs.remove(mobId);
            }
        });
        
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Волны остановлены");
        }
    }

    /**
     * Запускает конкретную волну
     * @param waveIndex индекс волны
     */
    private void startWave(int waveIndex) {
        if (waveIndex >= waves.size() || waveIndex < 0) {
            if (plugin.getConfigManager().getDebug()) {
                plugin.getLogger().info("Все волны завершены. Перезапуск с первой волны...");
            }
            currentWaveIndex = 0;
            startWave(0);
            return;
        }
        
        WaveData wave = waves.get(waveIndex);
        currentWaveIndex = waveIndex;
        
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Запуск волны #" + wave.getCount() + " с " + wave.getMobsCount() + " мобами");
        }
        
        runner.run(() -> spawnWaveMobs(wave));
    }

    private void spawnWaveMobs(@NotNull WaveData wave) {
        List<Location> coordinates = wave.getCoordinates();
        List<MobSpawnData> mobsData = wave.getMobs();
        int mobsCount = wave.getMobsCount();
        
        if (coordinates.isEmpty() || mobsData.isEmpty()) {
            plugin.getLogger().warning("Волна #" + wave.getCount() + " имеет пустые данные!");
            return;
        }

        double totalChance = mobsData.stream().mapToDouble(MobSpawnData::getChance).sum();
        if (totalChance <= 0) {
            plugin.getLogger().warning("Волна #" + wave.getCount() + " имеет нулевые шансы для всех мобов!");
            return;
        }
        
        Random random = new Random();
        int spawned = 0;
        
        for (int i = 0; i < mobsCount; i++) {
            Location spawnLoc = coordinates.get(random.nextInt(coordinates.size()));

            MobSpawnData selectedMob = selectMobByChance(mobsData, random, totalChance);
            
            if (selectedMob == null) {
                continue;
            }

            spawnMythicMob(selectedMob.getMobName(), spawnLoc, wave);
            spawned++;
        }
        
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Заспавнено " + spawned + " мобов для волны #" + wave.getCount());
        }
    }
    
    /**
     * Выбирает моба по шансам
     * @param mobsData список мобов
     * @param random генератор случайных чисел
     * @param totalChance общая сумма шансов
     * @return выбранный моб или null
     */
    @Nullable
    private MobSpawnData selectMobByChance(@NotNull List<MobSpawnData> mobsData, 
                                           @NotNull Random random, 
                                           double totalChance) {
        double roll = random.nextDouble() * totalChance;
        double current = 0;
        
        for (MobSpawnData mobData : mobsData) {
            current += mobData.getChance();
            if (roll <= current) {
                return mobData;
            }
        }
        
        // Если не выбран (из-за округления), возвращаем последнего
        return mobsData.isEmpty() ? null : mobsData.get(mobsData.size() - 1);
    }

    private void spawnMythicMob(@NotNull String mobName, @NotNull Location location, @NotNull WaveData wave) {
        try {
            Optional<MythicMob> optionalMob = MythicBukkit.inst().getMobManager().getMythicMob(mobName);
            if (optionalMob.isEmpty()) {
                plugin.getLogger().warning("Моб с именем '" + mobName + "' не найден в MythicMobs.");
                return;
            }
            
            MythicMob mob = optionalMob.get();
            
            ActiveMob spawned = mob.spawn(BukkitAdapter.adapt(location), 1.0);

            if (spawned == null) {
                plugin.getLogger().warning("Не удалось заспавнить моба: " + mobName);
                return;
            }
            
            Entity entity = spawned.getEntity().getBukkitEntity();
            
            if (entity != null) {
                activeMobs.put(entity.getUniqueId(), wave);
                
                if (plugin.getConfigManager().getDebug()) {
                    plugin.getLogger().info("Заспавнен моб " + mobName + " в " + 
                            location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при спавне моба '" + mobName + "': " + e.getMessage());
            if (plugin.getConfigManager().getDebug()) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        if (!isActive) {
            return;
        }
        
        UUID entityId = event.getEntity().getUniqueId();
        if (activeMobs.containsKey(entityId)) {
            activeMobs.remove(entityId);
            
            if (plugin.getConfigManager().getDebug()) {
                plugin.getLogger().info("Моб убит. Осталось: " + activeMobs.size());
            }

            runner.run(() -> checkWaveCompletion());
        }
    }
    
    /**
     * Проверяет завершение волны и запускает следующую
     */
    private void checkWaveCompletion() {
        if (!isActive || !activeMobs.isEmpty() || isCheckingCompletion) {
            return;
        }
        
        // Проверяем, что индекс валиден
        if (currentWaveIndex < 0 || currentWaveIndex >= waves.size()) {
            if (plugin.getConfigManager().getDebug()) {
                plugin.getLogger().warning("Невалидный индекс волны: " + currentWaveIndex);
            }
            return;
        }

        isCheckingCompletion = true;
        
        WaveData currentWave = waves.get(currentWaveIndex);
        
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Волна #" + currentWave.getCount() + " завершена. " +
                    "Следующая волна через " + currentWave.getExceptions() + " секунд");
        }

        int delayTicks = currentWave.getExceptions() * 20;
        
        runner.runDelayed(() -> {
            if (!isActive) {
                isCheckingCompletion = false;
                return;
            }
            
            isCheckingCompletion = false;
            currentWaveIndex++;
            startWave(currentWaveIndex);
        }, delayTicks);
    }
    
    /**
     * @return количество живых мобов
     */
    public int getRemainingMobsCount() {
        // Очищаем мертвых мобов синхронно
        Iterator<UUID> iterator = activeMobs.keySet().iterator();
        while (iterator.hasNext()) {
            UUID mobId = iterator.next();
            Entity entity = plugin.getServer().getEntity(mobId);
            if (entity == null || entity.isDead()) {
                iterator.remove();
            }
        }
        
        return activeMobs.size();
    }
    
    /**
     * Проверяет, активны ли волны
     * @return true если волны активны
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * @return номер волны или -1 если не активна
     */
    public int getCurrentWaveNumber() {
        if (!isActive || currentWaveIndex < 0 || currentWaveIndex >= waves.size()) {
            return -1;
        }
        return waves.get(currentWaveIndex).getCount();
    }
}
