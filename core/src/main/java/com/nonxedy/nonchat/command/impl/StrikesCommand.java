package com.nonxedy.nonchat.command.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.nonxedy.nonchat.Nonchat;
import com.nonxedy.nonchat.config.PluginMessages;
import com.nonxedy.nonchat.moderation.SanctionsManager;
import com.nonxedy.nonchat.moderation.Strike;
import com.nonxedy.nonchat.moderation.ViolationType;
import com.nonxedy.nonchat.util.core.colors.ColorUtil;
import com.nonxedy.nonchat.util.core.messages.MessageUtil;

/**
 * Shows and clears the moderation strikes of a player:
 * /strikes &lt;player&gt; and /strikes clear &lt;player&gt; [type]
 */
public class StrikesCommand implements CommandExecutor, TabCompleter {

    private static final String VIEW_PERMISSION = "nonchat.moderation.strikes";
    private static final String CLEAR_PERMISSION = "nonchat.moderation.strikes.clear";

    private final Nonchat plugin;
    private final PluginMessages messages;

    public StrikesCommand(Nonchat plugin, PluginMessages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        plugin.logCommand(command.getName(), args);

        if (args.length == 0) {
            send(sender, "strikes-usage");
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            return handleClear(sender, args);
        }
        return handleShow(sender, args[0]);
    }

    private boolean handleShow(CommandSender sender, String playerName) {
        if (!sender.hasPermission(VIEW_PERMISSION)) {
            send(sender, "no-permission");
            return true;
        }

        OfflinePlayer target = findPlayer(playerName);
        if (target == null) {
            send(sender, "player-not-found");
            return true;
        }

        List<Strike> strikes = plugin.getSanctionsManager().getStrikes(target.getUniqueId());
        if (strikes.isEmpty()) {
            send(sender, "strikes-none", "{player}", name(target, playerName));
            return true;
        }

        send(sender, "strikes-header", "{player}", name(target, playerName), "{count}", String.valueOf(strikes.size()));
        Instant now = Instant.now();
        for (Strike strike : strikes) {
            send(sender, "strikes-entry",
                    "{type}", strike.type().name().toLowerCase(Locale.ROOT),
                    "{ago}", format(Duration.between(strike.issuedAt(), now)),
                    "{remaining}", format(Duration.between(now, strike.expiresAt())));
        }
        return true;
    }

    private boolean handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission(CLEAR_PERMISSION)) {
            send(sender, "no-permission");
            return true;
        }
        if (args.length < 2) {
            send(sender, "strikes-usage");
            return true;
        }

        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            send(sender, "player-not-found");
            return true;
        }

        SanctionsManager manager = plugin.getSanctionsManager();
        UUID playerId = target.getUniqueId();
        int removed;
        if (args.length >= 3) {
            ViolationType type = parseType(args[2]);
            if (type == null) {
                send(sender, "strikes-invalid-type", "{types}", String.join(", ", typeNames()));
                return true;
            }
            removed = manager.clearStrikes(playerId, type);
        } else {
            removed = manager.getStrikes(playerId).size();
            manager.clearStrikes(playerId);
        }

        plugin.logModeration(sender.getName() + " cleared " + removed + " strike(s) of " + name(target, args[1]));
        send(sender, "strikes-cleared", "{count}", String.valueOf(removed), "{player}", name(target, args[1]));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission(CLEAR_PERMISSION)) {
                suggestions.add("clear");
            }
            if (sender.hasPermission(VIEW_PERMISSION)) {
                suggestions.addAll(onlineNames());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("clear") && sender.hasPermission(CLEAR_PERMISSION)) {
            suggestions.addAll(onlineNames());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("clear") && sender.hasPermission(CLEAR_PERMISSION)) {
            suggestions.addAll(typeNames());
        }

        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        return suggestions.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(typed)).toList();
    }

    private List<String> onlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    private List<String> typeNames() {
        return Arrays.stream(ViolationType.values()).map(t -> t.name().toLowerCase(Locale.ROOT)).toList();
    }

    private ViolationType parseType(String text) {
        try {
            return ViolationType.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Online players first, then players the server already knows; unknown names are not looked up online
    private OfflinePlayer findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        return online != null ? online : Bukkit.getOfflinePlayerIfCached(name);
    }

    private String name(OfflinePlayer player, String fallback) {
        return player.getName() != null ? player.getName() : fallback;
    }

    private String format(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long days = seconds / 86400;
        long hours = seconds % 86400 / 3600;
        long minutes = seconds % 3600 / 60;
        long secs = seconds % 60;
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + secs + "s";
        }
        return secs + "s";
    }

    private void send(CommandSender sender, String key, String... replacements) {
        String text = messages.getString(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        MessageUtil.send(sender, ColorUtil.parseComponentCached(text));
    }
}
