package com.elmakers.mine.bukkit.plugins.liminal.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.entities.LiminalEntitySpawn;
import com.elmakers.mine.bukkit.plugins.liminal.populator.LiminalPopulator;
import com.elmakers.mine.bukkit.plugins.liminal.random.WeightedList;

public abstract class LiminalRoom extends ChunkGenerator {
    protected final LiminalWorld world;
    private final double weight;
    private final int minDistanceSquared;
    private final int maxDistanceSquared;
    private final double entityProbability;
    private final WeightedList<LiminalEntitySpawn> spawns = new WeightedList();

    private final List<LiminalPopulator> populators = new ArrayList<>();

    public LiminalRoom(LiminalWorld world, ConfigurationSection config) {
        this.world = world;
        this.weight = config.getDouble("weight", 1.0);
        final int minDistance = config.getInt("min_distance", 0);
        final int maxDistance = config.getInt("max_distance", 0);
        minDistanceSquared = minDistance * minDistance;
        maxDistanceSquared = maxDistance * maxDistance;
        entityProbability = config.getDouble("entity_probability", 0.0);

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

        final ConfigurationSection entities = config.getConfigurationSection("entities");
        if (entities != null) {
            for (String entityId : entities.getKeys(false)) {
                ConfigurationSection entityConfig = entities.getConfigurationSection(entityId);
                LiminalEntitySpawn spawn = new LiminalEntitySpawn(world, entityConfig);
                if (spawn == null) continue;
                spawns.add(spawn);
            }
        }
    }

    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        for (LiminalPopulator populator : populators) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }

        if (!spawns.isEmpty() && random.nextDouble() < entityProbability) {
            LiminalEntitySpawn spawn = spawns.get(random);
            // The world may not be initialized yet, chunks generate on init
            World world = Bukkit.getWorld(this.world.getName());
            boolean spawned = false;
            if (world != null && spawn != null) {
                final int chunkGlobalX = chunkX << 4;
                final int chunkGlobalZ = chunkZ << 4;
                for (int x = 0; x < 16 && !spawned; x++) {
                    for (int z = 0; z < 16 && !spawned; z++) {
                        // Spawn in center if possible
                        final int xAbs = (x + 8) % 16 + chunkGlobalX;
                        final int zAbs = (z + 8) % 16 + chunkGlobalZ;
                        Location location = new Location(world, xAbs, getFloorLevel() + 1, zAbs);
                        Material block = region.getType(location);
                        if (!block.isSolid()) {
                            spawn.spawn(region, location);
                            spawned = true;
                        }
                    }
                }
            }
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

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return getSpawnLocation(world);
    }

    public abstract int getFloorLevel();

    public abstract int getBedrockLevel();
}
