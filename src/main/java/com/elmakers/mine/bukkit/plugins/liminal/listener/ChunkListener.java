package com.elmakers.mine.bukkit.plugins.liminal.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalController;

public class ChunkListener implements Listener {
    private final LiminalController controller;

    public ChunkListener(LiminalController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            controller.checkNewChunk(event.getChunk());
        }
    }
}
