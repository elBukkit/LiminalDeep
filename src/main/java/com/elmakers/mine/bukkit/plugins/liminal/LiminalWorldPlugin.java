package com.elmakers.mine.bukkit.plugins.liminal;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class LiminalWorldPlugin extends JavaPlugin {
    private static int CURRENT_VERSION = 1;
    private LiminalController controller;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigurationSection configuration = getConfig();
        ConfigurationSection generalConfig = configuration.getConfigurationSection("general");
        if (generalConfig == null || generalConfig.getInt("version", 0) < CURRENT_VERSION) {
            getLogger().severe("Plugin configuration is outdated. Disabling plugin. Please regenerate the config, make a copy first if you want to add any edits you've made.");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            controller = new LiminalController(this, configuration);
        }
    }
}
