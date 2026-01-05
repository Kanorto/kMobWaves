package vv0ta3fa9.plugin.kMobWaves.main;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WaveData {
    
    private final int count;
    private final List<MobSpawnData> mobs;
    private final List<Location> coordinates;
    private final int mobsCount;
    private final int exceptions;
    private final double healthMultiplier;
    private final String customTitle;
    private final List<String> rewards;
    
    public WaveData(int count, @NotNull List<MobSpawnData> mobs, @NotNull List<Location> coordinates, 
                    int mobsCount, int exceptions) {
        this(count, mobs, coordinates, mobsCount, exceptions, 1.0, null, new ArrayList<>());
    }
    
    public WaveData(int count, @NotNull List<MobSpawnData> mobs, @NotNull List<Location> coordinates, 
                    int mobsCount, int exceptions, double healthMultiplier, 
                    @Nullable String customTitle, @NotNull List<String> rewards) {
        this.count = count;
        this.mobs = new ArrayList<>(mobs);
        this.coordinates = new ArrayList<>(coordinates);
        this.mobsCount = mobsCount;
        this.exceptions = exceptions;
        this.healthMultiplier = healthMultiplier;
        this.customTitle = customTitle;
        this.rewards = new ArrayList<>(rewards);
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
    
    public double getHealthMultiplier() {
        return healthMultiplier;
    }
    
    @Nullable
    public String getCustomTitle() {
        return customTitle;
    }
    
    @NotNull
    public List<String> getRewards() {
        return new ArrayList<>(rewards);
    }
}
