package com.nonxedy.nonchat.api;

import org.bukkit.entity.Player;

public interface IMessageHandler {
    void handleChat(Player player, String message);
}
