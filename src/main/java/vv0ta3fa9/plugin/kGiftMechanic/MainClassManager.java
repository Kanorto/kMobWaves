package vv0ta3fa9.plugin.kGiftMechanic;

public class MainClassManager extends kGiftMechanic {

    @Override
    public void onEnable() {
        try {
            loadingConfiguration();
            configManager.setupColorizer();
            registerCommands();
            server.getPluginManager().registerEvents(eventHendler, this);
        } catch (Exception e) {
            getLogger().severe("Error load plugin. Plugin disable...");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
    }

}
