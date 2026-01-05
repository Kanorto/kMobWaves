package vv0ta3fa9.plugin.kGiftMechanic;

import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import vv0ta3fa9.plugin.kGiftMechanic.main.CommandManager;
import vv0ta3fa9.plugin.kGiftMechanic.main.EventHendler;
import vv0ta3fa9.plugin.kGiftMechanic.managers.ConfigManager;
import vv0ta3fa9.plugin.kGiftMechanic.managers.DataManager;
import vv0ta3fa9.plugin.kGiftMechanic.utils.Color.impl.Utils;

public class kGiftMechanic extends JavaPlugin {

    public final Server server = getServer();
    @Getter protected ConfigManager configManager;
    @Getter protected Utils utils;
    @Getter protected CommandManager commandsManager;
    @Getter protected DataManager dataManager;
    @Getter protected EventHendler eventHendler;


    protected void loadingConfiguration() {
        try {
            configManager = new ConfigManager(this);
            utils = new Utils();
            commandsManager = new CommandManager(this);
            dataManager = new DataManager(this);
            eventHendler = new EventHendler(this);
        } catch (Exception e) {
            getLogger().severe("Error load configuration! Plugin disable...");
            e.printStackTrace();
            throw e;
        }
    }
    protected void registerCommands() {
        if (getCommand("kgm") != null) getCommand("kgm").setExecutor(commandsManager);
        else getLogger().severe("Команда 'kgm' не найдена в plugin.yml!");
    }

}
