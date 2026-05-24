package com.elmakers.mine.bukkit.plugins.liminal.entities;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;


public class EntityGenerator implements Listener {

    private final LiminalController controller;
    private final Map<String, LiminalEntity> entities = new HashMap<>();

    public EntityGenerator(LiminalController controller, ConfigurationSection generalConfig, ConfigurationSection entityConfigs) {
        this.controller = controller;
        for (String key : entityConfigs.getKeys(false)) {
            LiminalEntity entity = new LiminalEntity(controller, entityConfigs.getConfigurationSection(key));
            if (entity.isValid()) {
                this.entities.put(key, entity);
            }
        }
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::checkEntities, 0, 4);
    }

    public void checkEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                checkPassengers(entity);
            }
        }
    }

    private void checkPassengers(Entity entity) {
        final Location location = entity.getLocation();
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof ItemDisplay) {
                passenger.setRotation(location.getYaw(), location.getPitch());
                checkPassengers(passenger);
            }
        }
    }

    public LiminalEntity getEntity(ConfigurationSection config) {
        final String typeId = config.getString("type");
        if (typeId == null) {
            controller.getLogger().warning("Entity configuration missing type");
            return null;
        }
        LiminalEntity entity = entities.get(typeId);
        if (entity == null) {
            entity = new LiminalEntity(controller, config);
        }
        return entity != null && entity.isValid() ? entity : null;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        handleEntityDeath(entity);
    }

    private void handleEntityDeath(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof ItemDisplay) {
                handleEntityDeath(passenger);
                passenger.remove();
            }
        }
    }
}
