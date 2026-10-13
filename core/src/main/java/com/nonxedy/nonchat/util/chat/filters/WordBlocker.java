package com.nonxedy.nonchat.util.chat.filters;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.entity.Player;

import com.nonxedy.nonchat.api.MessageFilter;
import com.nonxedy.nonchat.config.PluginConfig;
import com.nonxedy.nonchat.config.PluginMessages;
import com.nonxedy.nonchat.util.chat.MentionCompletionUtil;
import com.nonxedy.nonchat.util.core.colors.ColorUtil;


/**
 * Handles message filtering by checking for banned words and regex patterns.
 * When a violation is detected, executes the actions configured under
 * 'banned-words.actions' (special actions 'block' and 'notify-staff', plus
 * arbitrary console commands) and sends the configured warning message to the player.
 */
public class WordBlocker implements MessageFilter {

    @Override
    public boolean shouldFilter(Player player, String message) {
        return checkAndHandle(player, message);
    }

    private final PluginConfig config;
    private final PluginMessages messages;
    private final Supplier<List<String>> bannedWords;
    private final Supplier<List<String>> bannedPatterns;
    private final BooleanSupplier wholeWords;
    private final Supplier<List<String>> exceptions;

    /**
     * Creates a fully-featured word blocker that executes configured actions on detection.
     * @param config Plugin configuration
     * @param messages Plugin messages
     */
    public WordBlocker(PluginConfig config, PluginMessages messages) {
        this.config = config;
        this.messages = messages;
        this.bannedWords = config::getBannedWords;
        this.bannedPatterns = config::getBannedPatterns;
        this.wholeWords = config::isBannedWordsWholeWords;
        this.exceptions = config::getBannedWordsExceptions;
    }

    /**
     * Creates a matcher-only word blocker without action handling (used in unit tests).
     * @param bannedWords List of banned words
     * @param bannedPatterns List of banned regex patterns
     */
    public WordBlocker(List<String> bannedWords, List<String> bannedPatterns) {
        this(bannedWords, bannedPatterns, false, List.of());
    }

    /**
     * Creates a matcher-only word blocker without action handling (used in unit tests).
     * @param bannedWords List of banned words
     * @param bannedPatterns List of banned regex patterns
     * @param wholeWords Whether banned words match only as whole words
     * @param exceptions Words allowed even though they contain a banned word
     */
    public WordBlocker(List<String> bannedWords, List<String> bannedPatterns,
                       boolean wholeWords, List<String> exceptions) {
        this.config = null;
        this.messages = null;
        this.bannedWords = () -> bannedWords;
        this.bannedPatterns = () -> bannedPatterns;
        this.wholeWords = () -> wholeWords;
        this.exceptions = () -> exceptions;
    }

    /**
     * Checks a message and, when it contains banned content, executes the configured actions.
     * Requires the blocker to be created with the PluginConfig/PluginMessages constructor.
     * @param player Player who sent the message
     * @param message Original message as typed by the player
     * @return true if the message is blocked, false if it is allowed
     */
    public boolean checkAndHandle(Player player, String message) {
        if (config == null || messages == null) {
            throw new IllegalStateException("checkAndHandle requires the PluginConfig/PluginMessages constructor");
        }
        if (!config.isWordBlockingEnabled()) {
            return false;
        }
        if (player == null || player.hasPermission("nonchat.antiblockedwords")) {
            return false;
        }

        // Check blocked words on the message without color codes
        if (isMessageAllowed(ColorUtil.stripFormatting(message))) {
            return false;
        }

        return FilterActionHandler.handle(config, player, message,
                FilterSettings.read(config.getConfig(), "banned-words", config.getBannedWordsActions(),
                        config.getBannedWordsMessage(), messages.getString("banned-words-detected"),
                        config.isBannedWordsConsoleNotifyEnabled(), "nonchat.wordblocker.notify"));
    }

    /**
     * Checks if a message is allowed by scanning for banned words and patterns
     * @param message The message to check
     * @return true if message is allowed, false if it contains banned content
     */
    public boolean isMessageAllowed(String message) {
        return isMessageAllowed(message, MentionCompletionUtil::isOnlinePlayerName);
    }

    /**
     * Checks if a message is allowed by scanning for banned words and patterns,
     * ignoring mentions of players matched by {@code onlinePlayerPredicate}.
     *
     * @param message The message to check
     * @param onlinePlayerPredicate Predicate identifying online player names
     * @return true if message is allowed, false if it contains banned content
     */
    public boolean isMessageAllowed(String message, Predicate<String> onlinePlayerPredicate) {
        if (message == null || message.isEmpty()) {
            return true;
        }

        String sanitized = MentionCompletionUtil.stripMentions(
                ColorUtil.stripFormatting(message),
                onlinePlayerPredicate);
        if (sanitized.isEmpty()) {
            return true;
        }

        String lowerMessage = removeExceptions(sanitized.toLowerCase());
        boolean matchWholeWords = wholeWords.getAsBoolean();

        // Check banned words (case-insensitive)
        for (String word : bannedWords.get()) {
            if (word.isEmpty()) {
                continue;
            }
            String lowerWord = word.toLowerCase();
            if (matchWholeWords ? containsWholeWord(lowerMessage, lowerWord) : lowerMessage.contains(lowerWord)) {
                return false;
            }
        }

        // Check regex patterns (case-insensitive)
        for (String pattern : bannedPatterns.get()) {
            try {
                Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                if (regex.matcher(sanitized).find()) {
                    return false;
                }
            } catch (PatternSyntaxException e) {
                // Log invalid regex pattern but don't crash
                System.err.println("Invalid regex pattern in banned patterns: " + pattern);
            }
        }

        return true;
    }

    /**
     * Removes exception words so banned words inside them are not matched.
     * Text around an exception is still checked, e.g. "classass" with exception "class" is blocked.
     * @param lowerMessage Lowercased message
     * @return Message with exception words replaced by spaces
     */
    private String removeExceptions(String lowerMessage) {
        String result = lowerMessage;
        for (String exception : exceptions.get()) {
            if (!exception.isEmpty()) {
                result = result.replace(exception.toLowerCase(), " ");
            }
        }
        return result;
    }

    private static boolean containsWholeWord(String lowerMessage, String lowerWord) {
        return wholeWordPattern(lowerWord).matcher(lowerMessage).find();
    }

    // \b only treats ASCII as word characters, so Cyrillic words need explicit Unicode boundaries
    private static Pattern wholeWordPattern(String word) {
        return Pattern.compile("(?<![\\p{L}\\p{N}_])" + Pattern.quote(word) + "(?![\\p{L}\\p{N}_])");
    }
}
