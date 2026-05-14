package com.elmakers.mine.bukkit.plugins.liminal.loot;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.random.WeightedList;

public class LootTable extends WeightedList<LootDrop>  {
    private final LiminalWorld world;

    public LootTable(LiminalWorld world, ConfigurationSection config) {
        this.world = world;
        final ConfigurationSection dropsConfig = config.getConfigurationSection("drops");
        for (String key : dropsConfig.getKeys(false)) {
            LootDrop drop = new LootDrop(world, dropsConfig.getConfigurationSection(key));
            this.add(drop);
        }
    }
}
