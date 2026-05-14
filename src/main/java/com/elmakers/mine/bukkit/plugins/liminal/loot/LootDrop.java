package com.elmakers.mine.bukkit.plugins.liminal.loot;

import java.util.List;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.liminal.generator.LiminalGenerator;
import com.elmakers.mine.bukkit.plugins.liminal.random.WeightedElement;

public class LootDrop implements WeightedElement {
    private final LiminalGenerator generator;
    private final String blockData;
    private final List<String> items;
    private final double weight;

    public LootDrop(LiminalGenerator generator, ConfigurationSection config) {
        this.generator = generator;
        blockData = config.getString("block");
        items = config.getStringList("items");
        weight = config.getDouble("weight", 1.0);
    }

    public BlockData getBlockData() {
        return generator.getPlugin().getServer().createBlockData(blockData);
    }

    public List<String> getItems() {
        return items;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}
