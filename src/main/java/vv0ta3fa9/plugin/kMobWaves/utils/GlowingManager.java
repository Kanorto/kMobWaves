package vv0ta3fa9.plugin.kMobWaves.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;

import java.util.*;

/**
 * Менеджер для управления подсветкой мобов с использованием ProtocolLib
 * Позволяет создавать подсветку, видимую только определенным игрокам
 */
public class GlowingManager {
    
    private final KMobWaves plugin;
    private final boolean protocolLibAvailable;
    
    public GlowingManager(@NotNull KMobWaves plugin) {
        this.plugin = plugin;
        this.protocolLibAvailable = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
        
        if (protocolLibAvailable) {
            plugin.getLogger().info("ProtocolLib обнаружен - подсветка мобов будет видна только администратору в режиме ADMIN");
        } else {
            plugin.getLogger().info("ProtocolLib не обнаружен - подсветка мобов будет видна всем игрокам");
        }
    }
    
    /**
     * Проверяет, доступен ли ProtocolLib
     */
    public boolean isProtocolLibAvailable() {
        return protocolLibAvailable;
    }
    
    /**
     * Применяет эффект свечения к мобам
     * 
     * @param mobs список мобов для подсветки
     * @param viewer игрок, для которого будет видна подсветка (null = все игроки)
     * @return количество подсвеченных мобов
     */
    public int applyGlowing(@NotNull List<Entity> mobs, Player viewer) {
        if (!protocolLibAvailable || viewer == null) {
            // Fallback: используем стандартное API (видно всем)
            return applyStandardGlowing(mobs);
        }
        
        // Используем ProtocolLib для подсветки только для конкретного игрока
        return applyProtocolLibGlowing(mobs, viewer);
    }
    
    /**
     * Применяет стандартное свечение (видно всем игрокам)
     */
    private int applyStandardGlowing(@NotNull List<Entity> mobs) {
        int count = 0;
        for (Entity mob : mobs) {
            if (mob != null && !mob.isDead()) {
                mob.setGlowing(true);
                count++;
            }
        }
        return count;
    }
    
    /**
     * Применяет свечение через ProtocolLib (видно только указанному игроку)
     */
    private int applyProtocolLibGlowing(@NotNull List<Entity> mobs, @NotNull Player viewer) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            int count = 0;
            
            for (Entity mob : mobs) {
                if (mob == null || mob.isDead()) {
                    continue;
                }
                
                try {
                    // Создаем пакет с метаданными сущности
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, mob.getEntityId());
                    
                    // Получаем текущие метаданные и клонируем их
                    WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mob).deepClone();
                    
                    // Устанавливаем флаг свечения (индекс 0, битовая маска 0x40)
                    byte currentFlags = watcher.getByte(0);
                    watcher.setObject(0, (byte) (currentFlags | 0x40));
                    
                    // Создаем список значений для пакета из ОБНОВЛЕННОГО watcher
                    // После setObject нужно получать данные из модифицированного watcher
                    List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
                    for (var watchableObject : watcher.getWatchableObjects()) {
                        if (watchableObject == null) continue;
                        wrappedDataValueList.add(new WrappedDataValue(
                            watchableObject.getIndex(),
                            watchableObject.getWatcherObject().getSerializer(),
                            watchableObject.getRawValue()
                        ));
                    }
                    
                    packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
                    
                    // Отправляем пакет только указанному игроку
                    protocolManager.sendServerPacket(viewer, packet);
                    count++;
                    
                } catch (Exception e) {
                    if (plugin.getConfigManager().getDebug()) {
                        plugin.getLogger().warning("Ошибка при применении ProtocolLib подсветки к мобу " + mob.getType() + ": " + e.getMessage());
                    }
                }
            }
            
            return count;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при использовании ProtocolLib: " + e.getMessage());
            if (plugin.getConfigManager().getDebug()) {
                e.printStackTrace();
            }
            // Fallback на стандартное свечение
            return applyStandardGlowing(mobs);
        }
    }
    
    /**
     * Убирает эффект свечения с мобов
     * 
     * @param mobs список мобов
     * @param viewer игрок, у которого нужно убрать подсветку (null = все игроки)
     */
    public void removeGlowing(@NotNull List<Entity> mobs, Player viewer) {
        if (!protocolLibAvailable || viewer == null) {
            // Убираем стандартное свечение
            removeStandardGlowing(mobs);
        } else {
            // Убираем ProtocolLib свечение
            removeProtocolLibGlowing(mobs, viewer);
        }
    }
    
    /**
     * Убирает стандартное свечение
     */
    private void removeStandardGlowing(@NotNull List<Entity> mobs) {
        for (Entity mob : mobs) {
            if (mob != null && !mob.isDead()) {
                mob.setGlowing(false);
            }
        }
    }
    
    /**
     * Убирает ProtocolLib свечение для указанного игрока
     */
    private void removeProtocolLibGlowing(@NotNull List<Entity> mobs, @NotNull Player viewer) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            
            for (Entity mob : mobs) {
                if (mob == null || mob.isDead()) {
                    continue;
                }
                
                try {
                    // Создаем пакет с метаданными сущности
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, mob.getEntityId());
                    
                    // Получаем текущие метаданные и клонируем их
                    WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mob).deepClone();
                    
                    // Убираем флаг свечения (индекс 0, битовая маска 0x40)
                    byte currentFlags = watcher.getByte(0);
                    watcher.setObject(0, (byte) (currentFlags & ~0x40));
                    
                    // Создаем список значений для пакета из ОБНОВЛЕННОГО watcher
                    // После setObject нужно получать данные из модифицированного watcher
                    List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
                    for (var watchableObject : watcher.getWatchableObjects()) {
                        if (watchableObject == null) continue;
                        wrappedDataValueList.add(new WrappedDataValue(
                            watchableObject.getIndex(),
                            watchableObject.getWatcherObject().getSerializer(),
                            watchableObject.getRawValue()
                        ));
                    }
                    
                    packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
                    
                    // Отправляем пакет только указанному игроку
                    protocolManager.sendServerPacket(viewer, packet);
                    
                } catch (Exception e) {
                    if (plugin.getConfigManager().getDebug()) {
                        plugin.getLogger().warning("Ошибка при снятии ProtocolLib подсветки с моба " + mob.getType() + ": " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при использовании ProtocolLib: " + e.getMessage());
            if (plugin.getConfigManager().getDebug()) {
                e.printStackTrace();
            }
            // Fallback на стандартное снятие свечения
            removeStandardGlowing(mobs);
        }
    }
}
