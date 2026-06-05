package com.nonxedy.nonchat.util.core.messages;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
        LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageUtil() {}

    public static void send(@NotNull CommandSender sender, @NotNull Component message) {
        sender.sendMessage(toLegacy(message));
    }

    public static void send(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(message);
    }

    public static void broadcast(@NotNull Component message) {
        Bukkit.broadcastMessage(toLegacy(message));
    }

    public static void joinMessage(@NotNull PlayerJoinEvent event, @NotNull Component message) {
        event.setJoinMessage(toLegacy(message));
    }

    public static void quitMessage(@NotNull PlayerQuitEvent event, @NotNull Component message) {
        event.setQuitMessage(toLegacy(message));
    }

    public static void deathMessage(@NotNull PlayerDeathEvent event, Component message) {
        event.setDeathMessage(message == null ? null : toLegacy(message));
    }

    public static @NotNull String toLegacy(@NotNull Component message) {
        return LEGACY_SERIALIZER.serialize(message);
    }
}