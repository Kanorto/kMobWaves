package vv0ta3fa9.plugin.kMobWaves.main;

import org.jetbrains.annotations.NotNull;

/**
 * Класс для хранения данных о спавне моба с шансом
 */
public class MobSpawnData {
    
    private final String mobName;
    private final double chance;
    
    public MobSpawnData(@NotNull String mobName, double chance) {
        this.mobName = mobName;
        this.chance = Math.max(0.0, Math.min(100.0, chance)); // Ограничиваем от 0 до 100
    }
    
    @NotNull
    public String getMobName() {
        return mobName;
    }
    
    public double getChance() {
        return chance;
    }
    
    /**
     * Парсит строку формата "MobName:chance" или просто "MobName" (шанс по умолчанию 100)
     * @param data строка с данными о мобе
     * @return объект MobSpawnData
     */
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

