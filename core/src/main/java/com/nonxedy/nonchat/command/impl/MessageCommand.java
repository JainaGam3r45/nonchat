package com.nonxedy.nonchat.command.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.nonxedy.nonchat.Nonchat;
import com.nonxedy.nonchat.config.PluginConfig;
import com.nonxedy.nonchat.config.PluginMessages;
import com.nonxedy.nonchat.core.MessageManager;
import com.nonxedy.nonchat.util.chat.MentionCompletionUtil;
import com.nonxedy.nonchat.util.core.colors.ColorUtil;
import com.nonxedy.nonchat.util.core.messages.MessageUtil;

/**
 * Handles the /msg command. Delivery, filtering, ignore and world-scope checks
 * are done by {@link MessageManager}.
 */
public class MessageCommand implements CommandExecutor, TabCompleter {
    private final Nonchat plugin;
    private final PluginConfig config;
    private final PluginMessages messages;
    private final MessageManager messageManager;

    public MessageCommand(Nonchat plugin, PluginConfig config, PluginMessages messages, MessageManager messageManager) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.messageManager = messageManager;
    }

    /**
     * Handles private message command execution
     * @param sender Command sender
     * @param command Command being executed
     * @param label Command label used
     * @param args Command arguments
     * @return true if command handled successfully
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        plugin.logCommand(command.getName(), args);

        if (!config.isPrivateChatEnabled()) {
            MessageUtil.send(sender, ColorUtil.parseComponentCached(messages.getString("command-disabled")));
            return true;
        }

        // Check if sender has permission to use private messaging
        if (!sender.hasPermission("nonchat.message")) {
            MessageUtil.send(sender, ColorUtil.parseComponentCached(messages.getString("no-permission")));
            plugin.logError("Player " + sender.getName() + " tried to use the message command without permission.");
            return true;
        }

        // Validate command arguments (need at least target player and message)
        if (args.length < 2) {
            MessageUtil.send(sender, ColorUtil.parseComponentCached(messages.getString("invalid-usage-message")));
            return true;
        }

        // Get target player and verify they are online
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtil.send(sender, ColorUtil.parseComponentCached(messages.getString("player-not-found")));
            return true;
        }

        // Combine all remaining arguments into the message
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        messageManager.sendPrivateMessage(sender, target, message, false);
        return true;
    }

    /**
     * Provides tab completion suggestions
     * @param sender Command sender
     * @param command Command being completed
     * @param label Command label used
     * @param args Current arguments
     * @return List of suggestions
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                        @NotNull String label, @NotNull String[] args) {
        if (!config.isPrivateChatEnabled() || !sender.hasPermission("nonchat.message")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !(sender instanceof Player senderPlayer) || senderPlayer.canSee(player))
                    .filter(player -> !(sender instanceof Player senderPlayer)
                            || config.canPrivateMessage(senderPlayer, player))
                    .map(Player::getName)
                    .filter(name -> !name.equals(sender.getName()))
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length >= 2 && config.isMentionsEnabled()) {
            List<String> mentionSuggestions = MentionCompletionUtil.getMentionSuggestions(
                    sender,
                    args[args.length - 1],
                    player -> !(sender instanceof Player senderPlayer)
                            || config.canPrivateMessage(senderPlayer, player));
            if (!mentionSuggestions.isEmpty()) {
                return mentionSuggestions;
            }

            if (args.length == 2) {
                List<String> suggestions = Arrays.asList(
                    "message"
                );
                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
