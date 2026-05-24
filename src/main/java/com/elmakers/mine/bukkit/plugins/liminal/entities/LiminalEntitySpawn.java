package com.elmakers.mine.bukkit.plugins.liminal.entities;

import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.random.WeightedElement;

public class LiminalEntitySpawn implements WeightedElement {
    private final LiminalWorld world;
    private final LiminalEntity entity;
    private final double weight;

    public LiminalEntitySpawn(final LiminalWorld world, final ConfigurationSection config) {
        this.world = world;
        weight = config.getDouble("weight", 1.0);
        entity = world.getController().getEntity(config);
    }

    @Override
    public double getWeight() {
        return weight;
    }

    public void spawn(final RegionAccessor region, Location location) {
        entity.spawn(region, location);
    }
}
