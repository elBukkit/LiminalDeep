package com.elmakers.mine.bukkit.plugins.liminal.tasks;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.entities.LiminalEntity;

public class StalkerTask implements Consumer<BukkitTask> {
    private final LiminalWorld world;
    private final LiminalEntity entity;
    private final Map<UUID, WeakReference<Entity>> stalkers = new HashMap<>();

    public StalkerTask(LiminalWorld world, LiminalEntity entity) {
        this.world = world;
        this.entity = entity;
    }

    protected Entity spawnStalker(Player stalked) {
        Location spawnLocation = stalked.getLocation();
        spawnLocation.setY(0);
        while (spawnLocation.getBlock().getType() != Material.WATER) {
            spawnLocation.add(0, 1, 0);
        }
        spawnLocation.add(0, 4, 0);
        Entity entity = this.entity.spawn(world.getWorld(), spawnLocation);
        if (entity instanceof final Mob mob) {
            mob.setTarget(stalked);
        }
        return entity;

    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        List<Player> players = world.getWorld().getPlayers();
        Set<UUID> worldPlayers = new HashSet<>();
        for (Player player : players) {
            worldPlayers.add(player.getUniqueId());
            WeakReference<Entity> stalkerReference = stalkers.get(player.getUniqueId());
            Entity stalker = stalkerReference == null ? null : stalkerReference.get();
            if (stalker == null || !stalker.isValid()) {
                Entity newStalker = spawnStalker(player);
                stalkers.put(player.getUniqueId(), new WeakReference<>(newStalker));
            }
        }
        Set<UUID> stalkerPlayers = new HashSet<>(stalkers.keySet());
        for (UUID playerId : stalkerPlayers) {
            if (!worldPlayers.contains(playerId)) {
                WeakReference<Entity> stalkerReference = stalkers.get(playerId);
                Entity stalker = stalkerReference.get();
                if (stalker != null && stalker.isValid()) {
                    stalker.remove();
                }
                stalkers.remove(playerId);
            }
        }
    }
}
