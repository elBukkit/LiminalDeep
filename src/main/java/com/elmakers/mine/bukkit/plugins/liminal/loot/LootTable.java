package com.elmakers.mine.bukkit.plugins.liminal.loot;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.liminal.generator.LiminalGenerator;
import com.elmakers.mine.bukkit.plugins.liminal.random.WeightedList;

public class LootTable extends WeightedList<LootDrop>  {
    private final LiminalGenerator generator;
    private double probability;

    public LootTable(LiminalGenerator generator, ConfigurationSection config) {
        this.generator = generator;
        probability = config.getDouble("probability", 1.0);
        final ConfigurationSection dropsConfig = config.getConfigurationSection("drops");
        for (String key : dropsConfig.getKeys(false)) {
            LootDrop drop = new LootDrop(generator, dropsConfig.getConfigurationSection(key));
            this.add(drop);
        }
    }

    public boolean isPresent(final Random random) {
        return !this.isEmpty() && random.nextDouble() < probability;
    }
}
