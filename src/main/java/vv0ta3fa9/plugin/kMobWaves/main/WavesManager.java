package vv0ta3fa9.plugin.kMobWaves.main;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.Runner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

public class WavesManager implements Listener {
    
    private final KMobWaves plugin;
    private final Runner runner;
    private final Map<UUID, WaveData> activeMobs = new ConcurrentHashMap<>();
    private final List<WaveData> waves = new ArrayList<>();
    private final BossBarManager bossBarManager;
    private final Set<Integer> highlightTasks = ConcurrentHashMap.newKeySet();
    private int currentWaveIndex = -1;
    private boolean isActive = false;
    private int taskId = -1;
    private boolean isCheckingCompletion = false;
    
    public WavesManager(@NotNull KMobWaves plugin, @NotNull Runner runner) {
        this.plugin = plugin;
        this.runner = runner;
        this.bossBarManager = new BossBarManager(plugin);
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
        
        // Удаляем BossBar
        bossBarManager.removeBossBar();
        
        // Отменяем все задачи подсветки
        cancelHighlightTasks();
        
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
                plugin.getLogger().info("Все волны завершены.");
            }
            
            // Проверяем настройку auto_restart
            if (plugin.getConfigManager().getAutoRestart()) {
                if (plugin.getConfigManager().isWaveMessagesEnabled()) {
                    String message = plugin.getConfigManager().getAllWavesCompleteMessage();
                    broadcastMessage(plugin.getConfigManager().COLORIZER.colorize(message));
                }
                currentWaveIndex = 0;
                startWave(0);
            } else {
                // Останавливаем волны
                stopWaves();
            }
            return;
        }
        
        WaveData wave = waves.get(waveIndex);
        currentWaveIndex = waveIndex;
        
        if (plugin.getConfigManager().getDebug()) {
            plugin.getLogger().info("Запуск волны #" + wave.getCount() + " с " + wave.getMobsCount() + " мобами");
        }
        
        // Отправляем сообщение о начале волны
        if (plugin.getConfigManager().isWaveMessagesEnabled()) {
            try {
                String message = plugin.getConfigManager().getWaveStartMessage()
                    .replace("%wave%", String.valueOf(wave.getCount()));
                broadcastMessage(plugin.getConfigManager().COLORIZER.colorize(message));
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при отправке сообщения о начале волны: " + e.getMessage());
                if (plugin.getConfigManager().getDebug()) {
                    e.printStackTrace();
                }
            }
        }
        
        // Воспроизводим звук начала волны
        if (plugin.getConfigManager().isSoundsEnabled()) {
            try {
                playSound(plugin.getConfigManager().getWaveStartSound(),
                         plugin.getConfigManager().getWaveStartVolume(),
                         plugin.getConfigManager().getWaveStartPitch());
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при воспроизведении звука начала волны: " + e.getMessage());
                if (plugin.getConfigManager().getDebug()) {
                    e.printStackTrace();
                }
            }
        }
        
        // Создаем BossBar
        try {
            bossBarManager.createBossBar(wave.getCount(), wave.getMobsCount(), wave.getCustomTitle());
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при создании BossBar: " + e.getMessage());
            if (plugin.getConfigManager().getDebug()) {
                e.printStackTrace();
            }
        }
        
        runner.run(() -> spawnWaveMobs(wave));
    }

    private void spawnWaveMobs(@NotNull WaveData wave) {
        List<Location> coordinates = wave.getCoordinates();
        List<MobSpawnData> mobsData = wave.getMobs();
        int mobsCount = wave.getMobsCount();
        int spawnRadius = plugin.getConfigManager().getSpawnRadius();
        
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
            Location baseLocation = coordinates.get(random.nextInt(coordinates.size()));
            
            // Применяем случайный разброс в радиусе spawn_radius
            Location spawnLoc = baseLocation.clone();
            if (spawnRadius > 0) {
                double offsetX = (random.nextDouble() * 2 - 1) * spawnRadius;
                double offsetZ = (random.nextDouble() * 2 - 1) * spawnRadius;
                spawnLoc.add(offsetX, 0, offsetZ);
                
                // Adjust Y to highest block at the new X/Z location to prevent spawning in air or underground
                try {
                    int highestY = spawnLoc.getWorld().getHighestBlockYAt(spawnLoc);
                    spawnLoc.setY(highestY + 1);
                } catch (Exception e) {
                    // If error occurs, keep original Y coordinate
                    if (plugin.getConfigManager().getDebug()) {
                        plugin.getLogger().warning("Ошибка при определении высоты для спавна: " + e.getMessage());
                    }
                }
            }

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
                // Применяем множитель здоровья
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    double healthMultiplier = wave.getHealthMultiplier();
                    if (healthMultiplier > 0 && healthMultiplier != 1.0) {
                        // Clamp to reasonable range (0.1 to 100.0)
                        healthMultiplier = Math.max(0.1, Math.min(100.0, healthMultiplier));
                        
                        try {
                            double newMaxHealth = livingEntity.getMaxHealth() * healthMultiplier;
                            livingEntity.setMaxHealth(newMaxHealth);
                            livingEntity.setHealth(newMaxHealth);
                            
                            if (plugin.getConfigManager().getDebug()) {
                                plugin.getLogger().info("Применен множитель здоровья " + healthMultiplier + " к мобу " + mobName);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Ошибка при применении множителя здоровья к мобу " + mobName + ": " + e.getMessage());
                        }
                    }
                }
                
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
        
        try {
            UUID entityId = event.getEntity().getUniqueId();
            if (activeMobs.containsKey(entityId)) {
                activeMobs.remove(entityId);
                
                // Воспроизводим звук смерти моба
                if (plugin.getConfigManager().isSoundsEnabled()) {
                    try {
                        playSound(plugin.getConfigManager().getMobDeathSound(),
                                 plugin.getConfigManager().getMobDeathVolume(),
                                 plugin.getConfigManager().getMobDeathPitch());
                    } catch (Exception e) {
                        if (plugin.getConfigManager().getDebug()) {
                            plugin.getLogger().warning("Ошибка при воспроизведении звука смерти моба: " + e.getMessage());
                        }
                    }
                }
                
                // Обновляем BossBar
                try {
                    bossBarManager.updateProgress(activeMobs.size());
                } catch (Exception e) {
                    if (plugin.getConfigManager().getDebug()) {
                        plugin.getLogger().warning("Ошибка при обновлении BossBar: " + e.getMessage());
                    }
                }
                
                if (plugin.getConfigManager().getDebug()) {
                    plugin.getLogger().info("Моб убит. Осталось: " + activeMobs.size());
                }

                runner.run(() -> checkWaveCompletion());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при обработке смерти моба: " + e.getMessage());
            if (plugin.getConfigManager().getDebug()) {
                e.printStackTrace();
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (isActive) {
            try {
                bossBarManager.addPlayer(event.getPlayer());
            } catch (Exception e) {
                if (plugin.getConfigManager().getDebug()) {
                    plugin.getLogger().warning("Ошибка при добавлении игрока к BossBar: " + e.getMessage());
                }
            }
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
        
        // Отправляем сообщение о завершении волны
        if (plugin.getConfigManager().isWaveMessagesEnabled()) {
            String message = plugin.getConfigManager().getWaveCompleteMessage()
                .replace("%wave%", String.valueOf(currentWave.getCount()))
                .replace("%delay%", String.valueOf(currentWave.getExceptions()))
                .replace("%next_wave%", String.valueOf(currentWave.getCount() + 1));
            broadcastMessage(plugin.getConfigManager().COLORIZER.colorize(message));
        }
        
        // Воспроизводим звук завершения волны
        if (plugin.getConfigManager().isSoundsEnabled()) {
            playSound(plugin.getConfigManager().getWaveCompleteSound(),
                     plugin.getConfigManager().getWaveCompleteVolume(),
                     plugin.getConfigManager().getWaveCompletePitch());
        }
        
        // Выполняем команды-награды
        executeRewards(currentWave.getRewards());

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
    
    /**
     * Получает список активных мобов волны
     * @return список активных сущностей мобов
     */
    @NotNull
    public List<Entity> getActiveMobEntities() {
        List<Entity> entities = new ArrayList<>();
        
        for (UUID mobId : activeMobs.keySet()) {
            Entity entity = plugin.getServer().getEntity(mobId);
            if (entity != null && !entity.isDead()) {
                entities.add(entity);
            }
        }
        
        return entities;
    }
    
    /**
     * Регистрирует задачу подсветки для отслеживания
     * @param taskId ID задачи
     */
    public void registerHighlightTask(int taskId) {
        highlightTasks.add(taskId);
    }
    
    /**
     * Отменяет все активные задачи подсветки
     */
    private void cancelHighlightTasks() {
        for (int taskId : highlightTasks) {
            try {
                Bukkit.getScheduler().cancelTask(taskId);
            } catch (Exception e) {
                if (plugin.getConfigManager().getDebug()) {
                    plugin.getLogger().warning("Ошибка при отмене задачи подсветки: " + e.getMessage());
                }
            }
        }
        highlightTasks.clear();
    }
    
    /**
     * Воспроизводит звук для всех онлайн игроков
     */
    private void playSound(String soundName, float volume, float pitch) {
        Sound sound;
        try {
            sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неверное название звука: " + soundName);
            return; // Exit early if sound is invalid
        }
        
        // Play for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                if (plugin.getConfigManager().getDebug()) {
                    plugin.getLogger().warning("Ошибка воспроизведения звука для " + player.getName());
                }
            }
        }
    }
    
    /**
     * Отправляет сообщение всем онлайн игрокам
     */
    private void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Выполняет команды-награды
     */
    private void executeRewards(List<String> rewards) {
        if (rewards.isEmpty()) {
            return;
        }
        
        for (String command : rewards) {
            if (command == null || command.trim().isEmpty()) {
                continue;
            }
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.trim());
                if (plugin.getConfigManager().getDebug()) {
                    plugin.getLogger().info("Выполнена команда-награда: " + command);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при выполнении команды-награды: " + command);
                if (plugin.getConfigManager().getDebug()) {
                    e.printStackTrace();
                }
            }
        }
    }
}
