package com.nonxedy.nonchat.adapter.v1_16_R3;

import com.nonxedy.nonchat.api.IMessageHandler;
import com.nonxedy.nonchat.api.ServiceAdapter;
import org.bukkit.event.Listener;

public final class PlatformAdapter116 extends ServiceAdapter {
    public PlatformAdapter116() {
        super("1.16");
    }

    @Override
    protected Listener createChatListener(IMessageHandler handler) {
        return new ChatListener116(handler);
    }
}
