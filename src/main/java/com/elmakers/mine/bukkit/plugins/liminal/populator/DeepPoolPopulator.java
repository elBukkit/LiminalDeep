package com.elmakers.mine.bukkit.plugins.liminal.populator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;
import com.elmakers.mine.bukkit.plugins.liminal.random.RandomUtils;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.LiminalRoom;

public class DeepPoolPopulator extends LiminalPopulator {
    private int EXIT_LEVEL = -60;
    private final String nextLevel;

    private Material[] LIGHT_BLOCKS = {
            Material.SEA_LANTERN
    };
    private Material[] WALL_BLOCKS = {
            Material.POLISHED_DIORITE,
            Material.DIORITE
    };

    public DeepPoolPopulator(LiminalRoom room, ConfigurationSection config) {
        super(room);

        final LiminalController controller = getController();
        EXIT_LEVEL = config.getInt("exit_level", EXIT_LEVEL);
        nextLevel = config.getString("next_level");
        LIGHT_BLOCKS = controller.getMaterials(config, "light_blocks", LIGHT_BLOCKS);
        WALL_BLOCKS = controller.getMaterials(config, "wall_blocks", WALL_BLOCKS);
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final LiminalController controller = getController();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = room.getFloorLevel();

        final Material lightBlock = LIGHT_BLOCKS[random.nextInt(LIGHT_BLOCKS.length)];
        final Material wallBlock = WALL_BLOCKS[random.nextInt(WALL_BLOCKS.length)];
        BlockData lightData = controller.getServer().createBlockData(lightBlock);
        BlockData portalData = controller.getServer().createBlockData(Material.END_PORTAL);
        BlockData waterData = controller.getServer().createBlockData(Material.WATER);
        BlockData quartzData = controller.getServer().createBlockData(Material.QUARTZ_BLOCK);
        BlockData wallData = controller.getServer().createBlockData(wallBlock);

        for (int relativeX = 5; relativeX <= 11; relativeX++) {
            for (int relativeZ = 5; relativeZ <= 11; relativeZ++) {
                final int x = chunkGlobalX + relativeX;
                final int z = chunkGlobalZ + relativeZ;

                // Deep pool
                for (int y = EXIT_LEVEL; y <= floorLevel; y++) {
                    region.setBlockData(x, y, z, waterData);
                }

                // Shaft downward
                if (relativeX == 5 || relativeZ == 5 || relativeX == 11 || relativeZ == 11) {
                    // Walls
                    for (int y = EXIT_LEVEL - 1; y < floorLevel; y++) {
                        final double lightChance = RandomUtils.lerp(y, EXIT_LEVEL + 8, floorLevel - 8);
                        final BlockData shaftWall = random.nextDouble() < lightChance ? lightData : wallData;
                        region.setBlockData(x, y, z, shaftWall);
                    }
                    // Rim
                    region.setBlockData(x, floorLevel, z, quartzData);
                }

                // Exit gateway
                region.setBlockData(x, EXIT_LEVEL, z, portalData);
            }
        }
    }

    @Override
    public String getNextLevel() {
        return nextLevel;
    }
}
