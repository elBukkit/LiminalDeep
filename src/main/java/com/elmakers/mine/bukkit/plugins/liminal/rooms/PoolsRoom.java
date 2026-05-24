package com.elmakers.mine.bukkit.plugins.liminal.rooms;

import java.util.Locale;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.GlowLichen;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;
import com.elmakers.mine.bukkit.plugins.liminal.loot.FoodType;
import com.elmakers.mine.bukkit.plugins.liminal.random.RandomUtils;

public class PoolsRoom extends LiminalRoom {
    private int BEDROCK_LEVEL = 60;
    private int FLOOR_LEVEL = 62;
    private int ROOF_MIN_HEIGHT = 4;
    private int ROOF_MAX_HEIGHT = 10;
    private int DOORWAY_MIN_HEIGHT = 2;
    private int DOORWAY_MAX_HEIGHT = 4;
    private int DOORWAY_MIN_WIDTH_HALF = 0;
    private int DOORWAY_MAX_WIDTH_HALF = 3;
    private int WALKWAY_MAX_WIDTH_HALF = 5;
    private int HALLWAY_MAX_WIDTH_HALF = 0;
    private int HALLWAY_MIN_WIDTH_HALF = 0;
    private int FOOD_LOCATION = 1;
    private double WALL_PROBABILITY = 0.75;
    private double WINDOW_PROBABILITY = 0.3;
    private double ISLAND_PROBABILITY = 0.75;
    private double POOL_PROBABILITY = 0.75;
    private double DOUBLE_DOOR_PROBABILITY = 0.5;
    private double FOOD_PROBABILITY = 0.01;
    private double UNDERWATER_FOOD_PROBABILITY = 1;
    private double LIGHT_PROBABILITY = 1;
    private double FLOOR_LIGHT_PROBABILITY = 0;
    private double SUNROOF_PROBABILITY = 1;
    private double FLOODING_PROBABILITY = 0;
    private int FLOODING_MIN_LEVEL = 1;
    private int FLOODING_MAX_LEVEL = 6;
    private int WATER_DEPTH_MAX = 1;
    private FoodType foodType = FoodType.VINES;
    private final BlockData foodBlock;

    private Material[] FLOOR_BLOCKS = {
        Material.BLUE_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE
    };

    private Material[] WALL_BLOCKS = {
        Material.POLISHED_DIORITE,
        Material.DIORITE
    };

    private Material[] CEILING_BLOCKS = {
            Material.POLISHED_DIORITE,
            Material.DIORITE
    };

    private Material[] LIGHT_BLOCKS = {
            Material.SEA_LANTERN
    };

    public PoolsRoom(LiminalWorld world, ConfigurationSection config) {
        super(world, config);

        BEDROCK_LEVEL = config.getInt("bedrock_level", BEDROCK_LEVEL);
        FLOOR_LEVEL = config.getInt("floor_level", FLOOR_LEVEL);
        ROOF_MIN_HEIGHT = config.getInt("roof_min_height", ROOF_MIN_HEIGHT);
        ROOF_MAX_HEIGHT = config.getInt("roof_max_height", ROOF_MAX_HEIGHT);
        DOORWAY_MIN_HEIGHT = config.getInt("doorway_min_height", DOORWAY_MIN_HEIGHT);
        DOORWAY_MAX_HEIGHT = config.getInt("doorway_max_height", DOORWAY_MAX_HEIGHT);
        DOORWAY_MAX_WIDTH_HALF = config.getInt("doorway_max_width_half", DOORWAY_MAX_WIDTH_HALF);
        DOORWAY_MIN_WIDTH_HALF = config.getInt("doorway_min_width_half", DOORWAY_MIN_WIDTH_HALF);
        WALKWAY_MAX_WIDTH_HALF = config.getInt("walkway_max_width_half", WALKWAY_MAX_WIDTH_HALF);
        WALL_PROBABILITY = config.getDouble("wall_probability", WALL_PROBABILITY);
        WINDOW_PROBABILITY = config.getDouble("window_probability", WINDOW_PROBABILITY);
        ISLAND_PROBABILITY = config.getDouble("island_probability", ISLAND_PROBABILITY);
        POOL_PROBABILITY = config.getDouble("pool_probability", POOL_PROBABILITY);
        FOOD_LOCATION = config.getInt("food_location", FOOD_LOCATION);
        DOUBLE_DOOR_PROBABILITY = config.getDouble("double_door_probability", DOUBLE_DOOR_PROBABILITY);
        FOOD_PROBABILITY = config.getDouble("food_probability", FOOD_PROBABILITY);
        UNDERWATER_FOOD_PROBABILITY = config.getDouble("underwater_food_probability", UNDERWATER_FOOD_PROBABILITY);
        LIGHT_PROBABILITY = config.getDouble("light_probability", LIGHT_PROBABILITY);
        FLOOR_LIGHT_PROBABILITY = config.getDouble("floor_light_probability", FLOOR_LIGHT_PROBABILITY);
        HALLWAY_MAX_WIDTH_HALF = config.getInt("hallway_max_width_half", HALLWAY_MAX_WIDTH_HALF);
        HALLWAY_MIN_WIDTH_HALF = config.getInt("hallway_min_width_half", HALLWAY_MIN_WIDTH_HALF);
        SUNROOF_PROBABILITY = config.getDouble("sunroof_probability", SUNROOF_PROBABILITY);
        FLOODING_PROBABILITY = config.getDouble("flooding_probability", FLOODING_PROBABILITY);
        FLOODING_MIN_LEVEL = config.getInt("flooding_min_level", FLOODING_MIN_LEVEL);
        FLOODING_MAX_LEVEL = config.getInt("flooding_mx_level", FLOODING_MAX_LEVEL);
        WATER_DEPTH_MAX =  config.getInt("water_depth_max", WATER_DEPTH_MAX);
        String foodBlockData = config.getString("food_block", "");
        foodBlock = foodBlockData.isEmpty() ? null : world.getPlugin().getServer().createBlockData(foodBlockData);

        String foodTypeString = config.getString("food_type", foodType.name());
        try {
            foodType = FoodType.valueOf(foodTypeString.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            getPlugin().getLogger().warning("Invalid food type: " + foodTypeString);
        }

        final LiminalController controller = world.getController();
        FLOOR_BLOCKS = controller.getMaterials(config, "floor_blocks", FLOOR_BLOCKS);
        WALL_BLOCKS = controller.getMaterials(config, "wall_blocks", WALL_BLOCKS);
        CEILING_BLOCKS = controller.getMaterials(config, "ceiling_blocks", CEILING_BLOCKS);
        LIGHT_BLOCKS = controller.getMaterials(config, "light_blocks", LIGHT_BLOCKS);
    }

    private BlockData getWindowBlock() {
        BlockData gatewayData = world.getPlugin().getServer().createBlockData(Material.END_GATEWAY);
        if (gatewayData instanceof EndGateway) {
            EndGateway gateway = (EndGateway)gatewayData;
            gateway.setAge(-Integer.MAX_VALUE);
        }
        return gatewayData;
    }

    private void makeFood(int x, int z, int minY, int maxY, @NonNull ChunkData chunk) {
        if (foodBlock == null) return;
        for (int y = minY; y <= maxY; y++) {
            chunk.setBlock(x, y, z, foodBlock);
        }
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final LiminalController controller = world.getController();
        final boolean isStartingChunk = chunkX == 0 && chunkZ == 0;
        final int floorLevel = FLOOR_LEVEL;
        final int roofLevel = floorLevel + RandomUtils.range(random, ROOF_MIN_HEIGHT, ROOF_MAX_HEIGHT);
        final int roofMaxLevel = floorLevel + ROOF_MAX_HEIGHT;
        final int doorwayLevel = Math.min(roofLevel, floorLevel + RandomUtils.range(random, DOORWAY_MIN_HEIGHT, DOORWAY_MAX_HEIGHT));
        final int doorwayWidthHalf = RandomUtils.range(random, DOORWAY_MIN_WIDTH_HALF, DOORWAY_MAX_WIDTH_HALF);
        final int doorwayLeft = 7 - doorwayWidthHalf;
        final int doorwayRight = 9 + doorwayWidthHalf;
        final int walkwayWidthHalf = isStartingChunk ? 0 : random.nextInt(WALKWAY_MAX_WIDTH_HALF);
        final int walkwayLeft = 8 - walkwayWidthHalf;
        final int walkWayRight = 8 + walkwayWidthHalf;
        final boolean canHaveWindow = doorwayWidthHalf < 4;
        int xWindowLocation = 0;
        int zWindowLocation = 0;
        if (canHaveWindow) {
            xWindowLocation = random.nextInt(4 - doorwayWidthHalf) + 1;
            if (random.nextDouble() > 0.5) xWindowLocation = 15 - xWindowLocation;
            zWindowLocation = random.nextInt(4 - doorwayWidthHalf) + 1;
            if (random.nextDouble() > 0.5) zWindowLocation = 15 - zWindowLocation;
        }
        final boolean hasXWall = random.nextDouble() < WALL_PROBABILITY;
        final boolean hasZWall = random.nextDouble() < WALL_PROBABILITY;
        final boolean hasXWindow = canHaveWindow && random.nextDouble() < WINDOW_PROBABILITY;
        final boolean hasZWindow = canHaveWindow && random.nextDouble() < WINDOW_PROBABILITY;
        final boolean hasIsland = !isStartingChunk && random.nextDouble() < ISLAND_PROBABILITY;
        final boolean hasPools = random.nextDouble() < POOL_PROBABILITY;
        final boolean hasSunRoof = isStartingChunk || random.nextDouble() < SUNROOF_PROBABILITY;
        final boolean hasDoubleDoor = random.nextDouble() < DOUBLE_DOOR_PROBABILITY;
        final boolean doorXSide = random.nextDouble() < 0.5;
        final boolean hasXDoor = hasDoubleDoor || doorXSide;
        final boolean hasZDoor = hasDoubleDoor || !doorXSide;
        final boolean hasFood = random.nextDouble() < FOOD_PROBABILITY;
        final boolean hasFoodVines = hasFood && foodType == FoodType.VINES;
        final boolean hasUnderwaterFood = hasFood && foodType == FoodType.UNDERWATER;
        final int foodCorner = random.nextInt(4);
        final Material floorBlock = FLOOR_BLOCKS[random.nextInt(FLOOR_BLOCKS.length)];
        final Material wallBlock = WALL_BLOCKS[random.nextInt(WALL_BLOCKS.length)];
        final Material ceilingBlock = CEILING_BLOCKS[random.nextInt(CEILING_BLOCKS.length)];
        final Material lightBlock = LIGHT_BLOCKS[random.nextInt(LIGHT_BLOCKS.length)];
        final int lightsFirst = walkwayLeft / 2 + 1;
        final int lightsSecond = 16 - lightsFirst;
        final boolean isFlooded = hasSunRoof && random.nextDouble() < FLOODING_PROBABILITY;
        final boolean hasFloorLights = random.nextDouble() < FLOOR_LIGHT_PROBABILITY;
        final int waterMinY = floorLevel - WATER_DEPTH_MAX;
        final int lightY = waterMinY;
        Levelled floodWater = null;
        if (isFlooded) {
            int floodLevel = RandomUtils.range(random, FLOODING_MIN_LEVEL, FLOODING_MAX_LEVEL);
            floodWater = (Levelled)controller.getServer().createBlockData(Material.WATER);
            floodWater.setLevel(floodLevel);
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                boolean isUnderwaterFood = random.nextDouble() < UNDERWATER_FOOD_PROBABILITY;
                final BlockData waterBlock = hasUnderwaterFood && isUnderwaterFood ? foodBlock : Material.WATER.createBlockData();
                final boolean hasLight = random.nextDouble() < LIGHT_PROBABILITY;
                final Material lightMaterial = hasLight ? lightBlock : floorBlock;

                // Fill in the sub-floor first
                chunk.setBlock(x, BEDROCK_LEVEL, z, Material.BEDROCK);
                for (int y = BEDROCK_LEVEL + 1; y < FLOOR_LEVEL; y++) {
                    chunk.setBlock(x, y, z, floorBlock);
                }

                final boolean isSunRoof = hasSunRoof && x >= 7 && z >= 7 && x <= 9 && z <= 9;
                final boolean isWalkway = (x > walkwayLeft && x < walkWayRight) || (z > walkwayLeft && z < walkWayRight);
                if (x == 0 || z == 0) {
                    chunk.setBlock(x, floorLevel, z, floorBlock);
                    if ((hasXWall && z == 0) || (hasZWall && x == 0)) {
                        // Walls and doorway
                        boolean isDoorway = (x >= doorwayLeft && x <= doorwayRight) || (z >= doorwayLeft && z <= doorwayRight);
                        if (!hasXDoor && z == 0) isDoorway = false;
                        else if (!hasZDoor && x == 0) isDoorway = false;
                        for (int y = floorLevel + 1; y <= roofMaxLevel; y++) {
                            if (isDoorway && y <= doorwayLevel) continue;
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    } else {
                        for (int y = roofLevel; y <= roofMaxLevel; y++) {
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    }
                } else if (x == 1 || z == 1 || x == 15 || z == 15) {
                    // Border walkway
                    chunk.setBlock(x, floorLevel, z, floorBlock);
                    chunk.setBlock(x, roofLevel, z, ceilingBlock);
                } else if (isWalkway) {
                    // Pathways
                    if (!isSunRoof) {
                        chunk.setBlock(x, roofLevel, z, ceilingBlock);
                    }
                    chunk.setBlock(x, floorLevel, z, floorBlock);
                    if (floodWater != null) {
                        chunk.setBlock(x, floorLevel + 1, z, floodWater);
                    }
                } else if (isSunRoof) {
                    // Island
                    if (!hasIsland) {
                        for (int y = floorLevel; y > waterMinY; y--) {
                            chunk.setBlock(x, y, z, waterBlock);
                        }
                        if (x == 8 && z == 8) {
                            chunk.setBlock(x, lightY, z, lightMaterial);
                        }
                    } else {
                        chunk.setBlock(x, floorLevel, z, floorBlock);
                    }
                } else {
                    // Water and roof
                    chunk.setBlock(x, roofLevel, z, ceilingBlock);
                    final boolean isCenterLight = (x == lightsFirst || x == lightsSecond) && (z == lightsFirst || z == lightsSecond);
                    if (hasPools) {
                        for (int y = floorLevel; y > waterMinY; y--) {
                            chunk.setBlock(x, y, z, waterBlock);
                        }
                        if (isCenterLight) {
                            chunk.setBlock(x, lightY, z, lightMaterial);
                        }
                    } else if (hasFloorLights && isCenterLight) {
                        chunk.setBlock(x, floorLevel, z, lightMaterial);
                    } else {
                        chunk.setBlock(x, floorLevel, z, floorBlock);
                    }
                }

                // Extend ceiling up
                if (!isSunRoof) {
                    final int ceilingHeight = isStartingChunk ? worldInfo.getMaxHeight() : roofMaxLevel;
                    for (int y = roofLevel + 1; y <= ceilingHeight; y++) {
                        chunk.setBlock(x, y, z, ceilingBlock);
                    }
                }

                // Add a dim light if no sunroof so it's not 100% dark
                if (!hasSunRoof && x == 8 && z == 8) {
                    GlowLichen dimLight = (GlowLichen)controller.getServer().createBlockData(Material.GLOW_LICHEN);
                    dimLight.setFace(BlockFace.DOWN, true);
                    BlockData centerBlock = chunk.getBlockData(x, floorLevel, z);
                    boolean isWaterlogged = centerBlock instanceof Waterlogged && ((Waterlogged)centerBlock).isWaterlogged();
                    if (centerBlock.getMaterial() == Material.WATER || isWaterlogged) {
                        dimLight.setWaterlogged(true);
                        chunk.setBlock(x, floorLevel, z, dimLight);
                    } else {
                        chunk.setBlock(x, floorLevel + 1, z, dimLight);
                    }
                }

                // Add food
                if (hasFoodVines) {
                    int foodLow = FOOD_LOCATION;
                    int foodHigh = 16 - FOOD_LOCATION;
                    switch (foodCorner) {
                        case 0:
                            if (x == foodLow && z == foodLow) {
                                makeFood(x, z, floorLevel + 1, roofLevel - 1, chunk);
                            }
                            break;
                        case 1:
                            if (x == foodHigh && z == foodLow) {
                                makeFood(x, z, floorLevel + 1, roofLevel - 1, chunk);
                            }
                            break;
                        case 2:
                            if (x == foodLow && z == foodHigh) {
                                makeFood(x, z, floorLevel + 1, roofLevel - 1, chunk);
                            }
                            break;
                        case 3:
                            if (x == foodHigh && z == foodHigh) {
                                makeFood(x, z, floorLevel + 1, roofLevel - 1, chunk);
                            }
                            break;
                    }
                }

                // Fill in windows after
                if (hasXWall && hasXWindow && x == xWindowLocation && z == 0) {
                    chunk.setBlock(x, floorLevel + 2, z, getWindowBlock());
                } else if (hasZWall && hasZWindow && z == zWindowLocation && x == 0) {
                    chunk.setBlock(x, floorLevel + 2, z, getWindowBlock());
                }

                // Fill in hallways after
                boolean isCenterWalkway = isWalkway || x == 8 || z == 8;
                if (!isCenterWalkway && (HALLWAY_MAX_WIDTH_HALF > 0 || HALLWAY_MIN_WIDTH_HALF > 0)) {
                    int hallwayWidthHalf = RandomUtils.range(random, HALLWAY_MIN_WIDTH_HALF, HALLWAY_MAX_WIDTH_HALF);
                    int hallwayLeft = 8 - hallwayWidthHalf;
                    int hallwayRight = 8 + hallwayWidthHalf;
                    if (isStartingChunk) {
                        hallwayLeft = Math.min(hallwayLeft, 6);
                        hallwayRight = Math.max(hallwayRight, 10);
                    }
                    if (x < hallwayLeft || x > hallwayRight || z < hallwayLeft || z > hallwayRight) {
                        for (int y = waterMinY; y <= roofLevel; y++) {
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Location getSpawnLocation(World world) {
        return new Location(world, 8.5, FLOOR_LEVEL + 1, 8.5);
    }

    @Override
    public int getFloorLevel() {
        return FLOOR_LEVEL;
    }

    @Override
    public int getBedrockLevel() {
        return BEDROCK_LEVEL;
    }
}
