package com.elmakers.mine.bukkit.plugins.liminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.liminal.generator.LiminalGenerator;
import com.elmakers.mine.bukkit.plugins.liminal.listener.ChunkListener;
import com.elmakers.mine.bukkit.plugins.liminal.listener.PlayerListener;
import com.elmakers.mine.bukkit.plugins.liminal.rp.ResourcePackManager;

public class LiminalController implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, LiminalWorld> worlds = new HashMap();
    private final Map<String, ConfigurationSection> populatorConfigs = new HashMap();
    private final Map<String, ConfigurationSection> roomConfigs = new HashMap();
    private final LiminalCommandExecutor commandExecutor;
    private final PlayerListener playerListener;
    private final ChunkListener chunkListener;
    private final String defaultWorld;
    private final ItemGenerator itemGenerator;
    private final ResourcePackManager resourcePacks;

    public LiminalController(JavaPlugin plugin, ConfigurationSection configuration) {
        this.plugin = plugin;
        PluginManager pm = getServer().getPluginManager();
        ConfigurationSection generalConfig = configuration.getConfigurationSection("general");
        defaultWorld = generalConfig.getString("default_world");

        // Load Populators
        ConfigurationSection populatorConfigs = configuration.getConfigurationSection("populators");
        for (String key : populatorConfigs.getKeys(false)) {
            this.populatorConfigs.put(key, populatorConfigs.getConfigurationSection(key));
        }

        // Load Rooms
        ConfigurationSection roomConfigs = configuration.getConfigurationSection("rooms");
        for (String key : roomConfigs.getKeys(false)) {
            this.roomConfigs.put(key, roomConfigs.getConfigurationSection(key));
        }

        // Load Worlds
        ConfigurationSection worldConfigs = configuration.getConfigurationSection("worlds");
        for (String key : worldConfigs.getKeys(false)) {
            LiminalWorld world = new LiminalWorld(this, key, generalConfig, worldConfigs.getConfigurationSection(key));
            worlds.put(key, world);
        }

        ConfigurationSection itemConfigs = configuration.getConfigurationSection("items");
        itemGenerator = new ItemGenerator(this, generalConfig, itemConfigs);

        commandExecutor = new LiminalCommandExecutor(this);
        playerListener = new PlayerListener(this);
        pm.registerEvents(playerListener, getPlugin());
        chunkListener = new ChunkListener(this);
        pm.registerEvents(chunkListener, getPlugin());
        getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            for (String worldName : worlds.keySet()) {
                getWorld(worldName).getWorld();
            }
        }, 1L);
        resourcePacks = new ResourcePackManager(this);
        resourcePacks.load(generalConfig);
        pm.registerEvents(resourcePacks, getPlugin());

        // Remove all crafting recipes
        Bukkit.clearRecipes();
    }

    public Location getSpawnLocation(String worldName) {
        LiminalWorld liminalWorld = getWorld(worldName);
        if (liminalWorld == null) {
            return null;
        }
        return liminalWorld.getSpawnLocation();
    }

    public Location getEntryLocation(String worldName) {
        LiminalWorld world = getWorld(worldName);
        if (world == null) {
            return null;
        }
        return world.getEntryLocation();
    }

    public boolean sendToLevel(Player player, String worldName) {
        Location spawnLocation = getSpawnLocation(worldName);
        if (spawnLocation == null) {
            return false;
        }
        player.teleport(spawnLocation);
        return true;
    }

    public LiminalWorld getWorld(String worldName) {
        return worlds.get(worldName);
    }

    public Material[] getMaterials(ConfigurationSection config, String key, Material[] defaults) {
        List<String> materialNames = config.getStringList(key);
        if (materialNames == null || materialNames.isEmpty()) {
            return defaults;
        }
        Material[] materials = new Material[materialNames.size()];
        for (int i = 0; i < materialNames.size(); i++) {
            String materialName = materialNames.get(i);
            materials[i] = Material.matchMaterial(materialName);
        }
        return materials;
    }

    public CustomSound[] getSounds(ConfigurationSection config, String key, CustomSound[] defaults) {
        List<String> soundNames = config.getStringList(key);
        if (soundNames == null || soundNames.isEmpty()) {
            return defaults;
        }
        CustomSound[] sounds = new CustomSound[soundNames.size()];
        for (int i = 0; i < soundNames.size(); i++) {
            String soundName = soundNames.get(i);
            sounds[i] = CustomSound.fromString(soundName);
        }
        return sounds;
    }

    public void checkNewChunk(Chunk chunk) {
        LiminalWorld liminalWorld = getWorld(chunk.getWorld().getName());
        if (liminalWorld != null) {
            liminalWorld.checkNewChunk(chunk);
        }
    }

    public List<LiminalGenerator> getWorlds() {
        return new ArrayList(worlds.values());
    }

    public List<String> getWorldKeys() {
        return new ArrayList(worlds.keySet());
    }

    public LiminalWorld getDefaultWorld() {
        return getWorld(defaultWorld);
    }

    public ItemStack createItem(String key) {
        ItemStack itemStack = itemGenerator.createItem(key);
        if (itemStack == null) {
            try {
                Material material = Material.valueOf(key.toUpperCase(Locale.ROOT));
                itemStack = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                getLogger().severe("Invalid material key: " + key);
                return null;
            }
        }
        return itemStack;
    }

    public ConfigurationSection combineConfigurations(ConfigurationSection overrides, ConfigurationSection inherit) {
        final ConfigurationSection combined = new MemoryConfiguration();
        for (String inheritKey : inherit.getKeys(true)) {
            if (!overrides.contains(inheritKey)) {
                combined.set(inheritKey, inherit.get(inheritKey));
            }
        }
        for (String overrideKey : overrides.getKeys(true)) {
            combined.set(overrideKey, overrides.get(overrideKey));
        }
        return combined;
    }

    private ConfigurationSection processInheritedConfig(ConfigurationSection config, Map<String, ConfigurationSection> templates) {
        if (config == null) return  null;
        String inheritId = config.getString("inherit");
        while (inheritId != null) {
            ConfigurationSection inheritConfig = templates.get(inheritId);
            if (inheritConfig != null) {
                inheritId = inheritConfig.getString("inherit");
                config = combineConfigurations(config, inheritConfig);
            } else {
                getLogger().warning("Invalid inherit id: " + inheritId);
                break;
            }
        }
        return config;
    }

    private ConfigurationSection processRoomConfig(ConfigurationSection config) {
        return processInheritedConfig(config, roomConfigs);
    }

    private ConfigurationSection processPopulatorConfig(ConfigurationSection config) {
        return processInheritedConfig(config, populatorConfigs);
    }

    public List<String> getItemKeys() {
        return itemGenerator.getItemKeys();
    }

    public ConfigurationSection getRoomConfig(String id) {
        return processRoomConfig(roomConfigs.get(id));
    }

    public ConfigurationSection getRoomConfig(ConfigurationSection config) {
        String id = config.getString("id");
        ConfigurationSection templateConfig = getRoomConfig(id);
        if (templateConfig != null) {
            return templateConfig;
        }
        return config;
    }

    public ConfigurationSection getPopulatorConfig(String id) {
        return processPopulatorConfig(populatorConfigs.get(id));
    }

    public ConfigurationSection getPopulatorConfig(ConfigurationSection config) {
        String id = config.getString("id");
        ConfigurationSection templateConfig = getPopulatorConfig(id);
        if (templateConfig != null) {
            return templateConfig;
        }
        return config;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }
}
