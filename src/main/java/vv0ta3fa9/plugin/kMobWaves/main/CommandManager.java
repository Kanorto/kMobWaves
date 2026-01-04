package vv0ta3fa9.plugin.kMobWaves.main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import vv0ta3fa9.plugin.kMobWaves.KMobWaves;
import vv0ta3fa9.plugin.kMobWaves.utils.Runner.Runner;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private final KMobWaves plugin;
    private Runner runner;

    public CommandManager(KMobWaves plugin, Runner runner) {
        this.plugin = plugin;
        this.runner = runner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            send(sender, "§cИспользование: /kmobwaves <reload|start|stop|info|force_start|highlight>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                if (!sender.hasPermission("kmobwaves.admin")) {
                    send(sender, plugin.getMessagesManager().nopermission());
                    return true;
                }
                try {
                    plugin.reloadPluginConfig();
                    send(sender, plugin.getMessagesManager().reloadplugin());
                } catch (Exception e) {
                    send(sender, "§4§lОшибка при перезагрузке конфигурации: " + e.getMessage());
                    if (plugin.getConfigManager().getDebug()) {
                        e.printStackTrace();
                    }
                }
                return true;
            case "start":
                if (!sender.hasPermission("kmobwaves.admin")) {
                    send(sender, plugin.getMessagesManager().nopermission());
                    return true;
                }
                if (plugin.getWavesManager().isActive()) {
                    send(sender, "§cВолны уже активны! Используйте /kmobwaves stop чтобы остановить.");
                    return true;
                }
                boolean started = plugin.getWavesManager().startWaves();
                if (started) {
                    send(sender, "§aВолны мобов запущены!");
                } else {
                    send(sender, "§cНе удалось запустить волны! Проверьте конфиг и логи.");
                }
                return true;
            case "stop":
                if (!sender.hasPermission("kmobwaves.admin")) {
                    send(sender, plugin.getMessagesManager().nopermission());
                    return true;
                }
                if (!plugin.getWavesManager().isActive()) {
                    send(sender, "§cВолны не активны!");
                    return true;
                }
                plugin.getWavesManager().stopWaves();
                send(sender, "§aВолны остановлены!");
                return true;
            case "forcestart", "force_start":
                if (!sender.hasPermission("kmobwaves.admin")) {
                    send(sender, plugin.getMessagesManager().nopermission());
                    return true;
                }
                if (args.length < 2) {
                    send(sender, "§cИспользование: /kmobwaves force_start <номер_волны>");
                    return true;
                }
                if (plugin.getWavesManager().isActive()) {
                    send(sender, "§cВолны уже активны! Используйте /kmobwaves stop чтобы остановить.");
                    return true;
                }
                try {
                    int waveNumber = Integer.parseInt(args[1]);
                    boolean forceStarted = plugin.getWavesManager().startWavesFromWave(waveNumber);
                    if (forceStarted) {
                        send(sender, "§aВолны мобов запущены с волны #" + waveNumber + "!");
                    } else {
                        send(sender, "§cНе удалось запустить волны с волны #" + waveNumber + "! Проверьте конфиг и логи.");
                    }
                } catch (NumberFormatException e) {
                    send(sender, "§cОшибка: '" + args[1] + "' не является числом!");
                }
                return true;
            case "info":
                if (!sender.hasPermission(plugin.getConfigManager().getPermissionInfo())) {
                    send(sender, plugin.getMessagesManager().nopermission());
                    return true;
                }
                if (!plugin.getWavesManager().isActive()) {
                    send(sender, "§eВолны не активны.");
                    return true;
                }
                int remaining = plugin.getWavesManager().getRemainingMobsCount();
                int currentWave = plugin.getWavesManager().getCurrentWaveNumber();
                send(sender, plugin.getMessagesManager().info(String.valueOf(remaining), String.valueOf(currentWave)));
                return true;
            case "highlight":
                String highlightMode = plugin.getConfigManager().getHighlightMode();
                
                if ("ADMIN".equalsIgnoreCase(highlightMode)) {
                    if (!sender.hasPermission("kmobwaves.admin")) {
                        send(sender, plugin.getMessagesManager().nopermission());
                        return true;
                    }
                } else {
                    if (!sender.hasPermission("kmobwaves.highlight")) {
                        send(sender, plugin.getMessagesManager().nopermission());
                        return true;
                    }
                }
                
                if (!(sender instanceof Player)) {
                    send(sender, plugin.getMessagesManager().playeronly());
                    return true;
                }
                if (!plugin.getWavesManager().isActive()) {
                    send(sender, "§cВолны не активны!");
                    return true;
                }
                
                Player player = (Player) sender;
                List<Entity> mobs = plugin.getWavesManager().getActiveMobEntities();
                
                if (mobs.isEmpty()) {
                    send(sender, "§cНет активных мобов для подсветки!");
                    return true;
                }
                
                try {
                    Player viewer = null;
                    String visibilityMessage;
                    
                    if ("ADMIN".equalsIgnoreCase(highlightMode) && plugin.getGlowingManager().isProtocolLibAvailable()) {
                        viewer = player;
                        visibilityMessage = plugin.getMessagesManager().highlightVisibilityAdmin();
                    } else {
                        viewer = null;
                        visibilityMessage = plugin.getMessagesManager().highlightVisibilityAll();
                    }
                    
                    int highlighted = plugin.getGlowingManager().applyGlowing(mobs, viewer);
                    
                    send(sender, plugin.getMessagesManager().highlightSuccess(highlighted, visibilityMessage));
                    
                    final List<Entity> glowingMobs = new ArrayList<>(mobs);
                    final Player finalViewer = viewer;
                    
                    int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            plugin.getGlowingManager().removeGlowing(glowingMobs, finalViewer);
                        } catch (Exception e) {
                            if (plugin.getConfigManager().getDebug()) {
                                plugin.getLogger().warning("Ошибка при снятии подсветки мобов: " + e.getMessage());
                            }
                        }
                    }, 200L).getTaskId();
                    
                    plugin.getWavesManager().registerHighlightTask(taskId);
                    
                } catch (Exception e) {
                    send(sender, "§cОшибка при подсветке мобов: " + e.getMessage());
                    if (plugin.getConfigManager().getDebug()) {
                        e.printStackTrace();
                    }
                }
                return true;
            default:
                send(sender, "§cНеизвестная подкоманда. Используйте: /kmobwaves <reload|start|stop|info|force_start|highlight>");
                return true;
        }
    }

    private void send(CommandSender sender, String msg) {
        sender.sendMessage(plugin.getConfigManager().COLORIZER.colorize(msg));
    }
}
