package com.nonxedy.nonchat.integration;

import org.bukkit.entity.Player;

import com.nonxedy.nonchat.Nonchat;
import com.nonxedy.nonchat.api.Channel;
import com.nonxedy.nonchat.chat.channel.ChannelManager;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;

public class DiscordSRVIntegration {
    private final Nonchat plugin;

    public DiscordSRVIntegration(Nonchat plugin) {
        this.plugin = plugin;
        DiscordSRV.api.subscribe(this);
    }

    public void unregister() {
        DiscordSRV.api.unsubscribe(this);
    }

    @Subscribe
    @SuppressWarnings("deprecation") // getMessage()/setMessage() are deprecated but replacements use incompatible shaded Adventure API
    public void onGameChatMessagePreProcess(GameChatMessagePreProcessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (player == null || plugin.getChatManager() == null) {
            return;
        }

        ChannelManager channelManager = plugin.getChatManager().getChannelManager();
        ChannelManager.ResolvedChannelMessage resolvedMessage = channelManager.resolveChannelMessage(message, player);

        if (resolvedMessage == null) {
            event.setCancelled(true);
            return;
        }

        Channel channel = resolvedMessage.channel();
        String cleanMessage = resolvedMessage.message();

        if (!channel.isEnabled() || !channel.canSend(player) || cleanMessage.trim().isEmpty()) {
            event.setCancelled(true);
            return;
        }

        if (resolvedMessage.updatePlayerChannel()) {
            channelManager.setPlayerChannel(player, channel.getId());
        }

        event.setChannel(channel.getId());
        event.setMessage(cleanMessage);
    }
    
    @Subscribe
    public void onDiscordGuildMessagePreProcess(DiscordGuildMessagePreProcessEvent event) {
        // Handle messages from Discord to Minecraft if needed
        // This is where you can filter which Discord messages go to which Minecraft channels
    }
}
