package com.nonxedy.nonchat.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.nonxedy.nonchat.Nonchat;
import com.nonxedy.nonchat.util.core.colors.ColorUtil;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * Provides PlaceholderAPI integration for nonchat plugin
 * Handles custom placeholders for chat features
 */
public class NonchatExpansion extends PlaceholderExpansion {

    private final Nonchat plugin;

    // Constructor initializes plugin reference
    public NonchatExpansion(Nonchat plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets expansion author name
     * @return Author identifier
     */
    @Override
    public @NotNull String getAuthor() {
        return "nonxedy";
    }

    /**
     * Gets expansion identifier for PlaceholderAPI
     * @return Expansion identifier string
     */
    @Override
    public @NotNull String getIdentifier() {
        return "nonchat";
    }

    /**
     * Gets expansion version from plugin
     * @return Current version string
     */
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Processes placeholder requests and returns values
     * @param player Player requesting the placeholder
     * @param identifier Placeholder identifier string
     * @return Processed placeholder value or null if invalid
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
    
        switch (identifier) {
            case "spy_enabled":
                return plugin.getSpyCommand().isSpying(player) ? 
                ColorUtil.parseColor(plugin.getConfigService().getMessages().getString("placeholders.spy.enabled")) :
                ColorUtil.parseColor(plugin.getConfigService().getMessages().getString("placeholders.spy.disabled"));
                
            case "bubble_enabled":
                return plugin.getConfigService().getConfig().isChatBubblesEnabled() ?
                ColorUtil.parseColor(plugin.getConfigService().getMessages().getString("placeholders.bubble.enabled")) :
                ColorUtil.parseColor(plugin.getConfigService().getMessages().getString("placeholders.bubble.disabled"));
                
            case "caps_filter_enabled":
                return plugin.getConfigService().getConfig().isCapsFilterEnabled() ?
                ColorUtil.parseColor(plugin.getConfigService().getMessages().getString("placeholders.caps-filter.enabled")) :
                ColorUtil.parseColor(plugin.getConfigService().getMessages().getString("placeholders.caps-filter.disabled"));
                
            default:
                return null;
        }
    }
}