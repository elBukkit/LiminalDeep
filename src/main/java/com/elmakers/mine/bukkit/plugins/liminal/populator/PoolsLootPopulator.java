package com.elmakers.mine.bukkit.plugins.liminal.populator;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorldPlugin;
import com.elmakers.mine.bukkit.plugins.liminal.loot.LootDrop;
import com.elmakers.mine.bukkit.plugins.liminal.loot.LootTable;
import com.elmakers.mine.bukkit.plugins.liminal.generator.LiminalGenerator;

public class PoolsLootPopulator extends LiminalPopulator {
    private final LootTable lootTable;
    private int FLOOR_LEVEL = 62;

    public PoolsLootPopulator(LiminalGenerator generator, ConfigurationSection config) {
        super(generator);

        FLOOR_LEVEL = config.getInt("floor_level", FLOOR_LEVEL);

        ConfigurationSection lootConfig = config.getConfigurationSection("loot");
        if (lootConfig != null) {
            lootTable = new LootTable(generator, lootConfig);
        } else {
            lootTable = null;
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final LiminalWorldPlugin plugin = getPlugin();
        if (lootTable == null || !lootTable.isPresent(random)) return;
        final LootDrop drop = lootTable.get(random);

        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;

        final int lootCorner = random.nextInt(4);
        final int lootSide = random.nextInt(2);

        final int lookDirection = lootSide == 0 ? 1 : -1;
        final int y = FLOOR_LEVEL + 2;
        int x;
        int z;
        int deltaX;
        int deltaZ;
        BlockFace facing;
        switch (lootCorner) {
            case 0:
                x = 8;
                z = 2;
                facing = lookDirection == 1 ? BlockFace.WEST : BlockFace.EAST;
                deltaX = lookDirection;
                deltaZ = 0;
                break;
            case 1:
                x = 8;
                z = 13;
                facing = lookDirection == 1 ? BlockFace.WEST : BlockFace.EAST;
                deltaX = lookDirection;
                deltaZ = 0;
                break;
            case 2:
                x = 2;
                z = 8;
                facing = lookDirection == 1 ? BlockFace.NORTH : BlockFace.SOUTH;
                deltaX = 0;
                deltaZ = lookDirection;
                break;
            case 3:
                x = 13;
                z = 8;
                facing = lookDirection == 1 ? BlockFace.NORTH : BlockFace.SOUTH;
                deltaX = 0;
                deltaZ = lookDirection;
                break;
            default:
                return;
        }

        BlockState blockState = region.getBlockState(chunkGlobalX + x, y, chunkGlobalZ + z);
        while (x >= 0 && z >= 0 && z <= 15 && x <= 15 && blockState.getType() == Material.AIR) {
            x += deltaX;
            z += deltaZ;
            blockState = region.getBlockState(chunkGlobalX + x, y, chunkGlobalZ + z);
        }
        int barrelX = chunkGlobalX + x;
        int barrelZ = chunkGlobalZ + z;
        BlockData containerBlock = drop.getBlockData();
        if (containerBlock instanceof Directional) {
            Directional directional = (Directional)containerBlock;
            directional.setFacing(facing);
        }
        region.setBlockData(barrelX, y, barrelZ, containerBlock);
        final List<String> items = drop.getItems();

        BlockState barrelState = region.getBlockState(barrelX, y, barrelZ);
        if (barrelState instanceof Container && !items.isEmpty()) {
            Container barrel = (Container)barrelState;
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack itemStack = plugin.createItem(items.get(slot));
                if (itemStack == null) {
                    itemStack = new ItemStack(Material.AIR);
                }
                barrel.getInventory().setItem(slot, itemStack);
            }
        }
    }
}
