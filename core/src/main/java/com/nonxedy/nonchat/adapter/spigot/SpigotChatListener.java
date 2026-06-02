package com.nonxedy.nonchat.adapter.spigot;

import com.nonxedy.nonchat.api.IMessageHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class SpigotChatListener implements Listener {
    private final IMessageHandler handler;

    public SpigotChatListener(IMessageHandler handler) {
        this.handler = handler;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        handler.handleChat(event.getPlayer(), event.getMessage());
    }
}
