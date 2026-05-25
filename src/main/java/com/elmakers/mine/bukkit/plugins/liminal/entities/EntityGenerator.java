package com.elmakers.mine.bukkit.plugins.liminal.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;

public class EntityGenerator implements Listener {
    private final LiminalController controller;
    private final NamespacedKey entityKey;
    private final Map<String, LiminalEntity> entities = new HashMap<>();

    public EntityGenerator(LiminalController controller) {
        this.controller = controller;
        this.entityKey = new NamespacedKey(controller.getPlugin(), "id");
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::checkEntities, 0, 1);
    }

    public void reset() {
        entities.clear();
    }

    public void load(ConfigurationSection generalConfig, ConfigurationSection entityConfigs) {
        for (String key : entityConfigs.getKeys(false)) {
            LiminalEntity entity = new LiminalEntity(this, key, entityConfigs.getConfigurationSection(key));
            if (entity.isValid()) {
                this.entities.put(key, entity);
            }
        }
    }

    public void checkEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                final String entityId = getEntityId(entity);
                final LiminalEntity liminalEntity = entityId == null ? null : entities.get(entityId);
                if (liminalEntity != null) {
                    checkPassengers(liminalEntity, entity);
                }
            }
        }
    }

    private void checkPassengers(LiminalEntity liminal, Entity entity) {
        final Location location = entity.getLocation();
        final float rotationSpeed = (float)liminal.getRotationSpeed();
        for (Entity passenger : entity.getPassengers()) {
            final float yaw = passenger.getLocation().getYaw();
            final float targetYaw = applyRotation(location.getYaw(), yaw, rotationSpeed);
            final float pitch = passenger.getLocation().getPitch();
            final float targetPitch = applyRotation(location.getPitch(), pitch, rotationSpeed);

            passenger.setRotation(targetYaw, targetPitch);
            checkPassengers(liminal, passenger);
        }
    }

    protected static float applyRotation(float targetRot, float rot, float rotationSpeed) {
        while (targetRot - rot < -180.0F) {
            rot -= 360.0F;
        }

        while (targetRot - rot >= 180.0F) {
            rot += 360.0F;
        }

        final float rotDelta = targetRot - rot;
        return rot + Math.signum(rotDelta) * Math.min(rotationSpeed, Math.abs(rotDelta));

    }

    public LiminalEntity getEntity(ConfigurationSection config) {
        if (config == null) {
            return null;
        }
        final String typeId = config.getString("type");
        if (typeId == null) {
            controller.getLogger().warning("Entity configuration missing type");
            return null;
        }
        LiminalEntity entity = entities.get(typeId);
        if (entity == null) {
            entity = new LiminalEntity(this, typeId, config);
        }
        return entity != null && entity.isValid() ? entity : null;
    }

    public Entity spawnEntity(String entityId, Location location) {
        ConfigurationSection config = new MemoryConfiguration();
        config.set("type", entityId);
        LiminalEntity liminal = getEntity(config);
        if (liminal == null) {
            return null;
        }
        return liminal.spawn(location.getWorld(), location);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (isLiminal(entity)) {
            handleEntityDeath(entity);
        }
    }

    private void handleEntityDeath(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            handleEntityDeath(passenger);
            passenger.remove();
        }
    }

    private String getEntityId(Entity entity) {
        return entity.getPersistentDataContainer().get(entityKey, PersistentDataType.STRING);
    }

    private boolean isLiminal(Entity entity) {
        return entity.getPersistentDataContainer().has(entityKey);
    }

    public LiminalController getController() {
        return controller;
    }

    public NamespacedKey getEntityKey() {
        return entityKey;
    }

    public List<String> getEntityKeys() {
        return new ArrayList<>(entities.keySet());
    }
}
