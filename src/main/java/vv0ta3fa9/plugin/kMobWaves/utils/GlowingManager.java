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
    
    public boolean isProtocolLibAvailable() {
        return protocolLibAvailable;
    }
    
    public int applyGlowing(@NotNull List<Entity> mobs, Player viewer) {
        if (!protocolLibAvailable || viewer == null) {
            return applyStandardGlowing(mobs);
        }
        
        return applyProtocolLibGlowing(mobs, viewer);
    }
    
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
    
    private int applyProtocolLibGlowing(@NotNull List<Entity> mobs, @NotNull Player viewer) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            int count = 0;
            
            for (Entity mob : mobs) {
                if (mob == null || mob.isDead()) {
                    continue;
                }
                
                try {
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, mob.getEntityId());
                    
                    WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mob);
                    
                    byte currentFlags = watcher.getByte(0);
                    byte newFlags = (byte) (currentFlags | 0x40);
                    
                    var watcherObject = watcher.getWatchableObject(0);
                    if (watcherObject == null) {
                        if (plugin.getConfigManager().getDebug()) {
                            plugin.getLogger().warning("Не удалось получить watchableObject с индексом 0 для моба " + mob.getType());
                        }
                        continue;
                    }
                    
                    var serializer = watcherObject.getWatcherObject().getSerializer();
                    if (serializer == null) {
                        if (plugin.getConfigManager().getDebug()) {
                            plugin.getLogger().warning("Serializer для индекса 0 равен null для моба " + mob.getType());
                        }
                        continue;
                    }
                    
                    List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
                    wrappedDataValueList.add(new WrappedDataValue(0, serializer, newFlags));
                    
                    packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
                    
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
            return applyStandardGlowing(mobs);
        }
    }
    
    public void removeGlowing(@NotNull List<Entity> mobs, Player viewer) {
        if (!protocolLibAvailable || viewer == null) {
            removeStandardGlowing(mobs);
        } else {
            removeProtocolLibGlowing(mobs, viewer);
        }
    }
    
    private void removeStandardGlowing(@NotNull List<Entity> mobs) {
        for (Entity mob : mobs) {
            if (mob != null && !mob.isDead()) {
                mob.setGlowing(false);
            }
        }
    }
    
    private void removeProtocolLibGlowing(@NotNull List<Entity> mobs, @NotNull Player viewer) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            
            for (Entity mob : mobs) {
                if (mob == null || mob.isDead()) {
                    continue;
                }
                
                try {
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, mob.getEntityId());
                    
                    WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mob);
                    
                    byte currentFlags = watcher.getByte(0);
                    byte newFlags = (byte) (currentFlags & ~0x40);
                    
                    var watcherObject = watcher.getWatchableObject(0);
                    if (watcherObject == null) {
                        if (plugin.getConfigManager().getDebug()) {
                            plugin.getLogger().warning("Не удалось получить watchableObject с индексом 0 для моба " + mob.getType());
                        }
                        continue;
                    }
                    
                    var serializer = watcherObject.getWatcherObject().getSerializer();
                    if (serializer == null) {
                        if (plugin.getConfigManager().getDebug()) {
                            plugin.getLogger().warning("Serializer для индекса 0 равен null для моба " + mob.getType());
                        }
                        continue;
                    }
                    
                    List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
                    wrappedDataValueList.add(new WrappedDataValue(0, serializer, newFlags));
                    
                    packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
                    
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
            removeStandardGlowing(mobs);
        }
    }
}
