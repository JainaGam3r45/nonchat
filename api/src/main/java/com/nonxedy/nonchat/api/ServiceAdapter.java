package com.nonxedy.nonchat.api;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class ServiceAdapter implements IPlatformAdapter {
    private final String supportedVersion;

    protected ServiceAdapter(String supportedVersion) {
        this.supportedVersion = supportedVersion;
    }

    @Override
    public String getSupportedVersion() {
        return supportedVersion;
    }

    @Override
    public void registerChatListener(Plugin plugin, IMessageHandler handler) {
        plugin.getServer().getPluginManager().registerEvents(createChatListener(handler), plugin);
    }

    protected abstract Listener createChatListener(IMessageHandler handler);
}
