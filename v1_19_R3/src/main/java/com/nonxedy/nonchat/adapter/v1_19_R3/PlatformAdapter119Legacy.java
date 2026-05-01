package com.nonxedy.nonchat.adapter.v1_19_R3;

import com.nonxedy.nonchat.api.IMessageHandler;
import com.nonxedy.nonchat.api.ServiceAdapter;
import org.bukkit.event.Listener;

public final class PlatformAdapter119Legacy extends ServiceAdapter {
    public PlatformAdapter119Legacy() {
        super("1.19");
    }

    @Override
    protected Listener createChatListener(IMessageHandler handler) {
        return new ChatListener119(handler);
    }
}
