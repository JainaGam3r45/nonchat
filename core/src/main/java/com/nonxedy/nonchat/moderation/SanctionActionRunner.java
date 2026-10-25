package com.nonxedy.nonchat.moderation;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.nonxedy.nonchat.util.core.colors.ColorUtil;
import com.nonxedy.nonchat.util.core.messages.MessageUtil;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;

/**
 * Executes the side effects of a sanction: staff notifications, console commands and player warnings.
 * Single place for what each filter used to implement on its own.
 */
class SanctionActionRunner {

    private final Consumer<String> debug;

    SanctionActionRunner(Consumer<String> debug) {
        this.debug = debug;
    }

    /**
     * Runs the actions of a tier
     * @param player Player who committed the violation
     * @param violation The detected violation
     * @param rule Rule of the violation
     * @param actions 'notify-staff' or console commands; 'block' and 'warn' are implied and skipped
     */
    void run(Player player, Violation violation, ViolationRule rule, List<String> actions) {
        for (String action : actions) {
            if (action.equalsIgnoreCase("block") || action.equalsIgnoreCase("warn")) {
                // Blocking and the warning message are implied by the SanctionsManager
                continue;
            }
            if (action.equalsIgnoreCase("notify-staff")) {
                debug.accept("Action notify-staff for " + player.getName());
                notifyStaff(player, violation, rule);
            } else {
                executeCommand(player, action);
            }
        }
    }

    /**
     * Sends the rule's warning message to the player, if configured
     */
    void warn(Player player, Violation violation, ViolationRule rule) {
        String warnMessage = rule.warnMessage();
        if (warnMessage == null || warnMessage.isEmpty()) {
            debug.accept("No warning message for " + violation.type());
            return;
        }
        debug.accept("Warning sent to " + player.getName());

        // Resolve placeholders on the template first, then insert the flagged message,
        // so user input never passes through placeholder parsing
        String resolved = resolvePlaceholders(player, warnMessage)
                .replace("%message%", orEmpty(violation.message()));
        MessageUtil.send(player, ColorUtil.parseComponentCached(resolved));
    }

    private void notifyStaff(Player player, Violation violation, ViolationRule rule) {
        String template = rule.staffNotifyMessage();
        if (template == null || template.isEmpty()) {
            return;
        }

        // Same rule as warn(): the flagged message is inserted last
        String notification = resolvePlaceholders(player, template.replace("{player}", player.getName()))
                .replace("{message}", orEmpty(violation.message()));
        Component component = ColorUtil.parseComponentCached(notification);

        String permission = rule.staffNotifyPermission();
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> (permission != null && p.hasPermission(permission)) || p.isOp())
                .forEach(p -> MessageUtil.send(p, component));

        if (rule.consoleNotify()) {
            MessageUtil.send(Bukkit.getConsoleSender(), component);
        }
    }

    private void executeCommand(Player player, String command) {
        if (command == null || command.isEmpty()) {
            return;
        }

        String resolvedCommand = resolvePlaceholders(player, command);
        debug.accept("Action command: " + resolvedCommand);
        try {
            // Run command sync on main thread
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("nonchat"), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolvedCommand);
            });
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.WARNING, "&#FFAFFB[nonchat] &cFailed to execute sanction command: {0}", e.getMessage());
        }
    }

    private String resolvePlaceholders(Player player, String text) {
        if (player == null || text == null) return text;

        // Check if PlaceholderAPI is loaded
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                return PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "&#FFAFFB[nonchat] &cError processing placeholders: {0}", e.getMessage());
            }
        }

        // Fall back to standard placeholders if PAPI not available
        return text.replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString());
    }

    private String orEmpty(String message) {
        return message != null ? message : "[empty]";
    }
}
