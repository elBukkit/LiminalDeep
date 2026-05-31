package com.elmakers.mine.bukkit.plugins.liminal.entities;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;

public class LiminalEntity {
    private final String id;
    private final EntityGenerator generator;
    private final EntityType entityType;
    private final boolean invisible;
    private final String itemId;
    private final double rotationSpeed;
    private final double scale;
    private final double itemScale;
    private final boolean persistent;
    private final String name;

    public LiminalEntity(EntityGenerator generator, String id, ConfigurationSection config) {
        this.generator = generator;
        this.id = id;
        String typeString = config.getString("type");
        EntityType entityType = null;
        try {
            entityType = EntityType.valueOf(typeString.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            generator.getController().getLogger().warning("Invalid entity type: " + typeString);
        }
        this.entityType = entityType;
        itemId = config.getString("item");
        invisible = config.getBoolean("invisible", itemId != null);
        rotationSpeed = config.getDouble("rotation_speed", 360);
        scale = config.getDouble("scale", 1.0);
        itemScale = config.getDouble("item_scale", 1.0);
        persistent = config.getBoolean("persistent", true);
        name = config.getString("name");
    }

    public boolean isValid() {
        return entityType != null;
    }

    public Entity spawn(RegionAccessor region, Location location) {
        final LiminalController controller = generator.getController();
        Entity entity = region.spawnEntity(location, entityType);
        entity.getPersistentDataContainer().set(generator.getEntityKey(), PersistentDataType.STRING, id);
        if (entity instanceof final LivingEntity li) {
            if (invisible) {
                li.setInvisible(true);
            }
            if (scale != 1) {
                li.getAttribute(Attribute.SCALE).setBaseValue(10);
            }
        }
        entity.setPersistent(persistent);
        if (itemId != null) {
            ItemStack itemStack = controller.createItem(itemId);
            if (itemStack != null) {
                ItemDisplay itemDisplay = (ItemDisplay)region.spawnEntity(location, EntityType.ITEM_DISPLAY);
                itemDisplay.setItemStack(itemStack);
                itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                entity.addPassenger(itemDisplay);
                if (itemScale != 1) {
                    Transformation transformation = itemDisplay.getTransformation();
                    transformation.getScale().set(itemScale);
                    itemDisplay.setTransformation(transformation);
                }
                itemDisplay.setPersistent(persistent);
            }
        }
        if (name != null) {
            entity.setCustomName(name);
        }
        if (entity instanceof final LivingEntity li) {
            li.getEquipment().clear();
        }
        return entity;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }
}
