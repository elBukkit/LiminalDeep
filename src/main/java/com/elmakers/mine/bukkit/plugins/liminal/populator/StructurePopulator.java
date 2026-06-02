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

public class StructurePopulator extends LiminalPopulator {
    private final String nextLevel;

    private Material[] WALL_BLOCKS = {
            Material.GRAY_CONCRETE,
            Material.LIGHT_GRAY_CONCRETE,
            Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE_POWDER
    };

    public StructurePopulator(LiminalRoom room, ConfigurationSection config) {
        super(room);
        final LiminalController controller = room.getWorld().getController();
        WALL_BLOCKS = controller.getMaterials(config, "floor_blocks", WALL_BLOCKS);
        nextLevel = config.getString("next_level");
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int minY = worldInfo.getMinHeight();
        final int maxHeight = worldInfo.getMaxHeight();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = minY + room.getFloorLevel();

        final int buffer = region.getBuffer();
        final int minX = chunkGlobalX - buffer;
        final int maxX = chunkGlobalX + 16 + buffer;
        final int minZ = chunkGlobalZ - buffer;
        final int maxZ = chunkGlobalZ + 16 + buffer;

        final Material wallBlock = WALL_BLOCKS[random.nextInt(WALL_BLOCKS.length)];
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // if (x != 0 && z != 0 && x != maxX - 1 && z != maxZ - 1) continue;
                for (int y = floorLevel; y < maxHeight; y++) {
                    region.setType(x, y, z, wallBlock);
                }
            }
        }
    }

    @Override
    public String getNextLevel() {
        return nextLevel;
    }
}
