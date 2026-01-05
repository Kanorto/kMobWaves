package vv0ta3fa9.plugin.kGiftMechanic.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vv0ta3fa9.plugin.kGiftMechanic.kGiftMechanic;
import vv0ta3fa9.plugin.kGiftMechanic.managers.PlayerData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CommandManager implements CommandExecutor {

    private final kGiftMechanic plugin;
    private final Set<UUID> pointModePlayers = new HashSet<>();

    public CommandManager(kGiftMechanic plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            send(sender, "§cИспользование: /kgm <take|point>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("take")) {
            if (!(sender instanceof Player)) {
                send(sender, "§cЭта команда доступна только игрокам!");
                return true;
            }

            Player player = (Player) sender;
            handleTakeCommand(player);
            return true;
        }

        if (subcommand.equals("point")) {
            if (!(sender instanceof Player)) {
                send(sender, "§cЭта команда доступна только игрокам!");
                return true;
            }

            if (!sender.hasPermission("kgiftmechanic.admin")) {
                send(sender, "§cУ вас нет прав на использование этой команды!");
                return true;
            }

            Player player = (Player) sender;
            handlePointCommand(player);
            return true;
        }

        return false;
    }

    private void handleTakeCommand(Player player) {
        PlayerData playerData = plugin.getDataManager().getOrCreatePlayerData(player.getName());

        if (playerData.isRewardClaimed()) {
            String message = plugin.getConfigManager().getAlreadyClaimedMessage();
            send(player, message);
            return;
        }
        int totalGifts = plugin.getConfigManager().getGiftCount();
        int collectedGifts = playerData.getCollectedGiftsCount();
        if (collectedGifts < totalGifts) {
            String message = plugin.getConfigManager().getNotAllGiftsMessage(collectedGifts, totalGifts);
            send(player, message);
            return;
        }
        playerData.setRewardClaimed(true);
        plugin.getDataManager().updatePlayerData(playerData);
        String successMessage = plugin.getConfigManager().getRewardSuccessMessage();
        send(player, successMessage);
        List<String> rewardCommands = plugin.getConfigManager().getRewardCommands();
        for (String cmd : rewardCommands) {
            if (cmd == null || cmd.trim().isEmpty()) {
                continue;
            }
            String finalCommand = cmd.replace("%sender%", player.getName());
            String commandToExecute = finalCommand;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandToExecute);
            });
        }
    }

    private void handlePointCommand(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (pointModePlayers.contains(playerUUID)) {
            // Выключаем режим добавления точек
            pointModePlayers.remove(playerUUID);
            send(player, "&cРежим добавления точек выключен. Напишите /kgm point снова, чтобы включить.");
        } else {
            // Включаем режим добавления точек
            pointModePlayers.add(playerUUID);
            send(player, "&aРежим добавления точек включен! Кликните ЛКМ по блоку, чтобы добавить его координаты в конфиг.");
            send(player, "&7Напишите /kgm point снова, чтобы выключить режим.");
        }
    }

    /**
     * Проверяет, находится ли игрок в режиме добавления точек
     */
    public boolean isInPointMode(Player player) {
        return pointModePlayers.contains(player.getUniqueId());
    }

    /**
     * Удаляет игрока из режима добавления точек
     */
    public void removeFromPointMode(Player player) {
        pointModePlayers.remove(player.getUniqueId());
    }

    private void send(CommandSender sender, String msg) {
        sender.sendMessage(plugin.getConfigManager().COLORIZER.colorize(msg));
    }
}
