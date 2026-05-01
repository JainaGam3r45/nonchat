package com.nonxedy.nonchat.api;

import java.util.Collection;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface IPlatformAdapter {
    String getSupportedVersion();

    void registerChatListener(Plugin plugin, IMessageHandler handler);

    default boolean supportsChatBubbles() {
        return false;
    }

    default List<?> spawnMultilineBubble(
        Player player,
        String message,
        Location location,
        double scale,
        double scaleX,
        double scaleY,
        double scaleZ,
        String backgroundColor
    ) {
        return List.of();
    }

    default void updateBubblesLocation(Collection<?> bubbles, Location location) {
    }

    default void removeBubbles(Collection<?> bubbles) {
    }

    default boolean supports(String bukkitVersion) {
        if (bukkitVersion == null) return false;

        // "1.21.11-R0.1-SNAPSHOT" → "1.21.11"
        String serverVer = bukkitVersion.split("-")[0];
        String supported  = getSupportedVersion();

        return serverVer.equals(supported)
            || serverVer.startsWith(supported + ".");
    }

    default void cleanup() {
    }
}
