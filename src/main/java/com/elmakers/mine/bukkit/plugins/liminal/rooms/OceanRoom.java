package com.elmakers.mine.bukkit.plugins.liminal.rooms;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.tasks.StalkerTask;

public class OceanRoom extends LiminalRoom {
    private int SEA_LEVEL = 190;
    private int SAND_LEVEL = 6;
    private int BEDROCK_LEVEL = 1;

    public OceanRoom(LiminalWorld world, ConfigurationSection config) {
        super(world, config);

        getPlugin().getServer().getScheduler().runTaskTimer(
            getPlugin(),
            new StalkerTask(world),
            0L,
            10L
        );
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final int minY = worldInfo.getMinHeight();
        final int seaLevel = SEA_LEVEL;
        final int sandLevel = minY + SAND_LEVEL;
        final int bedrockLevel = minY + BEDROCK_LEVEL;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = seaLevel; y > sandLevel; y--) {
                    chunk.setBlock(x, y, z, Material.WATER);
                }
                for (int y = sandLevel; y >= bedrockLevel; y--) {
                    chunk.setBlock(x, y, z, Material.SAND);
                }
                chunk.setBlock(x, minY, z, Material.BEDROCK);
            }
        }
    }

    @Override
    public Location getSpawnLocation(World world) {
        final int maxY = world.getMaxHeight();
        return new Location(world, 0, maxY - SEA_LEVEL + 1, 0);
    }

    @Override
    public Location getEntryLocation(World world) {
        Location location = getSpawnLocation(world);
        location.setY(world.getMaxHeight());
        return location;
    }

    @Override
    public int getFloorLevel() {
        return SAND_LEVEL;
    }

    @Override
    public int getBedrockLevel() {
        return BEDROCK_LEVEL;
    }
}
