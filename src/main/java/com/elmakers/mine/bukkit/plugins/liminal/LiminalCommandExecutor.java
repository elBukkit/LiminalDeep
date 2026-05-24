package com.elmakers.mine.bukkit.plugins.liminal;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LiminalCommandExecutor implements TabExecutor {
    private final LiminalController controller;

    public LiminalCommandExecutor(LiminalController controller) {
        this.controller = controller;
        final JavaPlugin plugin = controller.getPlugin();

        plugin.getCommand("liminal").setExecutor(this);
        plugin.getCommand("liminal").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length < 1) {
            return false;
        }
        String subCommand = args[0];
        switch (subCommand) {
            case "reload":
                processReloadCommand(sender);
                return true;
        }

        if (args.length < 2) {
            return false;
        }

        switch (subCommand) {
            case "go":
                processGoCommand(sender, args[1]);
                return true;
            case "summon":
                processSummonCommand(sender, args[1]);
                return true;
            case "give":
                if (args.length < 3) {
                    return false;
                }
                processGiveCommand(sender, args[1], args[2]);
                return true;
            default:
                return false;
        }
    }

    private void processGoCommand(CommandSender sender, String level) {
        if (!checkPlayer(sender)) {
            return;
        }
        Player player = (Player)sender;
        if (!controller.sendToLevel(player, level)) {
            sender.sendMessage(ChatColor.RED + "Unable to load world " + level);
        }
    }

    private void processGiveCommand(CommandSender sender, String playerName, String itemId) {
        Player player = controller.getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }
        ItemStack item = controller.createItem(itemId);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Invalid item: " + itemId);
            return;
        }
        player.getInventory().addItem(item);
    }

    private void processSummonCommand(CommandSender sender, String entityId) {
        if (!checkPlayer(sender)) {
            return;
        }
        Player player = (Player)sender;
        Entity entity = controller.spawnEntity(entityId, player.getLocation());
        if (entity == null) {
            player.sendMessage(ChatColor.RED + "Invalid entity: " + entityId);
        }
    }

    private void processReloadCommand(CommandSender sender) {
        controller.reload(sender);
    }

    private boolean checkPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }

        sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 1) {
            return List.of("go", "give", "reload", "summon");
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "go":
                    return controller.getWorldKeys();
                case "give":
                    return controller.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
                case "summon":
                    return controller.getEntityKeys();
            }
        }
        if (args.length == 3) {
            switch (args[0]) {
                case "give":
                    return controller.getItemKeys();
            }
        }
        return null;
    }
}
