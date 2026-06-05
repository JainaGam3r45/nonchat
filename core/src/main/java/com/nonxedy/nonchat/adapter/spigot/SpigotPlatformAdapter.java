package com.nonxedy.nonchat.adapter.spigot;

import com.nonxedy.nonchat.api.IMessageHandler;
import com.nonxedy.nonchat.api.ServiceAdapter;
import org.bukkit.event.Listener;

public final class SpigotPlatformAdapter extends ServiceAdapter {
    public SpigotPlatformAdapter() {
        super("1.16");
    }

    @Override
    public boolean supports(String bukkitVersion) {
        return isAtLeastSupportedVersion(bukkitVersion) && !isPaperAsyncChatAvailable();
    }

    @Override
    public boolean supportsChatBubbles() {
        return false;
    }

    @Override
    protected Listener createChatListener(IMessageHandler handler) {
        return new SpigotChatListener(handler);
    }

    private boolean isPaperAsyncChatAvailable() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private boolean isAtLeastSupportedVersion(String bukkitVersion) {
        if (bukkitVersion == null || bukkitVersion.isBlank()) {
            return false;
        }

        String[] parts = bukkitVersion.split("-", 2)[0].split("\\.");
        if (parts.length < 2) {
            return false;
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major > 1 || (major == 1 && minor >= 16);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
