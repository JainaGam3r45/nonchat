package com.nonxedy.nonchat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.nonxedy.nonchat.api.InteractivePlaceholder;
import com.nonxedy.nonchat.util.chat.filters.LinkDetector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

/**
 * Manager for handling interactive placeholders
 * Processes messages and replaces placeholder patterns with interactive components
 */
public class InteractivePlaceholderManager {

    private final Map<String, InteractivePlaceholder> placeholders = new HashMap<>();
    private final Pattern placeholderPattern;

    public InteractivePlaceholderManager() {
        // Pattern to match placeholders like [name], [name:arg1:arg2], etc.
        this.placeholderPattern = Pattern.compile("\\[([\\w]+)(?::([^\\]]*))?\\]", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Registers an interactive placeholder
     *
     * @param placeholder The placeholder to register
     */
    public void registerPlaceholder(@NotNull InteractivePlaceholder placeholder) {
        placeholders.put(placeholder.getPlaceholder().toLowerCase(), placeholder);
    }

    /**
     * Unregisters an interactive placeholder
     *
     * @param placeholderName The name of the placeholder to unregister
     */
    public void unregisterPlaceholder(@NotNull String placeholderName) {
        placeholders.remove(placeholderName.toLowerCase());
    }

    /**
     * Gets a registered placeholder by name
     *
     * @param placeholderName The name of the placeholder
     * @return The placeholder instance or null if not found
     */
    @Nullable
    public InteractivePlaceholder getPlaceholder(@NotNull String placeholderName) {
        return placeholders.get(placeholderName.toLowerCase());
    }

    /**
     * Checks if a placeholder is registered
     *
     * @param placeholderName The name of the placeholder
     * @return true if registered, false otherwise
     */
    public boolean isPlaceholderRegistered(@NotNull String placeholderName) {
        return placeholders.containsKey(placeholderName.toLowerCase());
    }

    @NotNull
    public Component processMessage(@NotNull Player player, @NotNull String message) {
        if (message.isEmpty()) {
            return Component.text(message);
        }

        List<Component> parts = new ArrayList<>();
        Matcher matcher = placeholderPattern.matcher(message);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textBefore = message.substring(lastEnd, matcher.start());
                parts.add(LinkDetector.makeLinksClickable(textBefore));
            }

            String placeholderName = matcher.group(1).toLowerCase();
            String arguments = matcher.group(2);
            InteractivePlaceholder placeholder = getPlaceholder(placeholderName);

            if (placeholder != null && placeholder.isEnabled()) {
                String permission = placeholder.getPermission();
                if (permission == null || permission.isEmpty()
                        || player.hasPermission(permission)) {
                    String[] args = arguments != null
                            ? arguments.split(":")
                            : new String[0];
                    parts.add(placeholder.process(player, args));
                } else {
                    parts.add(Component.text(matcher.group()));
                }
            } else {
                parts.add(Component.text(matcher.group()));
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            parts.add(LinkDetector.makeLinksClickable(
                message.substring(lastEnd)
            ));
        }

        if (parts.isEmpty()) {
            return Component.text(message);
        }

        return Component.join(
            JoinConfiguration.noSeparators(),
            parts
        );
    }

    /**
     * Gets all registered placeholders
     *
     * @return Map of placeholder names to placeholder instances
     */
    @NotNull
    public Map<String, InteractivePlaceholder> getAllPlaceholders() {
        return new HashMap<>(placeholders);
    }

    /**
     * Clears all registered placeholders
     */
    public void clearPlaceholders() {
        placeholders.clear();
    }
}
