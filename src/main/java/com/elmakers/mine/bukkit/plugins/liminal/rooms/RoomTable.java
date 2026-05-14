package com.elmakers.mine.bukkit.plugins.liminal.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.liminal.LiminalWorld;

public class RoomTable {
    private final List<LiminalRoom> rooms = new ArrayList<>();
    private final LiminalWorld world;

    public RoomTable(LiminalWorld world, ConfigurationSection config) {
        this.world = world;
        final ConfigurationSection roomsConfig = config.getConfigurationSection("rooms");
        if (roomsConfig != null) {
            for (String roomKey : roomsConfig.getKeys(false)) {
                ConfigurationSection roomConfig = roomsConfig.getConfigurationSection(roomKey);
                LiminalRoom room = world.createRoom(roomConfig);
                if (room == null) continue;
                rooms.add(room);
            }
        } else {
            List<String> roomList = config.getStringList("rooms");
            if (roomList != null) {
                for (String roomId : roomList) {
                    LiminalRoom room = world.createRoom(roomId);
                    if (room == null) continue;
                    rooms.add(room);
                }
            }
        }
    }

    public LiminalRoom getRoomAt(int chunkX, int chunkZ, long worldSeed) {
        final long chunkSeed = worldSeed
                ^ (long) chunkX * 0x9E3779B97F4A7C15L
                ^ (long) chunkZ * 0xD1B54A32D192ED03L;

        double totalWeight = 0;
        for (LiminalRoom room : rooms) {
            totalWeight += room.getWeight(chunkX, chunkZ);
        }
        if (totalWeight == 0) {
            return rooms.get(0);
        }

        double weight = new SplittableRandom(chunkSeed).nextDouble(totalWeight);
        for (LiminalRoom room : rooms) {
            double roomWeight = room.getWeight(chunkX, chunkZ);
            if (roomWeight <= 0) {
                continue;
            }
            weight -= roomWeight;
            if (weight <= 0) {
                return room;
            }
        }
        // Should never happen
        return rooms.get(rooms.size() - 1);
    }
}
