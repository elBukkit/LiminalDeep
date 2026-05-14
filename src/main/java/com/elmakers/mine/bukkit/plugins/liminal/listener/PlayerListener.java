package com.elmakers.mine.bukkit.plugins.liminal.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorldPlugin;

public class PlayerListener implements Listener {
    private final LiminalWorldPlugin plugin;

    public PlayerListener(LiminalWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        World startingWorld = plugin.getServer().getWorlds().get(0);
        LiminalWorld defaultWorld = plugin.getDefaultWorld();
        if (defaultWorld != null && world.equals(startingWorld)) {
            if (!plugin.sendToLevel(player, defaultWorld.getName())) {
                plugin.getLogger().warning("Unable to send " + player.getName() + " to starting world");
            } else {
                plugin.getLogger().info("Player " + player.getName() + " sent to starting world");
            }
        }
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        LiminalWorld liminalWorld = plugin.getWorld(world.getName());
        if (liminalWorld != null) {
            String nextLevel = liminalWorld.getNextLevel(player.getLocation());
            if (nextLevel != null && !nextLevel.isEmpty()) {
                Location entryLocation = plugin.getEntryLocation(nextLevel);
                event.setTo(entryLocation);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        LiminalWorld defaultWorld = plugin.getDefaultWorld();
        if (defaultWorld != null) {
            event.getEntity().setRespawnLocation(defaultWorld.getSpawnLocation(), true);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        LiminalWorld liminalWorld = plugin.getWorld(player.getWorld().getName());
        if (liminalWorld != null) {
            liminalWorld.enter(player);
        }
    }
}
