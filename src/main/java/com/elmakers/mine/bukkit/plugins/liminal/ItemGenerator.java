package com.elmakers.mine.bukkit.plugins.liminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ItemGenerator {
    private final LiminalController controller;
    private final Map<String, ItemStack> items = new HashMap<>();

    public ItemGenerator(LiminalController controller) {
        this.controller = controller;
    }

    public void reset() {
        items.clear();
    }

    public void load(ConfigurationSection generalConfig, ConfigurationSection itemConfigs) {
        final Plugin plugin = controller.getPlugin();
        for (String itemKey : itemConfigs.getKeys(false)) {
            ConfigurationSection itemConfig = itemConfigs.getConfigurationSection(itemKey);
            String itemData = itemConfig.getString("item");
            if (itemData == null || itemData.isEmpty()) {
                plugin.getLogger().warning("Missing item data for " + itemKey);
                continue;
            }
            ItemStack itemStack = Bukkit.getItemFactory().createItemStack(itemData);
            if (itemStack == null) {
                plugin.getLogger().warning("Invalid item data for " + itemKey);
                continue;
            }
            String name = itemConfig.getString("name");
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (name != null && !name.isEmpty()) {
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
            List<String> lore = itemConfig.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                itemMeta.setLore(lore);
            }
            itemStack.setItemMeta(itemMeta);
            items.put(itemKey, itemStack);
        }
    }

    public List<String> getItemKeys() {
        return new ArrayList<>(items.keySet());
    }

    public ItemStack createItem(String id) {
        ItemStack item = items.get(id);
        return item == null ? null : item.clone();
    }
}
