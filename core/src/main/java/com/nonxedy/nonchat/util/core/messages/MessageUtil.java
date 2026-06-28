package com.nonxedy.nonchat.util.core.messages;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

/**
 * Sends Adventure components natively.
 * Hover/click events are preserved when sending Adventure Components directly.
 */
public final class MessageUtil {

    private MessageUtil() {
    }

    public static void send(@NotNull CommandSender sender, @NotNull Component message) {
        // Native Adventure support on modern Paper/Spigot (1.19+)
        // Hover and click events are preserved
        sender.sendMessage(message);
    }

    public static void send(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(message);
    }

    public static void broadcast(@NotNull Component message) {
        Bukkit.broadcast(message);
    }

    public static void joinMessage(@NotNull PlayerJoinEvent event, @NotNull Component message) {
        // For join/quit we still need legacy string because Bukkit event API
        // Join/Quit events don't support Adventure components directly in all versions
        if (event.getPlayer() instanceof Player player) {
            player.sendMessage(message);
        }
        // Fallback for very old API compatibility (rarely used now)
        event.setJoinMessage(null); // Prevent default, we sent via player.sendMessage
    }

    public static void quitMessage(@NotNull PlayerQuitEvent event, @NotNull Component message) {
        if (event.getPlayer() instanceof Player player) {
            player.sendMessage(message);
        }
        event.setQuitMessage(null);
    }

    public static void deathMessage(@NotNull PlayerDeathEvent event, Component message) {
        // Death messages can be set as Component in modern API
        if (message != null) {
            event.deathMessage(message);
        } else {
            event.deathMessage(null);
        }
    }
}
