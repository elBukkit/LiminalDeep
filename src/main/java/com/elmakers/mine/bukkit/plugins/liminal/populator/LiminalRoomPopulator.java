package com.elmakers.mine.bukkit.plugins.liminal.populator;

import java.util.Random;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;

public class LiminalRoomPopulator extends BlockPopulator {
    private final LiminalWorld world;

    public LiminalRoomPopulator(LiminalWorld world) {
        this.world = world;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        world.getRoomAt(chunkX, chunkZ).populate(worldInfo, random, chunkX, chunkZ, region);
    }
}
