package com.elmakers.mine.bukkit.plugins.liminal.rooms;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;
import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;

public class FlatRoom extends LiminalRoom {
    private PerlinNoiseGenerator noise;
    private double noiseScale = 0.1;
    private int GROUND_LEVEL = 2;
    private int BEDROCK_LEVEL = 1;
    private double FOOD_PROBABILITY = 0;

    private Material[] FLOOR_BLOCKS = {
            Material.BLACK_CONCRETE,
            Material.BLACK_CONCRETE_POWDER,
            Material.GRAY_CONCRETE,
            Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE,
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.WHITE_CONCRETE,
            Material.WHITE_CONCRETE_POWDER
    };

    public FlatRoom(LiminalWorld world, ConfigurationSection config) {
        super(world, config);
        final LiminalController controller = world.getController();
        GROUND_LEVEL = config.getInt("ground_level", GROUND_LEVEL);
        BEDROCK_LEVEL = config.getInt("bedrock_level", BEDROCK_LEVEL);
        FOOD_PROBABILITY = config.getDouble("food_probability", FOOD_PROBABILITY);
        noiseScale = config.getDouble("noise_scale", noiseScale);
        FLOOR_BLOCKS = controller.getMaterials(config, "floor_blocks", FLOOR_BLOCKS);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        synchronized (this) {
            if (noise == null) {
                noise = new PerlinNoiseGenerator(worldInfo.getSeed());
            }
        }
        final boolean hasFood = random.nextDouble() < FOOD_PROBABILITY;
        final int groundLevel = GROUND_LEVEL;
        final int bedrockLevel = BEDROCK_LEVEL;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;

                final double blockValue = noise.noise(worldX * noiseScale, worldZ * noiseScale);
                final int blockIndex = (int)Math.min(FLOOR_BLOCKS.length - 1, Math.max(0, (blockValue + 1) / 2 * FLOOR_BLOCKS.length));
                final Material floorBlock = FLOOR_BLOCKS[blockIndex];
                for (int y = bedrockLevel + 1; y <= groundLevel; y++) {
                    chunk.setBlock(x, y, z, floorBlock);
                }
                chunk.setBlock(x, BEDROCK_LEVEL, z, Material.BEDROCK);
            }
        }

        if (hasFood) {
            final BlockData blockData = getPlugin().getServer().createBlockData(Material.CARROTS);
            if (blockData instanceof Ageable) {
                Ageable crops = (Ageable)blockData;
                crops.setAge(crops.getMaximumAge());
            }
            for (int x = 6; x < 10; x++) {
                for (int z = 6; z < 10; z++) {
                    if (x == 6 || x == 9 || z == 6 || z == 9) {
                        chunk.setBlock(x, groundLevel + 1, z, Material.PALE_OAK_PRESSURE_PLATE);
                    } else {
                        chunk.setBlock(x, groundLevel + 1, z, blockData);
                    }
                }
            }
        }
    }

    @Override
    public Location getSpawnLocation(World world) {
        return new Location(world, 0, GROUND_LEVEL + 1, 0);
    }

    @Override
    public Location getEntryLocation(World world) {
        return getSpawnLocation(world);
    }

    @Override
    public int getFloorLevel() {
        return GROUND_LEVEL;
    }

    @Override
    public int getBedrockLevel() {
        return BEDROCK_LEVEL;
    }
}
