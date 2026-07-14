package com.nonxedy.nonchat.util.core.messages;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Sends Adventure components natively.
 * Hover/click events are preserved when sending Adventure Components directly.
 */
public final class MessageUtil {

    private static Method joinMessageMethod;
    private static Method quitMessageMethod;
    private static boolean reflectionInitialized = false;

    private MessageUtil() {
    }

    private static void initReflection() {
        if (reflectionInitialized) {
            return;
        }
        try {
            joinMessageMethod = PlayerJoinEvent.class.getMethod("joinMessage", Component.class);
        } catch (NoSuchMethodException e) {
            // Paper joinMessage(Component) is not available, will fall back to setJoinMessage(String)
        }
        try {
            quitMessageMethod = PlayerQuitEvent.class.getMethod("quitMessage", Component.class);
        } catch (NoSuchMethodException e) {
            // Paper quitMessage(Component) is not available, will fall back to setQuitMessage(String)
        }
        reflectionInitialized = true;
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
        initReflection();
        if (joinMessageMethod != null) {
            try {
                joinMessageMethod.invoke(event, message);
                return;
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error calling joinMessage(Component) via reflection, falling back to legacy", e);
            }
        }
        
        // Fallback to legacy String join message (Spigot or older versions)
        try {
            String legacyMsg = LegacyComponentSerializer.legacySection().serialize(message);
            event.setJoinMessage(legacyMsg);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to set legacy join message", e);
        }
    }

    public static void quitMessage(@NotNull PlayerQuitEvent event, @NotNull Component message) {
        initReflection();
        if (quitMessageMethod != null) {
            try {
                quitMessageMethod.invoke(event, message);
                return;
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error calling quitMessage(Component) via reflection, falling back to legacy", e);
            }
        }
        
        // Fallback to legacy String quit message (Spigot or older versions)
        try {
            String legacyMsg = LegacyComponentSerializer.legacySection().serialize(message);
            event.setQuitMessage(legacyMsg);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to set legacy quit message", e);
        }
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
