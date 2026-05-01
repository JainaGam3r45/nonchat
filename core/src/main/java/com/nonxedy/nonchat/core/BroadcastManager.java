package com.nonxedy.nonchat.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nonxedy.nonchat.util.integration.external.IntegrationUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.nonxedy.nonchat.Nonchat;
import com.nonxedy.nonchat.config.PluginConfig;
import com.nonxedy.nonchat.util.chat.filters.LinkDetector;
import com.nonxedy.nonchat.util.core.broadcast.BroadcastMessage;
import com.nonxedy.nonchat.util.core.colors.ColorUtil;

import net.kyori.adventure.text.Component;

public class BroadcastManager {
    private final Nonchat plugin;
    private final PluginConfig config;
    private final List<BukkitTask> activeTasks;
    private List<BroadcastMessage> messageSequence;

    public BroadcastManager(Nonchat plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.activeTasks = new ArrayList<>();
        this.messageSequence = new ArrayList<>();
        start();
    }

    public void start() {
        stop();
        Map<String, BroadcastMessage> configuredMessages = config.getBroadcastMessages();

        List<BroadcastMessage> enabledMessages = configuredMessages.values().stream()
            .filter(BroadcastMessage::isEnabled)
            .collect(Collectors.toList());

        if (enabledMessages.isEmpty()) return;

        messageSequence = new ArrayList<>(enabledMessages);

        if (config.isRandomBroadcastEnabled()) {
            Collections.shuffle(messageSequence);
        } else {
            // Keep the order from the config
        }

        long delay = 0;
        long totalPeriod = messageSequence.stream().mapToLong(BroadcastMessage::getInterval).sum() * 20L;

        for (BroadcastMessage message : messageSequence) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                broadcast(message);  // pass BroadcastMessage object directly
            }, delay, totalPeriod);
            activeTasks.add(task);
            delay += message.getInterval() * 20L;
        }
    }

    public void broadcast(BroadcastMessage broadcastMessage) {
        String message = broadcastMessage.getMessage();
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Process PAPI placeholders for each player individually
                String parsedMessage = IntegrationUtil.processPlaceholders(player, message);

                Component formatted;
                // Check if message contains MiniMessage tags
                if (ColorUtil.containsMiniMessageTags(parsedMessage)) {
                    formatted = ColorUtil.parseComponent(parsedMessage);
                } else {
                    // Use LinkDetector to make links clickable for legacy messages
                    formatted = LinkDetector.makeLinksClickable(parsedMessage);
                }
                // Try to use Adventure API first
                player.sendMessage(formatted);
            }

            // Console log using the raw message (no player context for PAPI)
            if (broadcastMessage.isDisplayInConsole()) {
                String consoleMessage = ColorUtil.stripAllColors(message);
                plugin.getLogger().info(consoleMessage);
            }

        } catch (NoSuchMethodError e) {
            // Fall back to traditional Bukkit sendMessage if Adventure API is not available
            plugin.logError("Adventure API isn't available: " + e.getMessage());
            for (Player player : Bukkit.getOnlinePlayers()) {
                String parsedMessage = IntegrationUtil.processPlaceholders(player, message);
                player.sendMessage(ColorUtil.parseColor(parsedMessage));
            }
        }
    }

    public void stop() {
        activeTasks.forEach(BukkitTask::cancel);
        activeTasks.clear();
        messageSequence.clear();
    }

    public void reload() {
        stop();
        start();
    }
}
