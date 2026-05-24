package com.elmakers.mine.bukkit.plugins.liminal.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.populator.LiminalPopulator;

public abstract class LiminalRoom extends ChunkGenerator {
    protected final LiminalWorld world;
    private final double weight;
    private final int minDistanceSquared;
    private final int maxDistanceSquared;

    private final List<LiminalPopulator> populators = new ArrayList<>();

    public LiminalRoom(LiminalWorld world, ConfigurationSection config) {
        this.world = world;
        this.weight = config.getDouble("weight", 1.0);
        final int minDistance = config.getInt("min_distance", 0);
        final int maxDistance = config.getInt("max_distance", 0);
        minDistanceSquared = minDistance * minDistance;
        maxDistanceSquared = maxDistance * maxDistance;

        final ConfigurationSection populatorsConfig = config.getConfigurationSection("populators");
        if (populatorsConfig != null) {
            for (String populatorKey : populatorsConfig.getKeys(false)) {
                ConfigurationSection populatorConfig = populatorsConfig.getConfigurationSection(populatorKey);
                LiminalPopulator populator = world.createPopulator(this, populatorConfig);
                if (populator == null) continue;
                populators.add(populator);
            }
        } else {
            List<String> populatorList = config.getStringList("populators");
            if (populatorList != null) {
                for (String populatorId : populatorList) {
                    LiminalPopulator populator = world.createPopulator(this, populatorId);
                    if (populator == null) continue;
                    populators.add(populator);
                }
            }
        }
    }

    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        for (LiminalPopulator populator : populators) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    public abstract Location getSpawnLocation(World world);

    public Location getEntryLocation(World world) {
        Location location = getSpawnLocation(world);
        location.setY(world.getMaxHeight() - 16);
        return location;
    }

    public void checkNewChunk(Chunk chunk) {
        for (LiminalPopulator populator : populators) {
            populator.checkNewChunk(chunk);
        }
    }

    public double getWeight(int chunkX, int chunkZ) {
        if (minDistanceSquared >= maxDistanceSquared) {
            return weight;
        }
        final long distanceSquared = ((long)chunkX * 16) * ((long)chunkX * 16) + ((long)chunkZ * 16) * ((long)chunkZ * 16);
        if (distanceSquared < minDistanceSquared) {
            return 0;
        }
        return weight * Math.min(1.0, ((double)distanceSquared - minDistanceSquared) / (maxDistanceSquared - minDistanceSquared));
    }

    public LiminalWorld getWorld() {
        return world;
    }

    public Plugin getPlugin() {
        return world.getPlugin();
    }

    public String getNextLevel() {
        for (LiminalPopulator populator : populators) {
            String nextLevel = populator.getNextLevel();
            if (nextLevel != null) {
                return nextLevel;
            }
        }
        return null;
    }

    public abstract int getFloorLevel();

    public abstract int getBedrockLevel();
}
