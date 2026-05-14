package com.elmakers.mine.bukkit.plugins.liminal.loot;

import java.util.List;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.random.WeightedElement;

public class LootDrop implements WeightedElement {
    private final LiminalWorld world;
    private final String blockData;
    private final List<String> items;
    private final double weight;

    public LootDrop(LiminalWorld world, ConfigurationSection config) {
        this.world = world;
        blockData = config.getString("block");
        items = config.getStringList("items");
        weight = config.getDouble("weight", 1.0);
        if (blockData == null) {
            world.getLogger().severe("Missing block data for loot drop");
        }
    }

    public BlockData getBlockData() {
        return world.getPlugin().getServer().createBlockData(blockData);
    }

    public List<String> getItems() {
        return items;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}
