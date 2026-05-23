package com.elmakers.mine.bukkit.plugins.liminal.rp;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorldPlugin;
import com.google.common.io.BaseEncoding;

public class ResourcePackManager implements Listener {
    private static final String RP_FILE = "resourcepack";

    private final LiminalWorldPlugin controller;
    private boolean resourcePacksEnabled = true;
    private String resourcePack = null;
    private long resourcePackDelay = 0;
    private boolean resourcePackDataLoaded = false;
    private final Map<String, ResourcePack> resourcePacks = new HashMap<>();

    public ResourcePackManager(LiminalWorldPlugin controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection properties) {
        resourcePack = properties.getString("resource_pack", "");
        checkResourcePack(getPlugin().getServer().getConsoleSender());
    }

    public boolean sendResourcePack(final Player player) {
        if (!resourcePacksEnabled) return false;

        ResourcePack rp = createResourcePack(resourcePack);
        if (rp == null) {
            return false;
        }
        return sendResourcePack(player, rp);
    }

    private boolean sendResourcePack(final Player player, ResourcePack pack) {
        return sendResourcePack(player, pack.getUrl(), pack.getHash());
    }

    public boolean sendResourcePack(final Player player, String url, byte[] hash) {
        if (url == null || hash == null) {
            return false;
        }

        // Give them some time to read the message
        Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable() {
            @Override
            public void run() {
                String sendURL = url;
                String hashString;
                try {
                    hashString = BaseEncoding.base64().encode(hash);
                    // MC 1.17 got super dumb about RPs changing their hash without a new URL :(
                    sendURL += "#" + hashString;
                } catch (Exception ignore) {
                }
                player.setResourcePack(sendURL, hash);
            }
        }, resourcePackDelay * 20 / 1000);

        return true;
    }

    protected ResourcePack getResourcePack(String url) {
        return getResourcePacks().get(ResourcePack.getKey(url));
    }

    protected ResourcePack createResourcePack(String url) {
        ResourcePack pack = getResourcePack(url);
        if (pack == null) {
            synchronized (resourcePacks) {
                pack = new ResourcePack(url);
                resourcePacks.put(pack.getKey(), pack);
            }
        }
        pack.setUrl(url);
        return pack;
    }

    public boolean checkResourcePack(CommandSender sender) {
        final Plugin plugin = controller;
        if (!plugin.isEnabled()) return false;
        final Server server = plugin.getServer();
        resourcePacksEnabled = true;

        if (resourcePack == null || resourcePack.isEmpty()) {
            plugin.getLogger().info("Resource pack disabled");
            resourcePacksEnabled = false;
            return false;
        }

        String serverPack = server.getResourcePack();
        if (serverPack != null && !serverPack.isEmpty()) {
            plugin.getLogger().info("Server resource pack set, will not override");
            resourcePacksEnabled = false;
            return false;
        }
        plugin.getLogger().info("Checking resource pack for updates: " + ChatColor.GRAY + resourcePack);
        ResourcePack resourcePackInfo = createResourcePack(resourcePack);
        updateResourcePackHash(resourcePackInfo, new ResourcePackResponse() {
            @Override
            public void finished(boolean success, boolean hasModifiedTime, List<String> responses, ResourcePack pack) {
                if (!success && !hasModifiedTime) {
                    plugin.getLogger().warning("Resource pack check failed");
                } else {
                    for (String response : responses) {
                        sender.sendMessage(response);
                    }
                }
            }
        });
        return true;
    }

    public void updateResourcePackHash(ResourcePack resourcePack, ResourcePackResponse callback) {
        final Plugin plugin = controller;
        if (!plugin.isEnabled()) return;
        final Server server = plugin.getServer();
        server.getScheduler().runTaskAsynchronously(plugin, new ResourcePackUpdateRunnable(this, resourcePack, callback));
    }


    protected Map<String, ResourcePack> getResourcePacks() {
        synchronized (resourcePacks) {
            if (!resourcePackDataLoaded) {
                resourcePackDataLoaded = true;
                final File rpFile = new File(getPlugin().getDataFolder(), "data/" + RP_FILE + ".yml");
                if (rpFile.exists()) {
                    try {
                        YamlConfiguration resourcePackConfiguration = new YamlConfiguration();
                        resourcePackConfiguration.load(rpFile);
                        Set<String> keys = resourcePackConfiguration.getKeys(false);
                        for (String key : keys) {
                            resourcePacks.put(key, new ResourcePack(key, resourcePackConfiguration.getConfigurationSection(key)));
                        }
                    } catch (Exception ex) {
                        getPlugin().getLogger().log(Level.WARNING, "Error loading resource pack save file", ex);
                    }
                }
            }
        }
        return resourcePacks;
    }

    public void saveResourcePacks() {
        Map<String, ResourcePack> packs = getResourcePacks();
        synchronized (resourcePacks) {
            try {
                final File rpFile = new File(getPlugin().getDataFolder(), "data/" + RP_FILE + ".yml");
                YamlConfiguration resourcePackConfiguration = new YamlConfiguration();
                for (Map.Entry<String, ResourcePack> entry : packs.entrySet()) {
                    ConfigurationSection save = resourcePackConfiguration.createSection(entry.getKey());
                    entry.getValue().save(save);
                }
                resourcePackConfiguration.save(rpFile);
            } catch (Exception ex) {
                getPlugin().getLogger().log(Level.WARNING, "Error saving resource pack save file", ex);
            }
        }
    }

    public Plugin getPlugin() {
        return controller;
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        final Player player = event.getPlayer();
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                break;
            case DECLINED:
                player.sendMessage(ChatColor.RED + "Using the resource pack is strongly recommended");
                break;
            case FAILED_DOWNLOAD:
                player.sendMessage(ChatColor.RED + "The resource pack failed to download, please try again");
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        sendResourcePack(player);
    }
}
