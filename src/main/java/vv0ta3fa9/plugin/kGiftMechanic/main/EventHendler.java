package vv0ta3fa9.plugin.kGiftMechanic.main;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import vv0ta3fa9.plugin.kGiftMechanic.kGiftMechanic;
import vv0ta3fa9.plugin.kGiftMechanic.managers.PlayerData;

public class EventHendler implements Listener {

    private final kGiftMechanic plugin;

    public EventHendler(kGiftMechanic plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Location blockLocation = event.getClickedBlock().getLocation();

        String giftKey = blockLocation.getWorld().getName() + ":" + 
                        blockLocation.getBlockX() + ":" + 
                        blockLocation.getBlockY() + ":" + 
                        blockLocation.getBlockZ();

        if (plugin.getCommandsManager().isInPointMode(player)) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                boolean added = plugin.getConfigManager().addGiftLocation(giftKey);
                
                if (added) {
                    String message = "&aКоординаты добавлены в конфиг: &e" + giftKey;
                    player.sendMessage(plugin.getConfigManager().COLORIZER.colorize(message));
                } else {
                    String message = "&cЭти координаты уже есть в конфиге: &e" + giftKey;
                    player.sendMessage(plugin.getConfigManager().COLORIZER.colorize(message));
                }

                event.setCancelled(true);
                return;
            }
        }
        if (!plugin.getConfigManager().getGiftLocations().contains(giftKey)) {
            return;
        }
        PlayerData playerData = plugin.getDataManager().getOrCreatePlayerData(player.getName());
        if (playerData.hasCollectedGift(giftKey)) {
            String message = plugin.getConfigManager().getGiftAlreadyCollectedMessage();
            player.sendMessage(plugin.getConfigManager().COLORIZER.colorize(message));
            return;
        }
        playerData.addCollectedGift(giftKey);
        plugin.getDataManager().updatePlayerData(playerData);
        int totalGifts = plugin.getConfigManager().getGiftCount();
        int collectedGifts = playerData.getCollectedGiftsCount();
        String message = plugin.getConfigManager().getGiftCollectMessage(collectedGifts, totalGifts);
        player.sendMessage(plugin.getConfigManager().COLORIZER.colorize(message));
    }
}
