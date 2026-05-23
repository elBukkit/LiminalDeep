package com.elmakers.mine.bukkit.plugins.liminal;

import java.util.Collection;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CustomSound {
    private final String customId;
    private final Sound sound;

    private CustomSound(String customId, Sound sound) {
        this.customId = customId;
        this.sound = sound;
    }

    public static CustomSound fromString(String customId) {
        try {
            Sound sound = Sound.valueOf(customId);
            return new CustomSound(null, sound);
        } catch (Exception ex) {
            return new CustomSound(customId, null);
        }
    }

    public static CustomSound of(Sound sound) {
        return new CustomSound(null, sound);
    }

    public void play(Player player) {
        if (sound != null) {
            player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT,1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), customId, SoundCategory.AMBIENT,1.0f, 1.0f);
        }
    }

    public void play(World world) {
        Collection<? extends Player> players = world.getPlayers();
        for (Player player : players) {
            play(player);
        }
    }
}
