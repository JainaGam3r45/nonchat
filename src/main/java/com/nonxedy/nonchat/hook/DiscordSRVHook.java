package com.nonxedy.nonchat.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nonxedy.nonchat.nonchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;

/**
 * Provides integration with the DiscordSRV plugin
 */
public class DiscordSRVHook {

    private final nonchat plugin;
    private DiscordSRV discordSRV;
    private boolean isHooked = false;

    public DiscordSRVHook(nonchat plugin) {
        this.plugin = plugin;
        try {
            Plugin discordSRVPlugin = Bukkit.getPluginManager().getPlugin("DiscordSRV");
            
            if (discordSRVPlugin != null && discordSRVPlugin.isEnabled()) {
                this.discordSRV = DiscordSRV.getPlugin();
                // Register this as a listener for DiscordSRV events
                DiscordSRV.api.subscribe(this);
                this.isHooked = true;
                plugin.logResponse("Successfully hooked into DiscordSRV!");
            } else {
                plugin.logError("DiscordSRV plugin not found or is disabled. DiscordSRV integration will not be available.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to hook into DiscordSRV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send a message to a specific Discord channel through DiscordSRV
     * 
     * @param channelName The name of the channel as configured in DiscordSRV
     * @param message The message to send
     * @return true if message was sent, false otherwise
     */
    public boolean sendMessageToChannel(String channelName, String message) {
        if (!isHooked || discordSRV == null) {
            return false;
        }

        try {
            plugin.logResponse("DiscordSRVHook: Sending message to channel '" + channelName + "': " + message);
            
            // Get the TextChannel object from DiscordSRV using the channel name
            TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName(channelName);
            
            if (channel != null) {
                plugin.logResponse("Found Discord channel: " + channel.getName() + " (" + channel.getId() + ")");
                DiscordUtil.sendMessage(channel, message);
                return true;
            } else {
                plugin.logError("DiscordSRV channel not found: " + channelName);
                return false;
            }
        } catch (Exception e) {
            plugin.logError("Error sending message to Discord: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Handle Discord ready event
     */
    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        plugin.logResponse("Connected to Discord via DiscordSRV!");
    }

    /**
     * Handle messages received from Discord
     */
    @Subscribe
    public void onDiscordMessageReceived(DiscordGuildMessageReceivedEvent event) {
        // Ignore messages from bots
        if (event.getAuthor().isBot()) {
            return;
        }
        
        // Get channel mapping
        String minecraftChannel = discordSRV.getDestinationGameChannelNameForTextChannel(event.getChannel());
        
        if (minecraftChannel != null) {
            // Process the message if needed
            // This could relay messages to specific in-game channels
            // or handle commands sent from Discord
            
            // Example: relay formatted message to players in the corresponding channel
            String formattedMessage = "§9[Discord] §b" + event.getAuthor().getName() + "§f: " + event.getMessage().getContentDisplay();
            
            // This is a simple broadcast to all players
            // In a real implementation, you might want to send this only to players in the specific channel
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Check if the hook to DiscordSRV is active
     * 
     * @return true if connected to DiscordSRV, false otherwise
     */
    public boolean isHooked() {
        return isHooked;
    }

    /**
     * Shutdown the hook - unsubscribe from DiscordSRV events
     */
    public void shutdown() {
        if (isHooked && discordSRV != null) {
            try {
                DiscordSRV.api.unsubscribe(this);
                plugin.logResponse("Successfully unhooked from DiscordSRV");
            } catch (Exception e) {
                plugin.logError("Error while unhooking from DiscordSRV: " + e.getMessage());
            }
        }
    }
}
