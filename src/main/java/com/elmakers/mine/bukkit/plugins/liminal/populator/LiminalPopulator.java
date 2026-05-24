package com.elmakers.mine.bukkit.plugins.liminal.populator;

import org.bukkit.Chunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;
import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;
import com.elmakers.mine.bukkit.plugins.liminal.rooms.LiminalRoom;

public class LiminalPopulator extends BlockPopulator {
    protected final LiminalRoom room;

    public LiminalPopulator(LiminalRoom room) {
        this.room = room;
    }

    public void checkNewChunk(Chunk chunk) {
    }

    public String getNextLevel() {
        return null;
    }

    public LiminalWorld getWorld() {
        return room.getWorld();
    }

    public Plugin getPlugin() {
        return getWorld().getPlugin();
    }

    public LiminalController getController() {
        return getWorld().getController();
    }
}
