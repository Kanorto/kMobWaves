package vv0ta3fa9.plugin.kMobWaves.main;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для хранения данных о волне
 */
public class WaveData {
    
    private final int count;
    private final List<MobSpawnData> mobs;
    private final List<Location> coordinates;
    private final int mobsCount;
    private final int exceptions;
    
    public WaveData(int count, @NotNull List<MobSpawnData> mobs, @NotNull List<Location> coordinates, 
                    int mobsCount, int exceptions) {
        this.count = count;
        this.mobs = new ArrayList<>(mobs);
        this.coordinates = new ArrayList<>(coordinates);
        this.mobsCount = mobsCount;
        this.exceptions = exceptions;
    }
    
    public int getCount() {
        return count;
    }
    
    @NotNull
    public List<MobSpawnData> getMobs() {
        return new ArrayList<>(mobs);
    }
    
    @NotNull
    public List<Location> getCoordinates() {
        return new ArrayList<>(coordinates);
    }
    
    public int getMobsCount() {
        return mobsCount;
    }
    
    public int getExceptions() {
        return exceptions;
    }
}

