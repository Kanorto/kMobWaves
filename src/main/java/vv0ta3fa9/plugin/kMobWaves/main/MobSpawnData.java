package vv0ta3fa9.plugin.kMobWaves.main;

import org.jetbrains.annotations.NotNull;

public class MobSpawnData {
    
    private final String mobName;
    private final double chance;
    
    public MobSpawnData(@NotNull String mobName, double chance) {
        this.mobName = mobName;
        this.chance = Math.max(0.0, Math.min(100.0, chance));
    }
    
    @NotNull
    public String getMobName() {
        return mobName;
    }
    
    public double getChance() {
        return chance;
    }
    
    @NotNull
    public static MobSpawnData parse(@NotNull String data) {
        if (data.contains(":")) {
            String[] parts = data.split(":", 2);
            try {
                double chance = Double.parseDouble(parts[1]);
                return new MobSpawnData(parts[0], chance);
            } catch (NumberFormatException e) {
                return new MobSpawnData(data, 100.0);
            }
        }
        return new MobSpawnData(data, 100.0);
    }
}
