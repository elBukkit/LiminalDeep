package com.elmakers.mine.bukkit.plugins.liminal.generator;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.populator.LiminalRoomPopulator;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.RoomTable;

public class LiminalGenerator extends ChunkGenerator {
    private final LiminalWorld world;
    private final BiomeProvider biomeProvider;
    private final RoomTable rooms;
    private final LiminalRoomPopulator populator;

    public LiminalGenerator(LiminalWorld world, ConfigurationSection config) {
        this.world = world;
        biomeProvider = createDefaultBiomeProvider(config);
        rooms = new RoomTable(world, config);
        populator = new LiminalRoomPopulator(world);
    }

    protected BiomeProvider createDefaultBiomeProvider(ConfigurationSection config) {
        String biomeKey = config.getString("biome");
        try {
            if (biomeKey != null) {
                return new SingleBiomeProvider(Biome.valueOf(biomeKey.toUpperCase(Locale.ROOT)));
            }
        } catch (Exception ex) {
            world.getLogger().warning("Invalid biome specified in " + world.getName() + " config: " + biomeKey);
        }
        return null;
    }

    @Nullable
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return biomeProvider;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return List.of(populator);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        world.getRoomAt(chunkX, chunkZ).generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
    }

    public LiminalWorld getWorld() {
        return world;
    }

    public Plugin getPlugin() {
        return world.getPlugin();
    }

    public RoomTable getRooms() {
        return rooms;
    }
}
