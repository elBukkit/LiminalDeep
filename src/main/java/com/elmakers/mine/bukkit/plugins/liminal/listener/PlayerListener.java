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
import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;

public class PlayerListener implements Listener {
    private final LiminalController controller;

    public PlayerListener(LiminalController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        World startingWorld = controller.getServer().getWorlds().get(0);
        LiminalWorld defaultWorld = controller.getDefaultWorld();
        if (defaultWorld != null && world.equals(startingWorld)) {
            if (!controller.sendToLevel(player, defaultWorld.getName())) {
                controller.getLogger().warning("Unable to send " + player.getName() + " to starting world");
            } else {
                controller.getLogger().info("Player " + player.getName() + " sent to starting world");
            }
        }
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        LiminalWorld liminalWorld = controller.getWorld(world.getName());
        if (liminalWorld != null) {
            String nextLevel = liminalWorld.getNextLevel(player.getLocation());
            if (nextLevel != null && !nextLevel.isEmpty()) {
                Location entryLocation = controller.getEntryLocation(nextLevel);
                event.setTo(entryLocation);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        LiminalWorld defaultWorld = controller.getDefaultWorld();
        if (defaultWorld != null) {
            event.getEntity().setRespawnLocation(defaultWorld.getSpawnLocation(), true);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        LiminalWorld liminalWorld = controller.getWorld(player.getWorld().getName());
        if (liminalWorld != null) {
            liminalWorld.enter(player);
        }
    }
}
