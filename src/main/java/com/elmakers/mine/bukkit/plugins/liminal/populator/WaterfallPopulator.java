package com.elmakers.mine.bukkit.plugins.liminal.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.LiminalRoom;

public class WaterfallPopulator extends LiminalPopulator {
    private int EXIT_LEVEL = -32;
    private boolean COMMAND_BLOCKS_ENABLED = true;
    private final String nextLevel;

    public WaterfallPopulator(LiminalRoom room, ConfigurationSection config) {
        super(room);

        COMMAND_BLOCKS_ENABLED = config.getBoolean("command_blocks", COMMAND_BLOCKS_ENABLED);
        EXIT_LEVEL = config.getInt("exit_level", EXIT_LEVEL);
        nextLevel = config.getString("next_level");
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final LiminalController controller = getController();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = room.getFloorLevel();
        final int bedrockLevel = room.getBedrockLevel();

        if (COMMAND_BLOCKS_ENABLED) {
            final int commandY = floorLevel - 4;
            final int commandX = chunkGlobalX + 4;
            final int commandZ = chunkGlobalZ + 4;

            BlockData commandBlock = controller.getServer().createBlockData(Material.REPEATING_COMMAND_BLOCK);
            CommandBlock command = (CommandBlock)commandBlock;
            command.setConditional(false);

            region.setBlockData(commandX, commandY, commandZ, command);
            BlockState commandState = region.getBlockState(commandX, commandY, commandZ);
            if (commandState instanceof org.bukkit.block.CommandBlock) {
                org.bukkit.block.CommandBlock commandBlockState = (org.bukkit.block.CommandBlock)commandState;
                commandBlockState.setCommand("/particle explosion_emitter ~4 ~1 ~4");
                commandState.update(true);
            }

            BlockData observer1Data = controller.getServer().createBlockData(Material.OBSERVER);
            BlockData observer2Data = controller.getServer().createBlockData(Material.OBSERVER);
            if (observer1Data instanceof Directional) {
                ((Directional)observer1Data).setFacing(BlockFace.EAST);
            }
            if (observer2Data instanceof Directional) {
                ((Directional)observer2Data).setFacing(BlockFace.WEST);
            }
            region.setBlockData(commandX + 1, commandY, commandZ, observer1Data);
            region.setBlockData(commandX + 2, commandY, commandZ, observer2Data);

            // Sounds
            region.setBlockData(commandX + 3, commandY, commandZ, commandBlock);
            commandState = region.getBlockState(commandX + 3, commandY, commandZ);
            if (commandState instanceof org.bukkit.block.CommandBlock) {
                org.bukkit.block.CommandBlock commandBlockState = (org.bukkit.block.CommandBlock)commandState;
                commandBlockState.setCommand("/playsound minecraft:entity.boat.paddle_water ambient @p ~4 ~1 ~4 10");
                commandState.update(true);
            }

        }
        BlockData airData = controller.getServer().createBlockData(Material.AIR);
        BlockData quartzData = controller.getServer().createBlockData(Material.QUARTZ_BLOCK);
        BlockData portalData = controller.getServer().createBlockData(Material.END_PORTAL);
        BlockData waterData = controller.getServer().createBlockData(Material.WATER);

        for (int relativeX = 5; relativeX <= 11; relativeX++) {
            for (int relativeZ = 5; relativeZ <= 11; relativeZ++) {
                final int x = chunkGlobalX + relativeX;
                final int z = chunkGlobalZ + relativeZ;

                // Hole in floor
                for (int y = bedrockLevel; y <= floorLevel; y++) {
                    region.setBlockData(x, y, z, airData);
                }

                // Shaft downward
                if (relativeX == 5 || relativeZ == 5 || relativeX == 11 || relativeZ == 11) {
                    // Walls
                    for (int y = EXIT_LEVEL; y < floorLevel; y++) {
                        region.setBlockData(x, y, z, quartzData);
                    }

                    // Waterfall
                    region.setBlockData(x, floorLevel, z, waterData);
                }

                // Exit gateway
                region.setBlockData(x, EXIT_LEVEL, z, portalData);
            }
        }
    }

    public void checkNewChunk(Chunk chunk) {
        final int floorLevel = room.getFloorLevel();
        final int commandY = floorLevel - 4;
        Block checkObserver = chunk.getBlock(5, commandY, 4);
        if (checkObserver.getType() == Material.OBSERVER) {
            BlockData blockData = checkObserver.getBlockData();
            if (blockData instanceof Powerable) {
                Powerable powerable = (Powerable)blockData;
                powerable.setPowered(true);
                checkObserver.setBlockData(blockData);
            }

            // Trigger waterfall
            chunk.getBlock(6, floorLevel, 6).setType(Material.WATER);
            chunk.getBlock(10, floorLevel, 6).setType(Material.WATER);
            chunk.getBlock(10, floorLevel, 10).setType(Material.WATER);
            chunk.getBlock(6, floorLevel, 10).setType(Material.WATER);
        }
    }

    @Override
    public String getNextLevel() {
        return nextLevel;
    }
}
