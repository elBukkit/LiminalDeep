package com.elmakers.mine.bukkit.plugins.liminal;

import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.liminal.generator.LiminalGenerator;
import com.elmakers.mine.bukkit.plugins.liminal.populator.DeepPoolPopulator;
import com.elmakers.mine.bukkit.plugins.liminal.populator.LiminalPopulator;
import com.elmakers.mine.bukkit.plugins.liminal.populator.WaterfallPopulator;
import com.elmakers.mine.bukkit.plugins.liminal.populator.LootPopulator;
import com.elmakers.mine.bukkit.plugins.liminal.random.RandomUtils;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.LiminalRoom;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.OceanRoom;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.PoolsRoom;

public class LiminalWorld {
    protected final String name;
    protected final LiminalWorldPlugin plugin;
    protected final LiminalGenerator generator;
    protected final int time;
    protected final boolean rain;
    protected final String title;
    protected final int titleDelay;
    private final int minAmbientSoundTime;
    private final int maxAmbientSoundTime;
    private final Random random = new Random();
    protected Long seed;
    private World world;

    private CustomSound[] ambientSounds = {
        CustomSound.of(Sound.AMBIENT_BASALT_DELTAS_ADDITIONS),
        CustomSound.of(Sound.AMBIENT_CAVE),
        CustomSound.of(Sound.AMBIENT_CRIMSON_FOREST_ADDITIONS),
        CustomSound.of(Sound.AMBIENT_NETHER_WASTES_ADDITIONS),
        CustomSound.of(Sound.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS),
        CustomSound.of(Sound.AMBIENT_WARPED_FOREST_ADDITIONS),
        CustomSound.of(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS),
        CustomSound.of(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE),
        CustomSound.of(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE)
    };

    public LiminalWorld(LiminalWorldPlugin plugin, String name, ConfigurationSection generalConfig, ConfigurationSection config) {
        this.plugin = plugin;
        this.name = name;
        time = config.getInt("time", 0);
        title = config.getString("title");
        titleDelay = config.getInt("title_delay", generalConfig.getInt("title_delay", 0));
        rain = config.getBoolean("rain");
        minAmbientSoundTime = config.getInt("min_ambient_sound_time", 0) * 20;
        maxAmbientSoundTime = config.getInt("max_ambient_sound_time", 0) * 20;
        if (config.contains("seed")) {
            seed = config.getLong("seed");
        }
        generator = new LiminalGenerator(this, config);
        ambientSounds = plugin.getSounds(config, "ambient_sounds", ambientSounds);
        scheduleAmbientSounds();
    }

    public String getNextLevel(Location location) {
        return getRoomAt(location.getChunk()).getNextLevel();
    }

    public void enter(Player player) {
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> player.sendTitle(
                        ChatColor.translateAlternateColorCodes('&', title),
                        null,
                        2 * 20,
                        4 * 20,
                        2 * 20
                ),
                titleDelay * 20 / 1000);
    }

    public void checkNewChunk(Chunk chunk) {
        getRoomAt(chunk).checkNewChunk(chunk);
    }

    public Location getSpawnLocation() {
        return getStartingRoom().getSpawnLocation(getWorld());
    }

    public Location getEntryLocation() {
        return getStartingRoom().getEntryLocation(getWorld());
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public LiminalWorldPlugin getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return getWorld(false);
    }

    public World getWorld(boolean reconfigure) {
        boolean configure = reconfigure;
        if (world == null) {
            world = plugin.getServer().getWorld(name);
            configure = true;
            if (world == null) {
                final WorldCreator creator = new WorldCreator(name).generator(generator);
                if (seed != null) {
                    creator.seed(seed);
                } else {
                    seed = creator.seed();
                }
                world = Bukkit.createWorld(creator);
            }
        }
        if (world == null) {
            plugin.getLogger().severe("Unable to create world " + name);
        } else if (configure) {
            configureWorld(world);
        }
        return world;
    }

    private void setGameRule(Registry<GameRule> gameRules, World world, String key, boolean value) {
        GameRule rule = gameRules.get(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
        if (rule != null) {
            world.setGameRule(rule, value);
        } else {
            plugin.getLogger().warning("Invalid game rule: " + key);
        }
    }

    private void configureWorld(World world) {
        final Registry<GameRule> gameRules = plugin.getServer().getRegistry(GameRule.class);

        setGameRule(gameRules, world, "ADVANCE_WEATHER", false);
        setGameRule(gameRules, world, "ADVANCE_TIME", false);
        setGameRule(gameRules, world, "SPAWN_MOBS", false);
        setGameRule(gameRules, world, "SPAWN_MONSTERS", false);
        setGameRule(gameRules, world, "SPAWN_PHANTOMS", false);
        setGameRule(gameRules, world, "SPAWN_PATROLS", false);
        setGameRule(gameRules, world, "COMMAND_BLOCK_OUTPUT", false);
        setGameRule(gameRules, world, "COMMAND_BLOCKS_WORK", true);

        world.setStorm(rain);
        world.setTime(time);
    }

    public LiminalRoom getRoomAt(Chunk chunk) {
        return getRoomAt(chunk.getX(), chunk.getZ());
    }

    public LiminalRoom getRoomAt(int chunkX, int chunkZ) {
        if (seed == null) {
            getLogger().severe("Seed not set for world " + name);
            return null;
        }
        return generator.getRooms().getRoomAt(chunkX, chunkZ, seed);
    }

    public LiminalRoom getStartingRoom() {
        return getRoomAt(0, 0);
    }

    public LiminalRoom createRoom(String id) {
        ConfigurationSection config = plugin.getRoomConfig(id);
        if (config == null) {
            plugin.getLogger().severe("Invalid room id: " + id);
            return null;
        }
        return createRoom(config);
    }

    public LiminalRoom createRoom(ConfigurationSection config) {
        config = plugin.getRoomConfig(config);
        final String roomType = config.getString("type", "");
        switch (roomType) {
            case "pools":
                return new PoolsRoom(this, config);
            case "ocean":
                return new OceanRoom(this, config);
            default:
                plugin.getLogger().severe("Unknown room type: " + roomType);
                return null;
        }
    }

    public LiminalPopulator createPopulator(LiminalRoom room, String id) {
        ConfigurationSection config = plugin.getPopulatorConfig(id);
        if (config == null) {
            plugin.getLogger().severe("Invalid populator id: " + id);
            return null;
        }
        return createPopulator(room, config);
    }

    public LiminalPopulator createPopulator(LiminalRoom room, ConfigurationSection config) {
        config = plugin.getPopulatorConfig(config);
        final String roomType = config.getString("type", "");
        switch (roomType) {
            case "waterfall":
                return new WaterfallPopulator(room, config);
            case "deep_pool":
                return new DeepPoolPopulator(room, config);
            case "loot":
                return new LootPopulator(room, config);
            default:
                plugin.getLogger().severe("Unknown populator type: " + roomType);
                return null;
        }
    }

    private void scheduleAmbientSounds() {
        if (minAmbientSoundTime == 0 || maxAmbientSoundTime == 0) {
            return;
        }

        final int soundTime = RandomUtils.range(random, minAmbientSoundTime, maxAmbientSoundTime);
        plugin.getServer().getScheduler().runTaskLater(
            plugin,
            () -> {
                final CustomSound sound = ambientSounds[random.nextInt(ambientSounds.length)];
                sound.play(getWorld());
                scheduleAmbientSounds();
            },
            soundTime);
    }
}
